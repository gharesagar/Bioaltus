package fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.administrator.bioaltus.CheckInOutActivity;
import com.example.administrator.bioaltus.LoginActivity;
import com.example.administrator.bioaltus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import connectivity.ConnectivityReceiver;
import dmax.dialog.SpotsDialog;
import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import model.Customer;
import model.Product;
import services.ApiConstants;
import services.AppController;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExistingCustomerFragment extends Fragment implements View.OnClickListener {

    public final String TAG = "ExistingCustomerFrag";
    private ArrayList<Customer> customerArrayList;
    private ArrayList<String> customerNamesList, productsNameList;
    private ArrayList<Product> productArrayList;
    private Customer customer;
    private Product product;
    View view;
    TextView tvProductName, tvCustomerName;
    ArrayList<MultiSelectModel> productsD;
    MultiSelectDialog multiSelectDialog;
    FragmentManager manager;
    SpinnerDialog spinnerDialog;
    ImageView imgDrop1;
    String customerId;
    Button btCheckIn;
    EditText edtRemark;

    android.app.AlertDialog dialog;
    boolean isConnected;
    StringBuilder builder = new StringBuilder();

    private boolean isConnected() {
        return isConnected = ConnectivityReceiver.isConnected(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_existing, container, false);

        initViews();
        manager = getActivity().getSupportFragmentManager();

        multiSelectDialog = new MultiSelectDialog();
        dialog = new SpotsDialog.Builder().setContext(getContext()).setMessage("Please wait").build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        loadProducts();
        loadCustomers();

        btCheckIn.setOnClickListener(this);
        tvProductName.setOnClickListener(this);

        return view;
    }

    private void initViews() {
        tvProductName = view.findViewById(R.id.tvProductName);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        imgDrop1 = view.findViewById(R.id.imgDrop1);
        btCheckIn = view.findViewById(R.id.btCheckIn);
        edtRemark = view.findViewById(R.id.edtRemark);
    }

    private void loadProducts() {
        productsNameList = new ArrayList<>();
        productArrayList = new ArrayList<>();
        productsD = new ArrayList<>();

        if(builder==null) {
            builder.setLength(0);
        }
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, ApiConstants.PRODUCT_MASTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    Log.e(TAG, response);

                    JSONArray jsonArray = new JSONArray(response);

                    //json response
                    if (productArrayList.size() > 0) {
                        productArrayList.clear();
                    }
                    if (productsNameList.size() > 0) {
                        productsNameList.clear();
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String productId = jsonObject1.getString("ProductId").trim();
                        String productName = jsonObject1.getString("ProductName");
                        //product = new Product(productId, productName);

                        try {
                            productsD.add(new MultiSelectModel(i, productName));
                        } catch (NumberFormatException nfe) {
                            // Handle the condition when str is not a number.
                            Log.e(TAG, String.valueOf(nfe));
                        }
                        productArrayList.add(product);
                        productsNameList.add(productName);
                    }

                    //MultiSelectModel
                    multiSelectDialog.title("Select product") //setting title for dialog
                            .titleSize(25)
                            .positiveText("Done")
                            .negativeText("Cancel")
                            .setMinSelectionLimit(0)
                            .setMaxSelectionLimit(productsD.size())
                            .multiSelectList(productsD) // the multi select model list with ids and name
                            .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                                @Override
                                public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {
                                    //will return list of selected IDS
                                    for (int i = 0; i < selectedIds.size(); i++) {
                                        Toast.makeText(getActivity(), "Selected Ids : " + selectedIds.get(i) + "\n" +
                                                "Selected Names : " + selectedNames.get(i) + "\n" +
                                                "DataString : " + dataString, Toast.LENGTH_SHORT).show();

                                        builder.append(selectedNames.get(i)+ "\n");

                                    }
                                    tvProductName.setText(builder.toString());


                                }

                                @Override
                                public void onCancel() {
                                    Log.d(TAG, "Dialog cancelled");

                                }
                            });


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                AlertDialog.Builder al = new AlertDialog.Builder(getActivity());
                String mesaage = null;
                if (error instanceof NetworkError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof ServerError) {
                    mesaage = "The server could not be found. Please try again after some time!!";
                } else if (error instanceof AuthFailureError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof NoConnectionError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof TimeoutError) {
                    mesaage = "Connection TimeOut! Please check your internet connection.";
                } else if (error instanceof ParseError) {
                    mesaage = "Indicates that the server response could not be parsed.";
                } else {
                    mesaage = "Something went wrong. Please try again after some time!!";
                }
                al.setTitle("Error");
                al.setMessage(mesaage);
                al.show();

            }
        });

        int MY_SOCKET_TIMEOUT_MS = 30000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void loadCustomers() {
        customerArrayList = new ArrayList<>();
        customerNamesList = new ArrayList<>();

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, ApiConstants.CUSTOMER_MASTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    Log.e(TAG, response);
                    JSONArray jsonArray = new JSONArray(response);

                    //json response
                    if (customerArrayList.size() > 0) {
                        customerArrayList.clear();
                    }
                    if (customerNamesList.size() > 0) {
                        customerNamesList.clear();
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String customerId = jsonObject1.getString("CustomerId");
                        String cutomerName = jsonObject1.getString("CustomerName");
                        customer = new Customer(customerId, cutomerName);
                        customerArrayList.add(customer);
                        customerNamesList.add(cutomerName);
                    }

                    spinnerDialog = new SpinnerDialog(getActivity(), customerNamesList, "Select customer");
                    spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String item, int pos) {
                            customerId = getCustomerId(pos);
                            tvCustomerName.setText(item);
                        }
                    });

                    tvCustomerName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spinnerDialog.showSpinerDialog();
                        }
                    });

                    imgDrop1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spinnerDialog.showSpinerDialog();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                AlertDialog.Builder al = new AlertDialog.Builder(getContext());
                String mesaage = null;
                if (error instanceof NetworkError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof ServerError) {
                    mesaage = "The server could not be found. Please try again after some time!!";
                } else if (error instanceof AuthFailureError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof NoConnectionError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof TimeoutError) {
                    mesaage = "Connection TimeOut! Please check your internet connection.";
                } else if (error instanceof ParseError) {
                    mesaage = "Indicates that the server response could not be parsed.";
                } else {
                    mesaage = "Something went wrong. Please try again after some time!!";
                }
                al.setTitle("Error");
                al.setMessage(mesaage);
                al.show();

            }
        });

        int MY_SOCKET_TIMEOUT_MS = 30000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private String getCustomerId(int pos) {
        return customerArrayList.get(pos).getCustomerId();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tvProductName:
                multiSelectDialog.show(getFragmentManager(), "multiSelectDialog");
                break;
            case R.id.btCheckIn:
                empCheckIn();
                break;
        }
    }

    private void empCheckIn() {

        String productName = tvProductName.getText().toString().trim();
        String customerName = tvCustomerName.getText().toString().trim();
        String remark = edtRemark.getText().toString().trim();


        if (remark.isEmpty() && remark.matches("")) {
            Toast.makeText(getContext(), "Select remark", Toast.LENGTH_SHORT).show();
        } else if (productName.equalsIgnoreCase("Product Name")) {
            Toast.makeText(getContext(), "Select product", Toast.LENGTH_SHORT).show();

        } else if (customerName.equalsIgnoreCase("Customer Name")) {
            Toast.makeText(getContext(), "Select Customer", Toast.LENGTH_SHORT).show();

        } else {

            if (isConnected()) {

                checkIn();
            } else {
                Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void checkIn() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.CHECKTIN_REGISTRATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    Log.e(TAG, response);

                    JSONObject jsonObject = new JSONObject(response);
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "Checkin successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), CheckInOutActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, error.getMessage());
                dialog.dismiss();

                android.app.AlertDialog.Builder al = new android.app.AlertDialog.Builder(getContext());
                String mesaage = null;
                if (error instanceof NetworkError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof ServerError) {
                    mesaage = "The server could not be found. Please try again after some time!!";
                } else if (error instanceof AuthFailureError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof NoConnectionError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof TimeoutError) {
                    mesaage = "Connection TimeOut! Please check your internet connection.";
                } else if (error instanceof ParseError) {
                    mesaage = "Indicates that the server response could not be parsed.";
                } else {
                    mesaage = "Something went wrong. Please try again after some time!!";
                }
                al.setTitle("Error");
                al.setMessage(mesaage);
                al.show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Location", "Thane");
                params.put("CheckInDate", "05-03-2019");
                params.put("CheckInTime", "10.00 Am");
                params.put("CustomerID", tvCustomerName.getText().toString());
                params.put("ProductID", tvProductName.getText().toString());
                params.put("Remark", "This is remarks");
                params.put("EmpID", "222");
                return params;
            }
        };

        int MY_SOCKET_TIMEOUT_MS = 30000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(stringRequest);
        dialog.show();
    }
}

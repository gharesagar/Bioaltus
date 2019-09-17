package fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
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
import bio.example.administrator.bioaltus.CheckInOutActivity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.bio.bioaltus.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import connectivity.ConnectivityReceiver;
import dmax.dialog.SpotsDialog;
import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import model.Customer;
import model.Product;
import services.ApiConstants;
import services.AppController;
import services.GpsUtils;
import services.SessionManager;
import static androidx.core.content.PermissionChecker.checkSelfPermission;


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

    SessionManager sessionManager;
    HashMap<String, String> empData;
    String empId, productName, customerName, remark;

    String locAddress;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isGPS = false;
    double longitude, latitude;


    private boolean isConnected() {
        return isConnected = ConnectivityReceiver.isConnected(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_existing, container, false);


        sessionManager = new SessionManager(getActivity());
        empData = new HashMap<>();
        empData = sessionManager.getEmpDetails();
        empId = empData.get(SessionManager.EMP_CODE);

        initViews();
        manager = getActivity().getSupportFragmentManager();

        multiSelectDialog = new MultiSelectDialog();

        loadProducts();
        loadCustomers();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds

        new GpsUtils(getActivity()).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        Log.e(TAG, "latitude" + latitude + " longitude" + longitude);

                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                            locAddress = addresses.get(0).getAddressLine(0);

                            checkIn();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };


        btCheckIn.setOnClickListener(this);
        tvProductName.setOnClickListener(this);
        tvCustomerName.setOnClickListener(this);
        imgDrop1.setOnClickListener(this);


        spinnerDialog = new SpinnerDialog(getActivity(), customerNamesList, "Select customer");
        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int pos) {
                customerId = getCustomerId(pos);
                tvCustomerName.setText(item);
            }
        });


        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        //Removelocationupdates
        fusedLocationClient.removeLocationUpdates(locationCallback);
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

        if (builder == null) {
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

                    tvProductName.setEnabled(true);
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

                                        builder.append(selectedNames.get(i) + "\n");

                                    }
                                    tvProductName.setText("");
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

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.CUSTOMER_MASTER, new Response.Listener<String>() {
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

                    tvCustomerName.setEnabled(true);

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
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("EmpID", empId);
                return params;
            }
        };

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
            case R.id.tvCustomerName:
                spinnerDialog.showSpinerDialog();
                break;
            case R.id.imgDrop1:
                spinnerDialog.showSpinerDialog();
                break;
        }
    }

    private void empCheckIn() {

        productName = tvProductName.getText().toString().trim();
        customerName = tvCustomerName.getText().toString().trim();
        remark = edtRemark.getText().toString().trim();


        if (remark.isEmpty() && remark.matches("")) {
            Toast.makeText(getContext(), "Select remark", Toast.LENGTH_SHORT).show();
        } else if (productName.equalsIgnoreCase("Product Name")) {
            Toast.makeText(getContext(), "Select product", Toast.LENGTH_SHORT).show();

        } else if (customerName.equalsIgnoreCase("Customer Name")) {
            Toast.makeText(getContext(), "Select Customer", Toast.LENGTH_SHORT).show();

        } else {

            if (isConnected()) {

                if (!isGPS) {
                    Toast.makeText(getActivity(), "Please turn on GPS", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                ApiConstants.LOCATION_REQUEST);

                    } else {
                        getDeviceLocation();
                    }
                } else {
                    getDeviceLocation();
                }
            } else {
                Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();

                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                                locAddress = addresses.get(0).getAddressLine(0);

                                checkIn();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {

                            //Location is null , request location
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    private void checkIn() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.CHECKTIN_REGISTRATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    Log.e(TAG, response);
                    //  {"PK_CID":0,"CID_ProductID":"AUCTIROZ 20\n","CID_CheckInID":"39","CID_EmpID":"222"}
                    JSONObject jsonObject = new JSONObject(response);
                    dialog.dismiss();

                    //save checkin data to check visibility of checkin and checkout cardview
                    Toast.makeText(getActivity(), "Checkin successful!", Toast.LENGTH_SHORT).show();
                    String mChecckInId = jsonObject.getString("CID_CheckInID");
                    sessionManager.saveCheckInData(mChecckInId);
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
                Log.e(TAG, error.toString());
                dialog.dismiss();

                android.app.AlertDialog.Builder al = new android.app.AlertDialog.Builder(getActivity());
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
                //locationAddress
                params.put("Location", locAddress);
                params.put("CheckInDate", "01/01/2019");
                params.put("CheckInTime", "default");
                params.put("CustomerID", customerId);
                params.put("ProductID", tvProductName.getText().toString());
                params.put("Remark", edtRemark.getText().toString());
                params.put("EmpID", empId);

                Log.e(TAG, locAddress);
                return params;
            }
        };

        int MY_SOCKET_TIMEOUT_MS = 30000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(stringRequest);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setMessage("Please wait").build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ApiConstants.LOCATION_REQUEST: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getActivity(),  Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        longitude = location.getLongitude();
                                        latitude = location.getLatitude();

                                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                        try {
                                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                                            locAddress = addresses.get(0).getAddressLine(0);

                                            checkIn();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {

                                        //Location is null , request location
                                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                                ActivityCompat.checkSelfPermission(getActivity(),  Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return;
                                        }
                                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.toString());
                                }
                            });

                } else {
                    Toast.makeText(getActivity(), "Permission denied. Please allow go to settings", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ApiConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

}

package fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import connectivity.ConnectivityReceiver;
import dmax.dialog.SpotsDialog;
import services.ApiConstants;
import services.AppController;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewCustomerFragment extends Fragment {

    public final String TAG = "NewCustomerFrag";

    View view;
    EditText edtName,edtMobile,edtEmailId,edtAddress;
    String name,mobile,email,address;
    Button btAdd;

    android.app.AlertDialog dialog;
    boolean isConnected;

    private boolean isConnected() {
        return isConnected = ConnectivityReceiver.isConnected(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_new_customer, container, false);

        edtName=view.findViewById(R.id.edtName);
        edtEmailId=view.findViewById(R.id.edtEmailId);
        edtMobile=view.findViewById(R.id.edtMobile);
        edtAddress=view.findViewById(R.id.edtAddress);
        btAdd=view.findViewById(R.id.btAdd);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setMessage("Please wait").build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewCustomer();
            }
        });


        return view;
    }

    private void addNewCustomer() {
        name=edtName.getText().toString().trim();
        mobile=edtMobile.getText().toString().trim();
        email=edtEmailId.getText().toString().trim();
        address=edtAddress.getText().toString().trim();

        if(name.isEmpty()&& name.matches("")){
            Toast.makeText(getContext(), "Add name", Toast.LENGTH_SHORT).show();

        }else if (mobile.isEmpty()&& mobile.matches("")){
            Toast.makeText(getContext(), "Add mobile", Toast.LENGTH_SHORT).show();

        }else if(email.isEmpty()&& email.matches("")){
            Toast.makeText(getContext(), "Add email", Toast.LENGTH_SHORT).show();

        }else if(address.isEmpty()&& address.matches("")){
            Toast.makeText(getContext(), "Add address", Toast.LENGTH_SHORT).show();

        }else {
            isConnected();
            if(isConnected) {
                addCustomer();
            }else {
                Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private void addCustomer() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.ADD_CUSTOMER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    Log.e(TAG, response);

                    JSONObject jsonObject = new JSONObject(response);

                    String message = jsonObject.getString("Message");
                    Boolean error = jsonObject.getBoolean("Error");

                    if (error) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        //get json data first
      /*                  int empCode = jsonObject.getInt("UserName");
                        String location = jsonObject.getString("Location");*/

                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                    }

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
                params.put("CustomerName", name);
                params.put("CustomerAddress", address);
                params.put("CustomerMobileNo", mobile);
                params.put("CustomerEmailId",email);

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

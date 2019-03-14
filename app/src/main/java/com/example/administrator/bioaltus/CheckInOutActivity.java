package com.example.administrator.bioaltus;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import connectivity.ConnectivityReceiver;
import dmax.dialog.SpotsDialog;
import services.ApiConstants;
import services.AppController;

public class CheckInOutActivity extends AppCompatActivity {

    CardView cv1,cv2,cv3;
    android.app.AlertDialog dialog;

    boolean isConnected;

    private boolean isConnected() {
        return isConnected = ConnectivityReceiver.isConnected(CheckInOutActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_out);

        cv1=findViewById(R.id.cv1);
        cv2=findViewById(R.id.cv2);
        cv3=findViewById(R.id.cv3);
        cv3.setVisibility(View.VISIBLE);


        dialog = new SpotsDialog.Builder().setContext(CheckInOutActivity.this).setMessage("Please wait").build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        cv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckInOutActivity.this,MainActivity.class));
            }
        });

        cv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckInOutActivity.this,TrackEmpActivity.class));
            }
        });

        cv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isConnected();

                if(isConnected) {
                    Toast.makeText(getApplicationContext(), "Checkout screen", Toast.LENGTH_SHORT).show();
                    checkOut();
                }else {
                    Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void checkOut() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.CHECKOUT_REGISTRATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    boolean error=jsonObject.getBoolean("Error");
                    String message=jsonObject.getString("Message");

                    if(error){
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }else {
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        cv3.setVisibility(View.GONE);

                    }
                } catch (JSONException e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, error.getMessage());
                dialog.dismiss();

                AlertDialog.Builder al = new AlertDialog.Builder(getApplicationContext());
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
                params.put("CheckInId", "5");
                params.put("EmpID", "223");
                params.put("CheckOutDate", "06-03-2019");
                params.put("CheckOutTime","10:00 Pm");
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

package maitritechnology.example.administrator.bioaltus;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
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
import com.maitritechnology.bioaltus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import connectivity.ConnectivityReceiver;
import dmax.dialog.SpotsDialog;
import services.ApiConstants;
import services.AppController;
import services.SessionManager;

public class CheckInOutActivity extends AppCompatActivity {

    CardView cv1,cv2,cv3;
    android.app.AlertDialog dialog;

    boolean isConnected;

    SessionManager sessionManager;
    HashMap<String,String> empData,checkInData;
    private String empId,mCheckInId;
    private Button btLogout;


    private boolean checkConnection() {
        return isConnected = ConnectivityReceiver.isConnected(CheckInOutActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_out);

        cv1=findViewById(R.id.cv1);
        cv2=findViewById(R.id.cv2);
        cv3=findViewById(R.id.cv3);
        btLogout=findViewById(R.id.btLogout);

        cv3.setVisibility(View.VISIBLE);

        sessionManager=new SessionManager(this);
        empData=new HashMap<>();
        empData=sessionManager.getEmpDetails();
        empId=empData.get(SessionManager.EMP_CODE);

        checkInData=new HashMap<>();
        checkInData=sessionManager.getCheckInDetails();
        mCheckInId=checkInData.get(SessionManager.CHECKIN_ID);

        dialog = new SpotsDialog.Builder().setContext(CheckInOutActivity.this).setMessage("Please wait").build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        //it has checkin id
        if(!mCheckInId.contains("default")){
            cv1.setVisibility(View.GONE);
            cv2.setVisibility(View.VISIBLE);

        }else {
            cv2.setVisibility(View.GONE);
            cv1.setVisibility(View.VISIBLE);

        }

        //checkIn cardview
        cv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckInOutActivity.this,MainActivity.class));
            }
        });

        //checkOut cardview
        cv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkConnection();
                if(isConnected) {
                    checkOut();
                }else {
                    Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckInOutActivity.this,TrackEmpActivity.class));
            }
        });

        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.logoutEmp();
                Intent intent=new Intent(CheckInOutActivity.this,LoginActivity2.class);
                startActivity(intent);
                finish();
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
                        cv1.setVisibility(View.VISIBLE);
                        cv2.setVisibility(View.GONE);

                        //After checkout pass "default" as checkout id
                        sessionManager.saveCheckInData("default");
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
                params.put("CheckInId", mCheckInId);
                params.put("EmpID", empId);
                params.put("CheckOutDate", "01-01-2019");
                params.put("CheckOutTime","default");
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

package maitritechnology.example.administrator.bioaltus;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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


public class LoginActivity2 extends AppCompatActivity {

    public String TAG = "LoginActivity";
    Button btLogin;
    EditText edtEmployeeCode, edtPassword;
    String empCode, password;
    boolean isConnected;
    RelativeLayout rootLayout;
    SessionManager sessionManager;
    android.app.AlertDialog dialog;


    private void checkInternet() {
        isConnected = ConnectivityReceiver.isConnected(LoginActivity2.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        btLogin = findViewById(R.id.btLogin);
        edtEmployeeCode = findViewById(R.id.edtEmployeeCode);
        edtPassword = findViewById(R.id.edtPassword);
        rootLayout = findViewById(R.id.rootLayout);

        sessionManager = new SessionManager(LoginActivity2.this);
        dialog = new SpotsDialog.Builder().setContext(LoginActivity2.this).setMessage("Please wait").build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);


        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                empLogin();
            }
        });

    }

    private void empLogin() {
        boolean failFalg = false;
        empCode = edtEmployeeCode.getText().toString().trim();
        password = edtPassword.getText().toString().trim();

        if (empCode.isEmpty() && empCode.matches("")) {
            edtEmployeeCode.setError("Enter employee code");
            failFalg = true;
        }

        if (password.isEmpty() && password.matches("")) {
            edtPassword.setError("Enter password");
            failFalg = true;
        }

        if (!failFalg) {
            checkInternet();

            if (isConnected) {

                login();


            } else {
                Snackbar.make(rootLayout, "No internet connection!", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void login() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    Log.e(TAG, response);

                    JSONObject jsonObject = new JSONObject(response);

                    String message = jsonObject.getString("Message");
                    Boolean error = jsonObject.getBoolean("Error");

                    if (error) {
                        dialog.dismiss();
                        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        //get json data first
                        int empCode = jsonObject.getInt("UserName");
                        String location = jsonObject.getString("Location");
                        String empName = jsonObject.getString("EmpName");

                        sessionManager.createEmpLoginSession(empCode, location, empName);
                        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity2.this, CheckInOutActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        // overridePendingTransitionEnter();
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

                AlertDialog.Builder al = new AlertDialog.Builder(LoginActivity2.this);
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
                params.put("UserName", empCode);
                params.put("password", password);
                params.put("Location", "Any");
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




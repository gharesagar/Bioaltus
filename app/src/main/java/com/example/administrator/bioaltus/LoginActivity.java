package com.example.administrator.bioaltus;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import connectivity.ConnectivityReceiver;
import dmax.dialog.SpotsDialog;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import services.ApiConstants;
import services.AppController;
import services.SessionManager;

public class LoginActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener, EasyPermissions.PermissionCallbacks {

    public String TAG = "LoginActivity";
    Button btLogin;
    EditText edtEmployeeCode, edtPassword;
    String empCode, password, mLocation;
    boolean isConnected;
    RelativeLayout rootLayout;
    ConnectivityReceiver connectivityReceiver;
    IntentFilter intentFilter;
    public final int PERMISSION_REQUEST_CODE = 101;
    public final int PERM_REQUEST_CODE = 10;
    android.app.AlertDialog dialog;

    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;
    SessionManager sessionManager;
    LocationListener locationListener;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btLogin = findViewById(R.id.btLogin);
        edtEmployeeCode = findViewById(R.id.edtEmployeeCode);
        edtPassword = findViewById(R.id.edtPassword);
        rootLayout = findViewById(R.id.rootLayout);

        sessionManager = new SessionManager(LoginActivity.this);
        dialog = new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Please wait").build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 /*   if (checkPermission()) {

                    } else {
                        requestPermission();
                    }*/

                givePermission();
            } else {
                //below M
                //getLocation();
            }

        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Error Mesg : " + e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }


        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empLogin();

            }
        });

        setConnectivityBroadcastReceiver();
    }

    @AfterPermissionGranted(PERM_REQUEST_CODE)
    private void givePermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {

          //  getLocation();
        } else {
            EasyPermissions.requestPermissions(this, "We need permission of location",
                    PERM_REQUEST_CODE, perms);
        }
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

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(LoginActivity.this, REQUEST_LOCATION);

                                // finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }


    private boolean checkPermission() {

        int coarseLocation = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineLocation = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (coarseLocation == PackageManager.PERMISSION_GRANTED && fineLocation == PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSSION GRANTED", "PERMISSSION");

            return true;
        } else {
            Log.e("PERMISSSION Denied", "PERMISSSION Denied");

            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);

     /*   switch (requestCode) {

            case PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(LoginActivity.this, "Permission denied, Now you can't login", Toast.LENGTH_LONG).show();
                }
                break;
        }*/
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

                        sessionManager.createEmpLoginSession(empCode);
                        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, CheckInOutActivity.class);
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

                AlertDialog.Builder al = new AlertDialog.Builder(LoginActivity.this);
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
                params.put("Location", "bhandup east mumbai");
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


    private void setConnectivityBroadcastReceiver() {
        //create intent filter instance
        intentFilter = new IntentFilter();
        // Add network connectivity change action.
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // Set broadcast receiver priority.
        intentFilter.setPriority(100);
        connectivityReceiver = new ConnectivityReceiver();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(connectivityReceiver, intentFilter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.connectivityReceiver != null) {
            unregisterReceiver(connectivityReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // register connection status listener
        AppController.getInstance().setConnectivityListener(this);
    }


    private void checkInternet() {
        isConnected = ConnectivityReceiver.isConnected(LoginActivity.this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        if (isConnected) {



        }else {

        }

    }

    private void getLocation() {

        this.setFinishOnTouchOutside(true);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, "Gps already enabled", Toast.LENGTH_SHORT).show();
        }
        // Todo Location Already on  ... end
        if (!hasGPSDevice(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, "Gps not Supported", Toast.LENGTH_SHORT).show();
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            enableLoc();
        } else {
            Toast.makeText(LoginActivity.this, "Gps already enabled", Toast.LENGTH_SHORT).show();

            // Define a listener that responds to location updates
            locationListener = new LocationListener() {

                public void onLocationChanged(Location location) {

                    try {
                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(LoginActivity.this, Locale.getDefault());

                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                        //mLocation = address + "," + city + "," + state + "," + country + "," + postalCode + "," + knownName;
                        mLocation = address;
                        Log.e("Location", mLocation);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5, 0, locationListener);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        //// Remove the listener you previously added
       // locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {

            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}

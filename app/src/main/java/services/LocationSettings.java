package services;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
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

public class LocationSettings {

    final static int REQUEST_LOCATION = 199;
    public static GoogleApiClient googleApiClient;
    public static final String TAG="LocationSettings";

    public LocationSettings(final Activity context) {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

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

                                        case LocationSettingsStatusCodes.SUCCESS:

                                            // NO need to show the dialog;

                                            break;
                                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                            try {
                                                // Show the dialog by calling startResolutionForResult(),
                                                // and check the result in onActivityResult().
                                                status.startResolutionForResult(context, REQUEST_LOCATION);
                                                // finish();
                                            } catch (IntentSender.SendIntentException e) {
                                                // Ignore the error.
                                            }
                                            break;

                                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                            // Location settings are unavailable so not possible to show any dialog now
                                            break;
                                    }
                                }
                            });
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.e(TAG, "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();

            googleApiClient.connect();
        }
    }


    public void disconnect(){
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
}

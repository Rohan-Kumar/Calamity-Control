package com.nest.calamitycontrol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NewVolunteerActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    EditText name, place, number;
    GeoFire geoFire;

    double latitude;
    double longitude;

    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_volunteer);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("geoFire");
        geoFire = new GeoFire(ref);

        dialog = new ProgressDialog(NewVolunteerActivity.this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Fetching location");
        dialog.show();


        connectToApi();

        init();
    }

    private void init() {
        name = (EditText) findViewById(R.id.name);
        place = (EditText) findViewById(R.id.place);
        number = (EditText) findViewById(R.id.number);
    }


    public void volunteer(View view) {
        if (name.getText().toString().equals("") || place.getText().toString().equals("") || number.getText().toString().equals("")) {
            Toast.makeText(NewVolunteerActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
        } else {
            sendData();
        }
    }

    private void sendData() {
        DatabaseReference databaseRef;
        databaseRef = FirebaseDatabase.getInstance().getReference("volunteers");

        String key = databaseRef.push().getKey();

        Map<String, Object> postValues = new HashMap<>();
        postValues.put("name", name.getText().toString());
        postValues.put("place", place.getText().toString());
        postValues.put("number", number.getText().toString());


        geoFire.setLocation(key, new GeoLocation(latitude, longitude));

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + key, postValues);

        databaseRef.updateChildren(childUpdates);

        Toast.makeText(this, "Thank you for your registering as a volunteer", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NewVolunteerActivity.this, MainActivity.class));
        finish();
    }

    public void connectToApi() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
                Log.d("TAG", "connect");
            }
        } else {
            Log.e("TAG", "unable to connect to google play services.");
        }

    }

    private void getLocation() {
        // shows an error but works if this permission check is not added.
        // here you get the location with location service/gps is on
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // milliseconds
        locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//        getLocation();
        checkGps();

    }

    public void checkGps() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(NewVolunteerActivity.this, 1000);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                getLocation();
            } else {
                checkGps();
            }
        }
    }


}

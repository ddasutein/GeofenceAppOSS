package dev.dasutein.geofenceapp.api;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.dasutein.geofenceapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationServicesHelper {

    /** DEBUGGING **/
    private static final String TAG = "LocationServicesHelper";

    /** LOCATIONS **/
    private LocationRequest mLocationRequest;
    private Location location;

    /** CONTEXT **/
    private Context context;

    /** STRINGS **/
    public String _debugLocationCoordinates;

    /** FLOATS **/
    private long UPDATE_INTERVAL = 10000;
    private long FASTEST_INTERVAL = 5000;


    public LocationServicesHelper(Context context){
        this.context = context;
    }

    public void InitializeLocationServices(){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showLocationSettingsError();
        }
        else {
            StartLocationUpdates();
        }
    }

    private void showLocationSettingsError() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.common_error_location_services_disabled);
        builder.setMessage(R.string.common_error_location_services_message);
        builder.setPositiveButton(R.string.common_location_services, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                System.exit(0);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void StartLocationUpdates() {
        // Create the location request to receive updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettings object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Checks if location settings are OK
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }else {
            getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            onLocationChanged(locationResult.getLastLocation());
//                            getLastLocation();
                        }
                    },
                    Looper.myLooper());
        }

    }

    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        _debugLocationCoordinates = Double.valueOf(latLng.latitude) + "," + Double.valueOf(latLng.longitude);

        this.location = location;

    }

    public void getLastLocation() {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        else {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {

                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                //onLocationChanged(location);
                            }
                        }
                    });
        }

    }
}

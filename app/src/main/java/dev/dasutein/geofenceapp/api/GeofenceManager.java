package dev.dasutein.geofenceapp.api;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import dev.dasutein.geofenceapp.activities.MainActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class GeofenceManager {

    private static final String TAG = "GeofenceManager";

    /** ARRAYLIST TO STORE GEOFENCES **/
    private static ArrayList<Geofence> mGeofenceList;

    /** GEOFENCE API **/
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;

    /** CONTEXT **/
    private Context context;

    /** DEMO PURPOSES ONLY **/
    private String geofenceName = "Tokyo Tower (東京タワー)";
    private float geofenceRadius = 50.0f; // 50 meters
    public LatLng latLng = new LatLng(35.6586545, 139.7454603);

    /** BOOLEANS **/
    private boolean isGeofenceLoaded = false;

    public GeofenceManager(Context context){
        this.context = context;
    }

    public void InitializeGeofence() {

        mGeofenceList = new ArrayList<>();
        mGeofencingClient = LocationServices.getGeofencingClient(context);

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        /* ALWAYS implement a check if Geofences are loaded, whether it is hard coded or from a
        database. You do not want to keep loading duplicate Geofences into the device's
        Location Services which may impact device performance or battery life.
         */
        if (!isGeofenceLoaded){

            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(geofenceName)
                    .setCircularRegion(latLng.latitude, latLng.longitude, geofenceRadius)
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            addGeofences();
        }
        else {
            Toast.makeText(context, "Please unload the Geofences before reloading", Toast.LENGTH_SHORT).show();
        }

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        builder.addGeofences(mGeofenceList);

        Log.d(TAG, "GEOFENCE_MODULE: Calling GeofencingRequest");
        return builder.build();

    }

    private void addGeofences() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Log.d(TAG, "Geofence added successfully");
                        DrawGeofenceOnMap();
                        isGeofenceLoaded = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        Log.d(TAG, "Failed to add Geofence");
                    }
                });
    }

    private void removeGeofences() {

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnSuccessListener((Activity) context, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                MainActivity.map.clear();
                Log.d(TAG, "Geofence removed");
            }
        })
        .addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to remove geofence");
            }
        });

    }

    public void StopGeofenceMonitoring(){

        if (mGeofencingClient != null){
            if (isGeofenceLoaded){
                removeGeofences();
                Toast.makeText(context, "Removing Geofences", Toast.LENGTH_SHORT).show();
                isGeofenceLoaded = false;
            }
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */

    private PendingIntent getGeofencePendingIntent() {
        Log.d(TAG, "GEOFENCE: Creating Geofence Pending Intent - getGeofencePendingIntent");
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void DrawGeofenceOnMap() {
        Log.d(TAG, "Draw Geofence on Map");

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(latLng.latitude, latLng.longitude))
                .fillColor( Color.argb(100, 150,150,150) )
                .strokeColor(Color.RED)
                .radius(geofenceRadius)
                .strokeWidth(5);

        MainActivity.map.addCircle(circleOptions);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(latLng.latitude, latLng.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        MainActivity.map.addMarker(markerOptions);

    }


}

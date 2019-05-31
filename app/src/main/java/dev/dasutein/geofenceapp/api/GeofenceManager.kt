package dev.dasutein.geofenceapp.api

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

import dev.dasutein.geofenceapp.activities.MainActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

import java.util.ArrayList

class GeofenceManager(
        /** CONTEXT  */
        private val context: Context) {

    /** GEOFENCE API  */
    private var mGeofencingClient: GeofencingClient? = null
    private var mGeofencePendingIntent: PendingIntent? = null

    /** DEMO PURPOSES ONLY  */
    private val geofenceName = "Tokyo Tower (東京タワー)"
    private val geofenceRadius = 50.0f // 50 meters
    var latLng = LatLng(35.6586545, 139.7454603)

    /** BOOLEANS  */
    private var isGeofenceLoaded = false

    private val geofencingRequest: GeofencingRequest
        get() {
            val builder = GeofencingRequest.Builder()

            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            builder.addGeofences(mGeofenceList)

            Log.d(TAG, "GEOFENCE_MODULE: Calling GeofencingRequest")
            return builder.build()

        }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */

    private// Reuse the PendingIntent if we already have it.
    val geofencePendingIntent: PendingIntent?
        get() {
            Log.d(TAG, "GEOFENCE: Creating Geofence Pending Intent - getGeofencePendingIntent")
            if (mGeofencePendingIntent != null) {
                return mGeofencePendingIntent
            }
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            mGeofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            return mGeofencePendingIntent
        }

    fun InitializeGeofence() {

        mGeofenceList = ArrayList()
        mGeofencingClient = LocationServices.getGeofencingClient(context)

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null

        /* ALWAYS implement a check if Geofences are loaded, whether it is hard coded or from a
        database. You do not want to keep loading duplicate Geofences into the device's
        Location Services which may impact device performance or battery life.
         */
        if (!isGeofenceLoaded) {

            mGeofenceList!!.add(Geofence.Builder()
                    .setRequestId(geofenceName)
                    .setCircularRegion(latLng.latitude, latLng.longitude, geofenceRadius)
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build())

            addGeofences()
        } else {
            Toast.makeText(context, "Please unload the Geofences before reloading", Toast.LENGTH_SHORT).show()
        }

    }

    private fun addGeofences() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return
        }
        mGeofencingClient!!.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    // Geofences added
                    Log.d(TAG, "Geofence added successfully")
                    DrawGeofenceOnMap()
                    isGeofenceLoaded = true
                }
                .addOnFailureListener {
                    // Failed to add geofences
                    Log.d(TAG, "Failed to add Geofence")
                }
    }

    private fun removeGeofences() {

        mGeofencingClient!!.removeGeofences(geofencePendingIntent).addOnSuccessListener(context as Activity) {
            MainActivity.map.clear()
            Log.d(TAG, "Geofence removed")
        }
                .addOnFailureListener(context) { Log.e(TAG, "Failed to remove geofence") }

    }

    fun StopGeofenceMonitoring() {

        if (mGeofencingClient != null) {
            if (isGeofenceLoaded) {
                removeGeofences()
                Toast.makeText(context, "Removing Geofences", Toast.LENGTH_SHORT).show()
                isGeofenceLoaded = false
            }
        }
    }

    private fun DrawGeofenceOnMap() {
        Log.d(TAG, "Draw Geofence on Map")

        val circleOptions = CircleOptions()
                .center(LatLng(latLng.latitude, latLng.longitude))
                .fillColor(Color.argb(100, 150, 150, 150))
                .strokeColor(Color.RED)
                .radius(geofenceRadius.toDouble())
                .strokeWidth(5f)

        MainActivity.map.addCircle(circleOptions)

        val markerOptions = MarkerOptions()
                .position(LatLng(latLng.latitude, latLng.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        MainActivity.map.addMarker(markerOptions)

    }

    companion object {

        private val TAG = "GeofenceManager"

        /** ARRAYLIST TO STORE GEOFENCES  */
        private var mGeofenceList: ArrayList<Geofence>? = null
    }


}

package dev.dasutein.geofenceapp.api

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log

import androidx.core.app.ActivityCompat

import com.example.dasutein.geofenceapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener

import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient

class LocationServicesHelper(
        /** CONTEXT  */
        private val context: Context) {

    /** LOCATIONS  */
    private var mLocationRequest: LocationRequest? = null
    private var location: Location? = null

    /** STRINGS  */
    var _debugLocationCoordinates: String = ""

    /** FLOATS  */
    private val UPDATE_INTERVAL: Long = 10000
    private val FASTEST_INTERVAL: Long = 5000

    fun InitializeLocationServices() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showLocationSettingsError()
        } else {
            StartLocationUpdates()
        }
    }

    private fun showLocationSettingsError() {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.common_error_location_services_disabled)
        builder.setMessage(R.string.common_error_location_services_message)
        builder.setPositiveButton(R.string.common_location_services) { dialogInterface, i ->
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            System.exit(0)
        }
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun StartLocationUpdates() {
        // Create the location request to receive updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        mLocationRequest!!.interval = UPDATE_INTERVAL
        mLocationRequest!!.fastestInterval = FASTEST_INTERVAL

        // Create LocationSettings object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        // Checks if location settings are OK
        val settingsClient = LocationServices.getSettingsClient(context)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return
        } else {
            getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    onLocationChanged(locationResult!!.lastLocation)
                    //                            getLastLocation();
                }
            },
                    Looper.myLooper())
        }

    }

    fun onLocationChanged(location: Location) {

        val latLng = LatLng(location.latitude, location.longitude)
        _debugLocationCoordinates = java.lang.Double.valueOf(latLng.latitude).toString() + "," + java.lang.Double.valueOf(latLng.longitude)

        this.location = location

    }

    fun getLastLocation() {
        val locationClient = getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        } else {
            locationClient.lastLocation
                    .addOnSuccessListener { location ->
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            //onLocationChanged(location);
                        }
                    }
        }

    }

    companion object {

        /** DEBUGGING  */
        private val TAG = "LocationServicesHelper"
    }
}

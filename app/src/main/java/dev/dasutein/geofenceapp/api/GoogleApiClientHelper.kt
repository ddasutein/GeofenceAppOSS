package dev.dasutein.geofenceapp.api

import android.content.Context
import android.os.Bundle
import android.util.Log

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import kotlin.properties.Delegates

class GoogleApiClientHelper(
        /** CONTEXT  */
        private val context: Context) : GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    /** GOOGLE API  */
    private lateinit var googleApiClient: GoogleApiClient

    private val isConnected: Boolean
        get() = googleApiClient != null && googleApiClient!!.isConnected

    fun connect() {
        if (googleApiClient != null) {
            Log.d(TAG, "Connecting Google API Client...")
            googleApiClient!!.connect()
        }
    }

    fun disconnect() {
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            Log.d(TAG, "Disconnecting Google API Client... ❌")
            googleApiClient!!.disconnect()
        }
    }

    fun BuildGoogleApiClient() {
        Log.d(TAG, "Building Google API Client...")
        googleApiClient = GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onConnected(bundle: Bundle?) {
        Log.d(TAG, "Google API Client is connected ✔")
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "Google API Client connection suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "Failed to connect Google API Client. Please try again.")

    }

    companion object {

        /** DEBUGGING  */
        private val TAG = "GoogleApiClientHelper"
    }
}

package dev.dasutein.geofenceapp.api;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class GoogleApiClientHelper implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    /** DEBUGGING **/
    private static final String TAG = "GoogleApiClientHelper";

    /** CONTEXT **/
    private Context context;

    /** GOOGLE API **/
    public GoogleApiClient googleApiClient;

    public GoogleApiClientHelper(Context context){
        this.context = context;
    }

    public GoogleApiClient getGoogleApiClient(){
        return this.googleApiClient;
    }

    public void connect(){
        if (googleApiClient != null){
            Log.d(TAG, "Connecting Google API Client...");
            googleApiClient.connect();
        }
    }

    public void disconnect(){
        if (googleApiClient != null && googleApiClient.isConnected()){
            Log.d(TAG, "Disconnecting Google API Client... ❌");
            googleApiClient.disconnect();
        }
    }

    private boolean isConnected(){
        return googleApiClient != null && googleApiClient.isConnected();
    }

    public void BuildGoogleApiClient(){
        Log.d(TAG, "Building Google API Client...");
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google API Client is connected ✔");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google API Client connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect Google API Client. Please try again.");

    }
}

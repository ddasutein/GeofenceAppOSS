package com.example.dasutein.geofenceapp.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dasutein.geofenceapp.GeofenceClass.Constants;
import com.example.dasutein.geofenceapp.R;
import com.example.dasutein.geofenceapp.WebViewActivities.AboutGeofenceWebView;
import com.example.dasutein.geofenceapp.WebViewActivities.NoActiveNetworkUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.google.android.gms.location.LocationServices.getGeofencingClient;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, View.OnClickListener, GoogleMap.OnMarkerClickListener,
        OnCompleteListener<Void>, ResultCallback<Status> {

    // Logcat
    private static final String TAG = "MainActivity";

    // Google Maps and Google Play API
    private GoogleApiClient googleApiClient;
    private GoogleMap map;
    private LocationRequest mLocationRequest;

    // Navigation Drawer Activity and Fragments
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    // NavigationView
    private NavigationView navigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialization of Google API Client and Location Services
        createGoogleAPI();
        isLocationServicesEnabled();

        // Initialize Geofence API
        mGeofenceList = new ArrayList<>();
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        // Checks network and location settings.
        if (isNetworkAvailable(getApplicationContext())) {
            Log.d(TAG, "onCreate :: isNetworkAvailable: Connection Successful!");
            startLocationUpdates();
            new FetchGeofencesFromDatabase().execute();
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        } else {
            showConnectionErrorDialogBox();
            return;
        }


        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Initialize "Hamburger" menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        // Allow Navigation Drawer to listen to clicks
        navigationView = (NavigationView) findViewById(R.id.appNavigationView);
        drawerItemListener(navigationView);

    }

    private class FetchGeofencesFromDatabase extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            loadGeofencesFromDatabase();
            return null;
        }
    }

    public void onResume(){

        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.geofence_map_action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Performs a check of system settings if certain services are unavailable or disabled.
     *
     * @connectionErrorDialogBox - Alert Dialog box to check if Network is Available.
     * @isNetworkAvailable - Checks if connection to Google Maps Services is active.
     * @isLocationServicesEnabled - Performs a system check if location services is enabled.
     * Otherwise prompt showLocationSettingsErrorDialogBox to correct the issue.
     * @showLocationSettingsErrorDialogBox - Alert Dialog box to check if location services is
     * enabled.
     *
     */

    public void showConnectionErrorDialogBox() {

        Intent showNoActiveNetworkUI = new Intent(this, NoActiveNetworkUI.class);
        startActivity(showNoActiveNetworkUI);
        this.onStop();

    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.isConnected())
                    return true;
            }
        }
        return false;
    }

    public void isLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showLocationSettingsError();
        }
    }

    private boolean isLocationServicesAvailable;

    public void showLocationSettingsError() {

        /* Intent noActiveNetworkUI = new Intent(this, NoActiveNetworkUI.class);
        startActivity(noActiveNetworkUI); */

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.common_error_location_services_disabled);
        builder.setMessage(R.string.common_error_location_services_message);
        builder.setPositiveButton(R.string.common_location_services, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                System.exit(0);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        TextView textview_coordinates = (TextView) findViewById(R.id.textview_map_coordinates);
        textview_coordinates.setText("Location Services disabled.");
    }

    /**
     * This section controls the Navigation Drawer Activity
     * @onOptionsItemsSelected - Enables the Hamburger icon to Open/Close drawer.
     * @drawerItemListener - Listens to clicks on Navigation Drawer.
     * @selectedDrawerItem - If a user clicks on an item, the app will transition to another
     * fragment. NOTE, it is important to call fragments here.
     */

    // Default zoom value
    private float googleMapsZoomLevel = 16.0f;

    // Default value
    private boolean IsGoogleMapSetBuildingsEnabled = false;

    // Light and Dark mode
    private boolean IsLightOrDarkModeEnabled = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (mToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "NAVIGATION DRAWER IS CLICKED (OPENED)");
            return false;
        }

        /* if (id == R.id.button_map_filter) {
            Toast.makeText(getApplicationContext(), "Feature has been temporarily disabled.", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.dialog_filter_map, null);
            builder.setView(mView);
            final AlertDialog dialog = builder.create();
            Button button_filterMapFilter = (Button)mView.findViewById(R.id.button_filterMapFilter);
            Button button_filterMapCancel = (Button)mView.findViewById(R.id.button_filterMapCancel);

            button_filterMapFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            button_filterMapCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();
            return true;
        } */

        if (id == R.id.zoom_level_city){
            googleMapsZoomLevel = 14.0f;
            return true;
        }

        if (id == R.id.zoom_level_street){
            googleMapsZoomLevel = 16.0f;
            return true;
        }

        if (id == R.id.zoom_level_building){
            googleMapsZoomLevel = 20.0f;
            return true;
        }

        if (id == R.id.enable3DMap){
            if (IsGoogleMapSetBuildingsEnabled == false){
                Toast.makeText(getApplicationContext(), R.string.main_activity_set_building_enabled, Toast.LENGTH_SHORT).show();
                map.setBuildingsEnabled(true);
                IsGoogleMapSetBuildingsEnabled = true;
                return true;
            }

            if (IsGoogleMapSetBuildingsEnabled == true){

                Toast.makeText(getApplicationContext(), R.string.main_activity_set_building_disabled, Toast.LENGTH_SHORT).show();
                map.setBuildingsEnabled(false);
                IsGoogleMapSetBuildingsEnabled = false;
                return true;
            }

            return true;
        }

        if (id == R.id.enableLightDarkMode){
            if (IsLightOrDarkModeEnabled == false){
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.google_maps_dark_mode));
                IsLightOrDarkModeEnabled = true;
                return true;
            }

            if (IsLightOrDarkModeEnabled == true){
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.google_maps_light_mode));
                IsLightOrDarkModeEnabled = false;
                return true;
            }
        }

        return super.onOptionsItemSelected(item);

    }

    private void drawerItemListener(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectedDrawerItem(item);
                return false;
            }
        });
    }

    private boolean isChecked = false;
    private boolean isGeofenceLoaded = true;

    private ProgressDialog progressDialogLogOut;

    public boolean selectedDrawerItem(MenuItem menuItem) {

        int id = menuItem.getItemId();

        // DEVELOPER OPTIONS
        if (id == R.id.nav_debug_show_gps_coordinates){
            Log.d(TAG, "DEBUG: DEBUG_BUTTON_GET_LOCATION_COORDINATES");
            String test_me = Double.toString(getCurrentLocationCoordinates.getLongitude())
                    + Double.toString(getCurrentLocationCoordinates.getLatitude());
            Toast.makeText(getApplicationContext(), test_me, Toast.LENGTH_LONG).show();
        }

        if (id == R.id.nav_debug_googlemapsindoor) {
            Log.d(TAG, "DEBUG: Sending you to Tokyo Sky Tree for testing. Latitude = 35.710067 Longitude = 139.8085117");
            LatLng latlng_debug_tokyo_skytree = new LatLng(35.710067, 139.8085117);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng_debug_tokyo_skytree, 17.0f));
            map.addMarker(new MarkerOptions().position(latlng_debug_tokyo_skytree).title("Marker in Tokyo"));
            Toast.makeText(getApplicationContext(), "Sending you to Tokyo Sky Tree for testing.", Toast.LENGTH_LONG).show();

        }

        if (id == R.id.nav_debug_toggle_googleAPI) {

            if (isChecked == true) {
                isChecked = false;
                googleApiClient.connect();
                Toast.makeText(getApplicationContext(), "GoogleAPIClient connected", Toast.LENGTH_SHORT).show();

            } else if (isChecked == false) {
                isChecked = true;
                googleApiClient.disconnect();
                Toast.makeText(getApplicationContext(), "GoogleAPIClient disconnected", Toast.LENGTH_SHORT).show();
            }

        }

        if (id == R.id.nav_debug_add_tempGeofence){

            if (isGeofenceLoaded == true){
                Toast.makeText(getApplicationContext(), "Geofences are already loaded.", Toast.LENGTH_SHORT).show();
            }
            else if (isGeofenceLoaded == false){
                Toast.makeText(getApplicationContext(), "Geofences loaded", Toast.LENGTH_SHORT).show();
                loadGeofencesFromDatabase();
            }
        }

        if (id == R.id.nav_debug_clear_geofences){
            if (isGeofenceLoaded == true){
                Toast.makeText(getApplicationContext(), "Geofences unloaded", Toast.LENGTH_SHORT).show();
                removeGeofences();
                isGeofenceLoaded = false;
            } else if (isGeofenceLoaded == false){
                Toast.makeText(getApplicationContext(), "There are no Geofences to unload.", Toast.LENGTH_SHORT).show();
            }

        }

        if (id == R.id.nav_about_third_party_software){
            Intent aboutThirdPartySoftwareIntent = new Intent(getApplicationContext(), AboutGeofenceWebView.class);
            startActivity(aboutThirdPartySoftwareIntent);
        }

        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        Log.d(TAG, "Navigation Drawer is opened");

        return super.onOptionsItemSelected(menuItem);

    }



    /**
     * Contains location functionality.
     */

    // Value: 1000 = 1 second
    private long UPDATE_INTERVAL = 10000;
    private long FASTEST_INTERVAL = 5000;

    protected void startLocationUpdates() {

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
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }else {
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            onLocationChanged(googleApiClient, locationResult.getLastLocation());
                            getLastLocation();
                        }
                    },
                    Looper.myLooper());
        }

    }

    public void getLastLocation() {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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

    private Location getCurrentLocationCoordinates;
    private String strGPScoordinates;

    public void onLocationChanged(GoogleApiClient googleApiClient, Location location) {

        // Do something if new location found
        TextView coordinatesTextView = (TextView) findViewById(R.id.textview_map_coordinates);

        String gpsCoordinates = "LAT: " +
                Double.toString(location.getLatitude()) + " LNG: " +
                Double.toString(location.getLongitude());
        Log.d(TAG, gpsCoordinates);
        coordinatesTextView.setText(gpsCoordinates);
        strGPScoordinates = coordinatesTextView.getText().toString();

        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, googleMapsZoomLevel));
        getCurrentLocationCoordinates = location;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details
                return;
            }
            else {

            }
            map = googleMap;
            googleMap.setMyLocationEnabled(true);
            googleMap.setBuildingsEnabled(false);
            map.setOnMarkerClickListener(this);

        }
    }

    private boolean checkPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    public static final int REQUEST_FINE_LOCATION = 99;

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION);
    }

    private void createGoogleAPI() {
        Log.d(TAG, "createGoogleAPI: Google API Client created.");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            Log.d(TAG, "Starting Google API Client.");
            googleApiClient.connect();
        } else {
            Log.d(TAG, "Google API Client error");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "Stopping Google API Client.");
        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google API Client is connected.");
        getLastLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google API Client is suspended.");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google API Client failed to connect.");
        Toast.makeText(this, "Google API Client failed to connect", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            drawDatabaseGeofence();
        } else {
            // inform about fail
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: Marker clicked " + marker.getPosition().latitude);
        
        float[] result = new float[1];

        Double getMarkerLatitude = marker.getPosition().latitude;
        Double getMarkerLongitude = marker.getPosition().longitude;

        Location.distanceBetween(getCurrentLocationCoordinates.getLatitude(), getCurrentLocationCoordinates.getLongitude(),
                getMarkerLatitude, getMarkerLongitude, result);

        Log.d(TAG, "onMarkerClick: " + result);

        final float distanceInKilometer = result[0] / 1000;
        final DecimalFormat decimalFormat = new DecimalFormat("0.00");
        final String distance = decimalFormat.format(distanceInKilometer) + " m";
        //String distance = Float.toString(distanceInKilometer);
        Log.d(TAG, "onMarkerClick: " + distanceInKilometer);
        Log.d(TAG, "onMarkerClick: " + decimalFormat);

        final String sendLatitude = String.valueOf(getMarkerLatitude);
        final String sendLongitude = String.valueOf(getMarkerLongitude);

        final Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), distance, Snackbar.LENGTH_LONG);

        return false;
    }


    /**
     *  GEOFENCE API
     *
     *  The Geofence API used in this app is using Google's set of APIs.
     *  https://developers.google.com/location-context/geofencing/
     *
     *  Reference
     *  https://developer.android.com/training/location/geofencing.html
     *
     *  Sample code from Google
     *  https://github.com/googlesamples/android-play-location/tree/master/
     *
     */

    private boolean isClientLoadingFromDatabase = false;

    /** ArrayList for Geofences **/
    private static ArrayList<Geofence> mGeofenceList;

    /** Initialize Geofence API **/
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;

    private void loadGeofencesFromDatabase() {

        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("SM Mall of Asia")

                // Set the circular region of this geofence.
                .setCircularRegion(14.5343913, 120.9818831, 100.0f)

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build());

        addGeofences();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        Log.d(TAG, "GEOFENCE_MODULE: Calling GeofencingRequest");
        return builder.build();

    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */

    @SuppressWarnings("MissingPermission")
    private void addGeofences() {

        if (!checkPermissions()) {
            return;
        }
        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "GEOFENCE_MODULE: Google API client not connected");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Log.d(TAG, "GEOFENCE_MODULE: Geofence added successfully");
                        //LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(), getGeofencePendingIntent());
                        drawDatabaseGeofence();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        // ...
                        Log.d(TAG, "GEOFENCE_MODULE: Failed to add Geofence");
                        //Toast.makeText(getApplicationContext(),"GeoFence add fail",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void removeGeofences() {
        if (!checkPermissions()) {
            return;
        }
        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "GEOFENCE_MODULE: Google API client not connected");
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
        map.clear();
        Log.d(TAG, "GEOFENCE_MODULE: Geofence and map objects unloaded.");
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
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void drawDatabaseGeofence() {
        Log.d(TAG, "GEOFENCE: Drawing Geofence on Map");

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(14.5343913, 120.9818831))
                .fillColor( Color.argb(100, 150,150,150) )
                .strokeColor(Color.RED)
                .radius(100)
                .strokeWidth(5);

        map.addCircle(circleOptions);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(14.5343913, 120.9818831))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        map.addMarker(markerOptions);

    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {

    }

}
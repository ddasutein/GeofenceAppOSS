package dev.dasutein.geofenceapp.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.dasutein.geofenceapp.R;
import dev.dasutein.geofenceapp.api.GeofenceManager;
import dev.dasutein.geofenceapp.api.GoogleApiClientHelper;
import dev.dasutein.geofenceapp.api.LocationServicesHelper;
import dev.dasutein.geofenceapp.ui.AboutGeofenceWebView;
import dev.dasutein.geofenceapp.ui.NoNetworkDialogUI;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnMarkerClickListener {

    /** DEBUGGING **/
    private static final String TAG = "MainActivity";

    /** NAVIGATION VIEW FUNCTIONS **/
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigationView;

    /** API AND HELPERS **/
    private GeofenceManager geofenceManager = new GeofenceManager(this);
    private GoogleApiClientHelper googleApiClientHelper = new GoogleApiClientHelper(this);
    private LocationServicesHelper locationServicesHelper = new LocationServicesHelper(this);

    /** GOOGLE PLAY SERVICES **/
    public static GoogleMap map;

    /** SHARED PREFENCES FOR APPLICATION **/
    private SharedPreferences lightAndDarkModePreference;
    private static String pref_isUsingLightorDarkMode = "isUsingLightorDarkMode";

    /** BOOLEANS **/
    private boolean isUsingLightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** INITIALIZE SHARED PERFERENCES **/
        lightAndDarkModePreference = getSharedPreferences(pref_isUsingLightorDarkMode, Context.MODE_PRIVATE);
        isUsingLightMode = lightAndDarkModePreference.getBoolean(pref_isUsingLightorDarkMode, true);
        Log.d(TAG, "isUsingLightMode: " + isUsingLightMode);

        /** TOOLBAR **/
        Toolbar toolbar = findViewById(R.id.toolbar);
        AppCompatActivity activity = this;
        activity.setSupportActionBar(toolbar);

        /** INITIALIZE NAVIGATION DRAWER **/
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        navigationView = findViewById(R.id.appNavigationView);
        drawerItemListener(navigationView);

        /** SHOW APP VERSION IN NAVIGATION DRAWER **/
        Menu menu = navigationView.getMenu();
        MenuItem nav_login = menu.findItem(R.id.nav_version_number);
        try {
            nav_login.setTitle("Version " + getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        /** CHECK NETWORK AND LOCATION SERVICE SETTINGS **/
        if (isNetworkAvailable(getApplicationContext())) {
            Log.d(TAG, "onCreate :: isNetworkAvailable: Connection Successful!");

            new InitializeGoogleApiClient().execute();
            locationServicesHelper.InitializeLocationServices();
            new InitializeGeofence().execute();

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        } else {
            showConnectionErrorDialogBox();
            return;
        }
    }

    private class InitializeGoogleApiClient extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            googleApiClientHelper.BuildGoogleApiClient();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            googleApiClientHelper.connect();
        }
    }

    private class InitializeGeofence extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            geofenceManager.InitializeGeofence();
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClientHelper.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClientHelper.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleApiClientHelper.disconnect();
        geofenceManager.StopGeofenceMonitoring();
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

        Intent showNoActiveNetworkUI = new Intent(this, NoNetworkDialogUI.class);
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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "NAVIGATION DRAWER IS CLICKED (OPENED)");

            return false;
        }

        switch(item.getItemId()){

            case R.id.zoom_level_city:
                googleMapsZoomLevel = 14.0f;
                break;

            case R.id.zoom_level_street:
                googleMapsZoomLevel = 16.0f;
                break;

            case R.id.zoom_level_building:
                googleMapsZoomLevel = 20.0f;
                break;

            case R.id.enable3DMap:
                if (!IsGoogleMapSetBuildingsEnabled){
                    Toast.makeText(getApplicationContext(), R.string.main_activity_set_building_enabled, Toast.LENGTH_SHORT).show();
                    map.setBuildingsEnabled(true);
                    IsGoogleMapSetBuildingsEnabled = true;

                } else {
                    Toast.makeText(getApplicationContext(), R.string.main_activity_set_building_disabled, Toast.LENGTH_SHORT).show();
                    map.setBuildingsEnabled(false);
                    IsGoogleMapSetBuildingsEnabled = false;
                }
                break;

            case R.id.enableLightDarkMode:

                if (!isUsingLightMode) {
                    SharedPreferences.Editor editor = lightAndDarkModePreference.edit();
                    editor.putBoolean(pref_isUsingLightorDarkMode, true);
                    editor.commit();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                } else {
                    SharedPreferences.Editor editor = lightAndDarkModePreference.edit();
                    editor.putBoolean(pref_isUsingLightorDarkMode, false);
                    editor.commit();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                break;

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
    private boolean isOpened = false;

    public boolean selectedDrawerItem(MenuItem menuItem) {

        switch (menuItem.getItemId()){

            case R.id.nav_debug_show_gps_coordinates:
                Log.d(TAG, "DEBUG: DEBUG_BUTTON_GET_LOCATION_COORDINATES");

                String locationCoordinates = locationServicesHelper._debugLocationCoordinates;

                final Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), locationCoordinates, Snackbar.LENGTH_LONG);
                snackbar.show();
                break;

            case R.id.nav_debug_googlemapsindoor:
                Log.d(TAG, "DEBUG: Sending you to Tokyo Sky Tree for testing. Latitude = 35.710067 Longitude = 139.8085117");
                LatLng latlng_debug_tokyo_skytree = new LatLng(35.710067, 139.8085117);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng_debug_tokyo_skytree, 17.0f));
                map.addMarker(new MarkerOptions().position(latlng_debug_tokyo_skytree).title("Marker in Tokyo"));
                Toast.makeText(getApplicationContext(), "Sending you to Tokyo Sky Tree for testing.", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_debug_toggle_googleAPI:

                if (isChecked == true) {
                    isChecked = false;
                    googleApiClientHelper.connect();
                    Toast.makeText(getApplicationContext(), "GoogleAPIClient connected", Toast.LENGTH_SHORT).show();

                } else if (isChecked == false) {
                    isChecked = true;
                    googleApiClientHelper.disconnect();
                    Toast.makeText(getApplicationContext(), "GoogleAPIClient disconnected", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.nav_debug_add_tempGeofence:
                geofenceManager.InitializeGeofence();
                break;

            case R.id.nav_debug_clear_geofences:
                geofenceManager.StopGeofenceMonitoring();
                break;

            case R.id.nav_copy_sample_geofence_coordinates:
                String sampleGeofenceCoordinates = geofenceManager.latLng.latitude + ", " + geofenceManager.latLng.longitude;
                ClipboardManager clipboard = (ClipboardManager)getApplicationContext().getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(null,sampleGeofenceCoordinates);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_about_third_party_software:
                Intent aboutThirdPartySoftwareIntent = new Intent(getApplicationContext(), AboutGeofenceWebView.class);
                startActivity(aboutThirdPartySoftwareIntent);
                break;
        }

        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        Log.d(TAG, "Navigation Drawer is opened");

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            else {

            }
            map = googleMap;
            googleMap.setMyLocationEnabled(true);
            googleMap.setBuildingsEnabled(false);
            map.setOnMarkerClickListener(this);

        }

        if (!isUsingLightMode) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.google_maps_dark_mode));

        } else {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.google_maps_light_mode));

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

    @Override
    public void onClick(View view) {
        // Do something for marker click
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: Marker clicked " + marker.getPosition().latitude);
        
        float[] result = new float[1];

        Double getMarkerLatitude = marker.getPosition().latitude;
        Double getMarkerLongitude = marker.getPosition().longitude;

//        Location.distanceBetween(getCurrentLocationCoordinates.getLatitude(), getCurrentLocationCoordinates.getLongitude(),
//                getMarkerLatitude, getMarkerLongitude, result);

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

}
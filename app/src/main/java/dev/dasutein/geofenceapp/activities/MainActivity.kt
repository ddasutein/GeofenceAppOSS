package dev.dasutein.geofenceapp.activities

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.example.dasutein.geofenceapp.R
import dev.dasutein.geofenceapp.api.GeofenceManager
import dev.dasutein.geofenceapp.api.GoogleApiClientHelper
import dev.dasutein.geofenceapp.api.LocationServicesHelper
import dev.dasutein.geofenceapp.ui.AboutGeofenceWebView
import dev.dasutein.geofenceapp.ui.NoNetworkDialogUI

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    /** NAVIGATION VIEW FUNCTIONS  */
    private var mDrawerLayout: DrawerLayout? = null
    private var mToggle: ActionBarDrawerToggle? = null
    private var navigationView: NavigationView? = null

    /** API AND HELPERS  */
    private val geofenceManager = GeofenceManager(this)
    private val googleApiClientHelper = GoogleApiClientHelper(this)
    private val locationServicesHelper = LocationServicesHelper(this)

    /** SHARED PREFENCES FOR APPLICATION  */
    private var lightAndDarkModePreference: SharedPreferences? = null

    /** BOOLEANS  */
    private var isUsingLightMode: Boolean = false


    /**
     * This section controls the Navigation Drawer Activity
     * @onOptionsItemsSelected - Enables the Hamburger icon to Open/Close drawer.
     * @drawerItemListener - Listens to clicks on Navigation Drawer.
     * @selectedDrawerItem - If a user clicks on an item, the app will transition to another
     * fragment. NOTE, it is important to call fragments here.
     */

    // Default zoom value
    private var googleMapsZoomLevel = 16.0f

    // Default value
    private var IsGoogleMapSetBuildingsEnabled = false

    private var isChecked = false
    private val isOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** INITIALIZE SHARED PERFERENCES  */
        lightAndDarkModePreference = getSharedPreferences(pref_isUsingLightorDarkMode, Context.MODE_PRIVATE)
        isUsingLightMode = lightAndDarkModePreference!!.getBoolean(pref_isUsingLightorDarkMode, true)
        Log.d(TAG, "isUsingLightMode: $isUsingLightMode")

        /** TOOLBAR  */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val activity = this
        activity.setSupportActionBar(toolbar)

        /** INITIALIZE NAVIGATION DRAWER  */
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mToggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close)
        mDrawerLayout!!.addDrawerListener(mToggle!!)
        mToggle!!.syncState()
        navigationView = findViewById(R.id.appNavigationView)
        drawerItemListener(navigationView!!)

        /** SHOW APP VERSION IN NAVIGATION DRAWER  */
        val menu = navigationView!!.menu
        val nav_login = menu.findItem(R.id.nav_version_number)
        try {
            nav_login.title = "Version " + applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        /** CHECK NETWORK AND LOCATION SERVICE SETTINGS  */
        if (isNetworkAvailable(applicationContext)) {
            Log.d(TAG, "onCreate :: isNetworkAvailable: Connection Successful!")

            InitializeGoogleApiClient().execute()
            locationServicesHelper.InitializeLocationServices()
            InitializeGeofence().execute()

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment!!.getMapAsync(this)

        } else {
            showConnectionErrorDialogBox()
            return
        }
    }

    private inner class InitializeGoogleApiClient : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            googleApiClientHelper.BuildGoogleApiClient()
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            googleApiClientHelper.connect()
        }
    }

    private inner class InitializeGeofence : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            geofenceManager.InitializeGeofence()
            return null
        }
    }

    override fun onStart() {
        super.onStart()
        //googleApiClientHelper.connect()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        googleApiClientHelper.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        googleApiClientHelper.disconnect()
        geofenceManager.StopGeofenceMonitoring()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.geofence_map_action_menu, menu)
        return super.onCreateOptionsMenu(menu)
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
     */

    fun showConnectionErrorDialogBox() {

        val showNoActiveNetworkUI = Intent(this, NoNetworkDialogUI::class.java)
        startActivity(showNoActiveNetworkUI)
        this.onStop()

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (mToggle!!.onOptionsItemSelected(item)) {
            Log.d(TAG, "NAVIGATION DRAWER IS CLICKED (OPENED)")

            return false
        }

        when (item.itemId) {

            R.id.zoom_level_city -> googleMapsZoomLevel = 14.0f

            R.id.zoom_level_street -> googleMapsZoomLevel = 16.0f

            R.id.zoom_level_building -> googleMapsZoomLevel = 20.0f

            R.id.enable3DMap -> if (!IsGoogleMapSetBuildingsEnabled) {
                Toast.makeText(applicationContext, R.string.main_activity_set_building_enabled, Toast.LENGTH_SHORT).show()
                map.isBuildingsEnabled = true
                IsGoogleMapSetBuildingsEnabled = true

            } else {
                Toast.makeText(applicationContext, R.string.main_activity_set_building_disabled, Toast.LENGTH_SHORT).show()
                map.isBuildingsEnabled = false
                IsGoogleMapSetBuildingsEnabled = false
            }

            R.id.enableLightDarkMode ->

                if (!isUsingLightMode) {
                    val editor = lightAndDarkModePreference!!.edit()
                    editor.putBoolean(pref_isUsingLightorDarkMode, true)
                    editor.commit()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO

                } else {
                    val editor = lightAndDarkModePreference!!.edit()
                    editor.putBoolean(pref_isUsingLightorDarkMode, false)
                    editor.commit()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
                }
        }

        return super.onOptionsItemSelected(item)

    }

    private fun drawerItemListener(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { item ->
            selectedDrawerItem(item)
            false
        }
    }

    fun selectedDrawerItem(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {

            R.id.nav_debug_show_gps_coordinates -> {
                Log.d(TAG, "DEBUG: DEBUG_BUTTON_GET_LOCATION_COORDINATES")

                val locationCoordinates = locationServicesHelper._debugLocationCoordinates

                val snackbar = Snackbar.make(findViewById(R.id.drawer_layout), locationCoordinates, Snackbar.LENGTH_LONG)
                snackbar.show()
            }

            R.id.nav_debug_googlemapsindoor -> {
                Log.d(TAG, "DEBUG: Sending you to Tokyo Sky Tree for testing. Latitude = 35.710067 Longitude = 139.8085117")
                val latlng_debug_tokyo_skytree = LatLng(35.710067, 139.8085117)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng_debug_tokyo_skytree, 17.0f))
                map.addMarker(MarkerOptions().position(latlng_debug_tokyo_skytree).title("Marker in Tokyo"))
                Toast.makeText(applicationContext, "Sending you to Tokyo Sky Tree for testing.", Toast.LENGTH_LONG).show()
            }

            R.id.nav_debug_toggle_googleAPI ->

                if (isChecked == true) {
                    isChecked = false
                    googleApiClientHelper.connect()
                    Toast.makeText(applicationContext, "GoogleAPIClient connected", Toast.LENGTH_SHORT).show()

                } else if (isChecked == false) {
                    isChecked = true
                    googleApiClientHelper.disconnect()
                    Toast.makeText(applicationContext, "GoogleAPIClient disconnected", Toast.LENGTH_SHORT).show()
                }

            R.id.nav_debug_add_tempGeofence -> geofenceManager.InitializeGeofence()

            R.id.nav_debug_clear_geofences -> geofenceManager.StopGeofenceMonitoring()

            R.id.nav_copy_sample_geofence_coordinates -> {
                val sampleGeofenceCoordinates = geofenceManager.latLng.latitude.toString() + ", " + geofenceManager.latLng.longitude
                val clipboard = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(null, sampleGeofenceCoordinates)
                clipboard.primaryClip = clip
                Toast.makeText(applicationContext, "Copied to clipboard", Toast.LENGTH_LONG).show()
            }

            R.id.nav_about_third_party_software -> {
                val aboutThirdPartySoftwareIntent = Intent(applicationContext, AboutGeofenceWebView::class.java)
                startActivity(aboutThirdPartySoftwareIntent)
            }
        }

        menuItem.isChecked = true
        mDrawerLayout!!.closeDrawers()
        Log.d(TAG, "Navigation Drawer is opened")

        return super.onOptionsItemSelected(menuItem)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            } else {

            }

            googleMap.isMyLocationEnabled = true
            googleMap.isBuildingsEnabled = false
            map.setOnMarkerClickListener(this)

        }

        if (!isUsingLightMode) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.google_maps_dark_mode))

        } else {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.google_maps_light_mode))

        }
    }

    private fun checkPermissions(): Boolean {

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            requestPermissions()
            return false
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION)
    }

    override fun onClick(view: View) {
        // Do something for marker click
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "onMarkerClick: Marker clicked " + marker.position.latitude)

        val result = FloatArray(1)

        val getMarkerLatitude = marker.position.latitude
        val getMarkerLongitude = marker.position.longitude

        //        Location.distanceBetween(getCurrentLocationCoordinates.getLatitude(), getCurrentLocationCoordinates.getLongitude(),
        //                getMarkerLatitude, getMarkerLongitude, result);

        Log.d(TAG, "onMarkerClick: $result")

        val distanceInKilometer = result[0] / 1000
        val decimalFormat = DecimalFormat("0.00")
        val distance = decimalFormat.format(distanceInKilometer.toDouble()) + " m"
        //String distance = Float.toString(distanceInKilometer);
        Log.d(TAG, "onMarkerClick: $distanceInKilometer")
        Log.d(TAG, "onMarkerClick: $decimalFormat")

        val sendLatitude = getMarkerLatitude.toString()
        val sendLongitude = getMarkerLongitude.toString()

        val snackbar = Snackbar.make(findViewById(R.id.drawer_layout), distance, Snackbar.LENGTH_LONG)

        return false
    }

    companion object {

        /** DEBUGGING  */
        private val TAG = "MainActivity"

        /** GOOGLE PLAY SERVICES  */
        lateinit var map: GoogleMap
        private val pref_isUsingLightorDarkMode = "isUsingLightorDarkMode"

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (connectivityManager != null) {
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo != null) {
                    if (networkInfo.isConnected)
                        return true
                }
            }
            return false
        }

        val REQUEST_FINE_LOCATION = 99
    }

}
package dev.dasutein.geofenceapp.ui

import android.content.Intent
import android.provider.Settings
import android.os.Bundle
import android.view.View
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.example.dasutein.geofenceapp.R

/** NoNetworkDialogUI
 *
 * Method to block all user interaction if network connection is no longer active. ONLY call this method
 * if an activity requires connection to Google Services or database.
 *
 */

class NoNetworkDialogUI : AppCompatActivity() {

    private var exitButton: Button? = null
    private var retryButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_active_network_ui)

        exitButton = findViewById<View>(R.id.NoActiveNetworkExitButton) as Button
        retryButton = findViewById<View>(R.id.NoActiveNetworkRetryButton) as Button

        val bundle = intent.extras
        if (bundle != null) {
            val str = bundle.getString("nolocation")
            if (str === "location_disabled") {

                exitButton!!.setOnClickListener {
                    // Quits application
                    ActivityCompat.finishAffinity(this@NoNetworkDialogUI)
                }

                retryButton!!.text = "Open Location Services"
                retryButton!!.setOnClickListener {
                    val callLocationServicesSettings = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(callLocationServicesSettings)
                    ActivityCompat.finishAffinity(this@NoNetworkDialogUI)
                }
            }
        } else {

            exitButton!!.setOnClickListener {
                // Quits application
                ActivityCompat.finishAffinity(this@NoNetworkDialogUI)
            }

            retryButton!!.setOnClickListener {
                // Attempts to restart activity
                System.exit(0)
            }
        }


    }

    override fun onBackPressed() {
        // Quits application
        ActivityCompat.finishAffinity(this@NoNetworkDialogUI)
    }
}

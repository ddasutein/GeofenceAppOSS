package com.example.dasutein.geofenceapp.WebViewActivities;

import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.dasutein.geofenceapp.R;

/** NoActiveNetworkUI
 *
 * Method to block all user interaction if network connection is no longer active. ONLY call this method
 * if an activity requires connection to Google Services or database.
 *
 */

public class NoActiveNetworkUI extends AppCompatActivity {

    private Button exitButton;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_active_network_ui);

        exitButton = (Button) findViewById(R.id.NoActiveNetworkExitButton);
        retryButton = (Button) findViewById(R.id.NoActiveNetworkRetryButton);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            String str = bundle.getString("nolocation");
            if (str == "location_disabled"){

                exitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Quits application
                        ActivityCompat.finishAffinity(NoActiveNetworkUI.this);
                    }
                });

                retryButton.setText("Open Location Services");
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent callLocationServicesSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callLocationServicesSettings);
                        ActivityCompat.finishAffinity(NoActiveNetworkUI.this);
                    }
                });
            }
        }

        else {

            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Quits application
                    ActivityCompat.finishAffinity(NoActiveNetworkUI.this);
                }
            });

            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Attempts to restart activity
                    System.exit(0);
                }
            });
        }


    }

    @Override
    public void onBackPressed() {
        // Quits application
        ActivityCompat.finishAffinity(NoActiveNetworkUI.this);
    }
}

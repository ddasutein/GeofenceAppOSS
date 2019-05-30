package dev.dasutein.geofenceapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import dev.dasutein.geofenceapp.activities.MainActivity;
import com.example.dasutein.geofenceapp.R;

public class SplashScreenUI extends AppCompatActivity {

    // Splash screen timer
    private static int TIME_OUT = 1000;

    // Logcat
    public static final String TAG = "SplashScreenUI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        TextView textViewVersionNum = (TextView) findViewById(R.id.versionLabel);
        try {

            // Get application version number
            String getVersionNum = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
            textViewVersionNum.setText(getVersionNum);
            Log.i(TAG, "Version: " + getVersionNum);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        Log.i(TAG, "Starting mainActivityIntent");
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                // Start app main activity
                Intent mainActivityIntent = new Intent(SplashScreenUI.this, MainActivity.class);
                startActivity(mainActivityIntent);

                // Close this activity
                finish();
                Log.i(TAG, "Closing SplashScreenUI");
            }
        }, TIME_OUT);
    }
}

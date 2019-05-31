package dev.dasutein.geofenceapp.ui

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView

import dev.dasutein.geofenceapp.activities.MainActivity
import com.example.dasutein.geofenceapp.R

class SplashScreenUI : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val textViewVersionNum = findViewById(R.id.versionLabel) as TextView
        try {

            // Get application version number
            val getVersionNum = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
            textViewVersionNum.text = getVersionNum
            Log.i(TAG, "Version: $getVersionNum")

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            Log.e(TAG, e.toString())
        }

        Log.i(TAG, "Starting mainActivityIntent")
        Handler().postDelayed({
            // Start app main activity
            val mainActivityIntent = Intent(this@SplashScreenUI, MainActivity::class.java)
            startActivity(mainActivityIntent)

            // Close this activity
            finish()
            Log.i(TAG, "Closing SplashScreenUI")
        }, TIME_OUT.toLong())
    }

    companion object {

        // Splash screen timer
        private val TIME_OUT = 1000

        // Logcat
        val TAG = "SplashScreenUI"
    }
}

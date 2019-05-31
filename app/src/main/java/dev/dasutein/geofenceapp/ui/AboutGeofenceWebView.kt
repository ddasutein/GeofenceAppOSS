package dev.dasutein.geofenceapp.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient

import androidx.appcompat.app.AppCompatActivity

import com.example.dasutein.geofenceapp.R

class AboutGeofenceWebView : AppCompatActivity() {

    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_geofence_web_view)

        // Checks network and location settings.
        if (isNetworkAvailable(applicationContext)) {

            webView = findViewById(R.id.webView)
            webView!!.webViewClient = WebViewClient()
            webView!!.loadUrl("https://ddasutein.github.io/geofenceAppAbout/")

        } else {
            val showNoActiveNetworkUI = Intent(this, NoNetworkDialogUI::class.java)
            startActivity(showNoActiveNetworkUI)
            return
        }

    }

    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
            webView!!.clearCache(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_about_geofence_action_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == R.id.button_refresh_webview) {

            Log.d(TAG, "Refreshing webView")
            webView!!.loadUrl("about:blank")
            webView!!.clearCache(true)
            webView!!.loadUrl("https://ddasutein.github.io/geofenceAppAbout/")
        }


        return super.onOptionsItemSelected(item)

    }

    companion object {

        // DEBUGGING
        private val TAG = "GeofenceListActivity"

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
    }
}

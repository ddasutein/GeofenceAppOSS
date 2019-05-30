package dev.dasutein.geofenceapp.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dasutein.geofenceapp.R;

public class AboutGeofenceWebView extends AppCompatActivity {

    // DEBUGGING
    private static final String TAG = "GeofenceListActivity";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_geofence_web_view);

        // Checks network and location settings.
        if (isNetworkAvailable(getApplicationContext())) {

            webView = (WebView) findViewById(R.id.webView);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl("https://ddasutein.github.io/geofenceAppAbout/");

        } else {
            Intent showNoActiveNetworkUI = new Intent(this, NoNetworkDialogUI.class);
            startActivity(showNoActiveNetworkUI);
            return;
        }

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

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            webView.clearCache(true);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_about_geofence_action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.button_refresh_webview) {

            Log.d(TAG, "Refreshing webView");
            webView.loadUrl("about:blank");
            webView.clearCache(true);
            webView.loadUrl("https://ddasutein.github.io/geofenceAppAbout/");
        }


        return super.onOptionsItemSelected(item);

    }
}

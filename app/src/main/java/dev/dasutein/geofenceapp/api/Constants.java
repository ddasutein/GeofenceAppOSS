package dev.dasutein.geofenceapp.api;

/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Constants used in this sample.
 */



public class Constants {


    // Creating Volley RequestQueue.
    static RequestQueue requestQueue;


    // Storing server url into String variable.
    static String HttpUrl = "http://192.168.1.11/geofence/load_geofences.php";

    private static final String TAG = "Constants";
    public Constants() {
        LOADFROMDATA();
    }

    static private String geof_name = null;
    static private double geof_lat = 0;
    static private double geof_lon = 0;

    static final private String getGeofenceNameConstants = geof_name;
    static final private double getGeofenceLat = geof_lat;
    static final private double getGeofenceLong = geof_lon;

    public void LOADFROMDATA(){

        //requestQueue = Volley.newRequestQueue(Constants.this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, HttpUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for(int i = 0; i < response.length(); i++){

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        Log.d(TAG, "Constants: jsonObjected called");

                        geof_name = jsonObject.getString("geof_name");
                        geof_lat = jsonObject.getDouble("geof_lat");
                        geof_lon = jsonObject.getDouble("geof_lon");

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonArrayRequest);
    }


    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    static final float GEOFENCE_RADIUS_IN_METERS = 50.0f; // 1 mile, 1.6 km

    //static final private String getGeofenceNameConstants = "Test";

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
    static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<>();


    static {

        // San Francisco International Airport.
        //BAY_AREA_LANDMARKS.put(getGeofenceNameConstants, new LatLng(getGeofenceLat, getGeofenceLong));

        // Googleplex.
        BAY_AREA_LANDMARKS.put("Landbank", new LatLng(14.5833461,121.1254852));

        BAY_AREA_LANDMARKS.put("Volley Golf Cainta", new LatLng(14.5823429, 121.1278552));

        BAY_AREA_LANDMARKS.put("Honda Cainta", new LatLng(14.5825135,121.1272829));

        // STI Ortigas Cainta
        //BAY_AREA_LANDMARKS.put("STI", new LatLng(14.563, 121.144));
        //BAY_AREA_LANDMARKS.put("STI_2", new LatLng(14.583, 121.136));
    }
}
package dev.dasutein.geofenceapp.api

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

import android.util.Log

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.google.android.gms.maps.model.LatLng

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap

/**
 * Constants used in this sample.
 */


class Constants {
    init {
        LOADFROMDATA()
    }

    fun LOADFROMDATA() {

        //requestQueue = Volley.newRequestQueue(Constants.this);

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, HttpUrl, null, Response.Listener { response ->
            for (i in 0 until response.length()) {

                try {
                    val jsonObject = response.getJSONObject(i)
                    Log.d(TAG, "Constants: jsonObjected called")

                    geof_name = jsonObject.getString("geof_name")
                    geof_lat = jsonObject.getDouble("geof_lat")
                    geof_lon = jsonObject.getDouble("geof_lon")

                } catch (e: JSONException) {
                    e.printStackTrace()

                }

            }
        }, Response.ErrorListener { })
        requestQueue!!.add(jsonArrayRequest)
    }

    companion object {


        // Creating Volley RequestQueue.
        internal var requestQueue: RequestQueue? = null


        // Storing server url into String variable.
        internal var HttpUrl = "http://192.168.1.11/geofence/load_geofences.php"

        private val TAG = "Constants"

        private var geof_name: String? = null
        private var geof_lat = 0.0
        private var geof_lon = 0.0

        private val getGeofenceNameConstants = geof_name
        private val getGeofenceLat = geof_lat
        private val getGeofenceLong = geof_lon


        private val PACKAGE_NAME = "com.google.android.gms.location.Geofence"

        internal val GEOFENCES_ADDED_KEY = "$PACKAGE_NAME.GEOFENCES_ADDED_KEY"

        /**
         * Used to set an expiration time for a geofence. After this amount of time Location Services
         * stops tracking the geofence.
         */
        val GEOFENCE_EXPIRATION_IN_HOURS: Long = 12

        /**
         * For this sample, geofences expire after twelve hours.
         */
        val GEOFENCE_EXPIRATION_IN_MILLISECONDS =
                GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000
        internal val GEOFENCE_RADIUS_IN_METERS = 50.0f // 1 mile, 1.6 km

        //static final private String getGeofenceNameConstants = "Test";

        /**
         * Map for storing information about airports in the San Francisco bay area.
         */
        internal val BAY_AREA_LANDMARKS = HashMap<String, LatLng>()


        init {

            // San Francisco International Airport.
            //BAY_AREA_LANDMARKS.put(getGeofenceNameConstants, new LatLng(getGeofenceLat, getGeofenceLong));

            // Googleplex.
            BAY_AREA_LANDMARKS["Landbank"] = LatLng(14.5833461, 121.1254852)

            BAY_AREA_LANDMARKS["Volley Golf Cainta"] = LatLng(14.5823429, 121.1278552)

            BAY_AREA_LANDMARKS["Honda Cainta"] = LatLng(14.5825135, 121.1272829)

            // STI Ortigas Cainta
            //BAY_AREA_LANDMARKS.put("STI", new LatLng(14.563, 121.144));
            //BAY_AREA_LANDMARKS.put("STI_2", new LatLng(14.583, 121.136));
        }
    }
}
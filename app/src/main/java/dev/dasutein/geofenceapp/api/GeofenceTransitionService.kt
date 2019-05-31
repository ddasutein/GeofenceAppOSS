package dev.dasutein.geofenceapp.api

import android.app.Notification
import android.app.NotificationManager
import android.app.*
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log

import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat

import com.android.volley.RequestQueue
import com.example.dasutein.geofenceapp.R
import dev.dasutein.geofenceapp.activities.MainActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

import java.util.ArrayList

import android.app.Notification.DEFAULT_VIBRATE

/**
 * Created by Dasutein on 2/5/2018.
 */

class GeofenceTransitionService : JobIntentService() {

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */

    internal var triggeringGeofencesIdsString: String = ""
    private val customNotificationTest: String? = null


    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */


    override fun onHandleWork(intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        } else {
            // Get the transition type.
            val geofenceTransition = geofencingEvent.geofenceTransition

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG, "Geofence: User has enter the Geofence")
                enableMapMarkerClick = true

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                val triggeringGeofences = geofencingEvent.triggeringGeofences

                // Get the transition details as a String.
                val geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                        triggeringGeofences)

                // Send notification and log the transition details.
                sendNotification(geofenceTransitionDetails)
                Log.i(TAG, geofenceTransitionDetails)
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                enableMapMarkerClick = false

                // Sends a broadcast to GeofenceContentActivity.java to close activity upon exit
                val closeActivityIntent = Intent("close_activity")
                sendBroadcast(closeActivityIntent)

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                val triggeringGeofences = geofencingEvent.triggeringGeofences

                // Get the transition details as a String.
                val geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                        triggeringGeofences)

                // Send notification and log the transition details.
                sendNotificationRECEIVE(geofenceTransitionDetails)
                Log.i(TAG, geofenceTransitionDetails)
            } else {
                // Log the error.
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition))
            }

        }


    }

    private fun getGeofenceTransitionDetails(
            geofenceTransition: Int,
            triggeringGeofences: List<Geofence>): String {

        val geofenceTransitionString = getTransitionString(geofenceTransition)

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)

        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    private fun sendNotification(notificationDetails: String) {
        // Get an instance of the Notification manager
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)
        }

        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)

        // Construct a task stack.
        val stackBuilder = TaskStackBuilder.create(this)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = NotificationCompat.Builder(this)

        // Custom Notification Sound
        val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.notification_default)


        // Define the notification settings.
        builder.setSmallIcon(R.drawable.geofence_icon)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(resources,
                        R.drawable.ic_menu_share))
                .setColor(Color.BLACK)
                .setContentTitle("You have entered $triggeringGeofencesIdsString")
                //.setContentText(customNotificationTest)
                .setContentIntent(notificationPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(DEFAULT_VIBRATE)
                .setSound(sound)

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID) // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }

    private fun sendNotificationRECEIVE(notificationDetails: String) {
        // Get an instance of the Notification manager
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)
        }

        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)

        // Construct a task stack.
        val stackBuilder = TaskStackBuilder.create(this)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = NotificationCompat.Builder(this)

        // Custom Notification Sound
        val sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.geofence_exit)

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.geofence_icon)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(resources,
                        R.drawable.ic_menu_share))
                .setColor(Color.BLACK)
                .setContentTitle("Thank you for visiting $triggeringGeofencesIdsString")
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(DEFAULT_VIBRATE)
                .setSound(sound)

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID) // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }


    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */

    private fun getTransitionString(transitionType: Int): String {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d(TAG, "TRANSITION TEST: GEOFENCE ENTERED")
                return getString(R.string.geofence_transition_entered)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d(TAG, "TRANSITION TEST:GEOFENCE EXIT")
                return getString(R.string.geofence_transition_exited)
            }
            else -> return getString(R.string.unknown_geofence_transition)
        }
    }

    companion object {

        private val JOB_ID = 573

        private val TAG = "GeofenceTransitionSRVC"

        private val CHANNEL_ID = "channel_01"

        var enableMapMarkerClick = false

        /*public GeofenceTransitionService() {
        super("GeofenceTransitionService");
    } */

        fun enqueueWork(context: Context, intent: Intent) {
            JobIntentService.enqueueWork(context, GeofenceTransitionService::class.java!!, JOB_ID, intent)
        }

        /**
         * Posts a notification in the notification bar when a transition is detected.
         * If the user clicks the notification, control goes to the MainActivity.
         */

        // Android Volley
        private val requestQueue: RequestQueue? = null
    }
}

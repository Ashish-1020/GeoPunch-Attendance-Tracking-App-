package com.example.geopunch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "GeofenceReceiver"
        const val CHANNEL_ID = "GeofenceChannel"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent!!.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent!!.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent!!.geofenceTransition

        // Get the triggered geofences
        val triggeringGeofences = geofencingEvent!!.triggeringGeofences

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                context!!,
                geofenceTransition,
                triggeringGeofences!!
            )

            // Send notification and log the transition details.
            sendNotification(context, geofenceTransitionDetails)
            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(TAG, "Invalid geofence transition type: $geofenceTransition")
        }
    }

    private fun getGeofenceTransitionDetails(
        context: Context,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entering"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exiting"
            else -> "Unknown Transition"
        }

        val triggeringGeofencesIdsList = triggeringGeofences.map { it.requestId }
        val triggeringGeofencesIdsString = triggeringGeofencesIdsList.joinToString(", ")

        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    private fun sendNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Geofence Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val notificationPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_bell)
            .setContentTitle("Geofence Transition")
            .setContentText(message)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}

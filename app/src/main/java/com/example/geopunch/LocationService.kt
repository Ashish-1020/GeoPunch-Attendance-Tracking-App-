package com.example.geopunch


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.geopunch.admin.OfficeLocation
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Define the target location
    private val targetLatLng = LatLng(26.264578, 81.5047300) // Example: New Delhi coordinates
    private val radius = 20.0 // Radius in meters

    private var userId:String?=null

    private var isInside:Boolean?=null
    private var status:String="Not Working"
    private var currentEntryDocId: String? = null
    private  val locations = listOf(
        OfficeLocation("Head Office", LatLng(26.2650652, 81.5066691),50.0),
        OfficeLocation("Hostel", LatLng(26.264578, 81.5047300),30.0),
        OfficeLocation("Academic Block", LatLng(26.2651623, 81.509465),100.0)
    )


    override fun onCreate() {
        super.onCreate()

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        createNotificationChannel()
        startForegroundService()





        // Set up location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    checkDistanceFromTarget(location.latitude, location.longitude)
                }
            }
        }
        /*
        onCreate(): Called when the service is first created,setting up initial configurations.

fusedLocationClient: Initializes the location client using LocationServices.getFusedLocationProviderClient(this).

createNotificationChannel(): Sets up the notification channel (required for Android O and later).

startForegroundService(): Starts the service in the foreground with a persistent notification.

locationCallback: Defines the callback for handling location updates. onLocationResult is called when the location is updated.

startLocationUpdates(): Requests location updates from the location client.
         */

        startLocationUpdates()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "location_service_channel"
            val channelName = "Location Service"
            val channelDescription = "Channel for location service notifications."
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    /*

    createNotificationChannel(): Creates a notification channel for Android 8.0 (Oreo) and higher.

channelId: Unique identifier for the notification channel.

channelName: User-visible name for the channel.

channelDescription: Description of the channel's purpose.

importance: The importance level of notifications posted to this channel (e.g., IMPORTANCE_LOW means notifications are not intrusive).
NotificationChannel: Creates and configures the notification channel.
notificationManager.createNotificationChannel(channel): Registers the channel with the system.
     */

    private fun startForegroundService() {
        val channelId = "location_service_channel"

        // Notification for foreground service to indicate tracking
        val persistentNotification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking")
            .setContentText("Your location is being tracked.")
            .setSmallIcon(R.drawable.notification_bell)
            .setOngoing(true) // Persistent notification
            .build()

        startForeground(1, persistentNotification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2_000 // 30 seconds
            fastestInterval = 1_000 // 15 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
    /*
    startLocationUpdates(): Sets up and requests location updates.
LocationRequest.create(): Creates a new LocationRequest object.
interval: How frequently the app receives location updates (30 seconds here).
fastestInterval: The fastest rate at which the app can receive location updates (15 seconds here).
priority: The accuracy of location updates (e.g., PRIORITY_HIGH_ACCURACY for GPS-based updates).
ActivityCompat.checkSelfPermission(): Checks if the app has the required location permissions.
fusedLocationClient.requestLocationUpdates(): Requests location updates using the specified LocationRequest and LocationCallback.
     */

    private fun checkDistanceFromTarget(latitude: Double, longitude: Double) {
        val currentLatLng = LatLng(latitude, longitude)

        var nameLocation: String? = null // Initialize as null

        locations.forEach { location ->
            val distance = FloatArray(1)
            Location.distanceBetween(
                latitude, longitude,
                location.latLng.latitude, location.latLng.longitude,
                distance
            )

            if (distance[0] < location.Radius) {
                sendNotification("You are inside the ${location.name} radius of ${location.Radius} meters!")
                isInside = true
                nameLocation = location.name // Update nameLocation
                uploadCheckIn(nameLocation!!) // Pass the location name

                // Broadcast the location to MainActivity
                val intent = Intent("LOCATION_UPDATE").apply {
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                    putExtra("Status", status)
                }
                sendBroadcast(intent)

                // Exit the loop after the condition is met
                return
            }
        }

        // If no location's radius condition is met, execute this code
        isInside = false
        uploadCheckIn(nameLocation ?: "Unknown") // Handle null case


        // Broadcast the location to MainActivity
        val intent = Intent("LOCATION_UPDATE").apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("Status", status)
        }

        sendBroadcast(intent)

    }



    private fun uploadCheckIn(location: String) {
        val db = FirebaseFirestore.getInstance()
        val time = Timestamp.now()

        // Converting the date to a formatted string
        val date = time.toDate()
        val timeInMillis = time.toDate().time
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        var Id: String? = null

        if (status == "Not Working" && isInside == true) {
            Id = userId + "_" + timeInMillis

            val entry = Entry(
                EntryId = Id,
                EntryTime = time,
                ExitTime = null,
                Status = "Working",
                Date = formattedDate,
                latLng = LatLng(12.25121212, 66.6556121), // Replace with the actual location
                OfficeLocation = location,
                mode ="Automatic"
            )

            // Add entry document for check-in
            db.collection("Employees Data")
                .document(userId.toString())
                .collection("Entries Data")
                .document(Id)
                .set(entry)
                .addOnSuccessListener {
                    currentEntryDocId = Id // Store the document ID
                    Toast.makeText(this, "Uploaded check-in time", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error during check-in: ${e.message}", Toast.LENGTH_LONG).show()
                }

            status = "Working at $location" // Correct the status format
            Toast.makeText(this, "Status changed to $status", Toast.LENGTH_SHORT).show()

        } else if (status.startsWith("Working at") && isInside == false) {
            Toast.makeText(this, "Checkout called", Toast.LENGTH_SHORT).show()
            currentEntryDocId?.let { docId ->
                db.collection("Employees Data")
                    .document(userId.toString())
                    .collection("Entries Data")
                    .document(docId) // Use the correct document ID
                    .update(
                        "exitTime", FieldValue.serverTimestamp(), // Ensure ExitTime field name matches Firestore
                        "status", "Complete"
                    )
                    .addOnSuccessListener {
                        Toast.makeText(this, "Uploaded checkout time", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error updating checkout: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            status = "Not Working"
            Toast.makeText(this, "Status changed to $status", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("NotificationPermission")
    private fun sendNotification(message: String) {
        val notificationId = 2
        val channelId = "location_service_channel"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Set the sound URI (default sound or custom sound)
       // val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // Default notification sound

        // If using a custom sound, place the sound file in res/raw/ and reference it here
         val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.voicemaker_checkin)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_bell)
            .setContentTitle("Location Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri) // Add sound to the notification

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        userId= intent?.getStringExtra("email")!!
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    data class Entry(
        val EntryId: String,
        val ExitTime: Timestamp?,
        val EntryTime: Timestamp,
        val Status:String,
        val Date: String,
        val latLng: LatLng,
        val OfficeLocation:String,
        val mode:String
    )
}

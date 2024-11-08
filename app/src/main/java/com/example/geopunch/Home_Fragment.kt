package com.example.geopunch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.example.geopunch.admin.OfficeLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Home_Fragment : Fragment(), OnMapReadyCallback {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var startbtn: AppCompatImageButton
    private  var btnclicked: Boolean?=null

    private lateinit var mMap: GoogleMap
    private lateinit var tvCoordinates: TextView
    private lateinit var homeTime: TextView
    private lateinit var homeDate: TextView
    private lateinit var substation: TextView

    // Declare a global variable to store the marker
    private var currentMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())

    private val targetLatLng = LatLng(26.2653975, 81.5047923)
    private val radius = 30.0

    private lateinit var locationUpdateReceiver: BroadcastReceiver
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var userNameTextView: TextView
    private lateinit var stat: TextView
    private lateinit var userName:String
    private lateinit var userId:String
    private lateinit var manual: AppCompatImageButton
    private lateinit var mapFrag : FragmentContainerView
    private lateinit var automaticLayout : ConstraintLayout
    private lateinit var manualLayout : ConstraintLayout
    private val locations = listOf(
        OfficeLocation("Head Office", LatLng(26.2650652, 81.5066691),50.0),
        OfficeLocation("Hostel", LatLng(26.264578, 81.5047300),30.0),
        OfficeLocation("Academic Block", LatLng(26.2651623, 81.509465),100.0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }



        createNotificationChannel()

//        // Initialize the permission launcher
//        requestPermissionLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted ->
//            if (isGranted ) {
//
//            } else {
//                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Request permissions if necessary
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED &&
//            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//            == PackageManager.PERMISSION_GRANTED
//        ) {
//
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//        }

        // Register receiver for location updates
        locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
                val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
                val status= intent?.getStringExtra("Status")
                val latLng = LatLng(latitude, longitude)


               stat.text=status



                if (status == "Not Working") {
                    stat.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                } else {
                    stat.setTextColor(ContextCompat.getColor(requireContext(),R.color.green))
                }
                // Update map and coordinates
                updateMapAndCoordinates(latLng)
            }
        }


            requireContext().registerReceiver(locationUpdateReceiver, IntentFilter("LOCATION_UPDATE"),
                Context.RECEIVER_EXPORTED)




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())







}

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_, container, false)


        substation=view.findViewById(R.id.sub1)
        // Initialize views
        startbtn = view.findViewById(R.id.startbtn)
        stat=view.findViewById(R.id.statusText)
        // Set up button click listener
        startbtn.setOnClickListener {
            btnclicked = !(btnclicked ?: false)

            if (btnclicked == true) {
                startbtn.setImageResource(R.drawable.stop)
                startLocationService()
            } else {
                stat.text = "Turn on Service"
                stat.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
                startbtn.setImageResource(R.drawable.start)
                stopLocationService()
            }

            // Check location permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                if(btnclicked== true){
                    startLocationService()}
                else{
                    stopLocationService()
                }
            } else {
//                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        // Initialize the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        userNameTextView = view.findViewById(R.id.UserNametxt)
        // Fetch and display user data
        fetchUserData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manual = view.findViewById(R.id.manual_but)
        mapFrag = view.findViewById(R.id.map)
        automaticLayout= view.findViewById(R.id.automatic_layout)
        manualLayout=view.findViewById(R.id.searchContainer)


        //manual button
//        manual.setOnClickListener{
//            if (isManual) {
//                val params = manuallayout.layoutParams
//
//                if (layoutHeight != null) {
//                    params.height = (layoutHeight/2.5).toInt()
//                }   // Set the height
//                manuallayout.layoutParams = params
//                isManual = false
//
//
//
//            }else{
//                val params = manuallayout.layoutParams
//                params.height = 1// Set the height
//                manuallayout.layoutParams = params
//                isManual = true
//
//            }
//        }





        //Start the time updater
        startUpdatingTime()

        //initializing home time and date
        homeTime = view.findViewById(R.id.homeTime)
        homeDate = view.findViewById(R.id.homeDate)

        //for gps time
//     locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            requestLocationUpdates()
//        } else {
//            // Request the location permission
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                LOCATION_PERMISSION_REQUEST
//            )
//        }

        //slider
        val slider = view.findViewById<SlideToActView>(R.id.sliderbut)

        slider.isReversed = false

        //initially disabled
        slider.isEnabled = false
        slider.outerColor = Color.LTGRAY

        substation.setOnClickListener {
            substation.setTextColor(  ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))

        }

        //enabling the slider
        manual.setOnClickListener(){
            automaticLayout.visibility=View.GONE
            manualLayout.visibility=View.VISIBLE
            slider.visibility=View.VISIBLE
            slider.isEnabled = !slider.isEnabled
            var map:GoogleMap
            map=mMap
            if(slider.isEnabled) {
                slider.outerColor = Color.WHITE

                mMap.clear()
                mMap.addMarker(
                    MarkerOptions()
                        .position(targetLatLng)
                        .title("Substation-1" )
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                )

                mMap.addCircle(
                    CircleOptions()
                        .center(targetLatLng)
                        .radius(radius)
                        .strokeColor(0xFF00FF00.toInt()) // Green stroke color
                        .fillColor(0x2200FF00.toInt())   // Transparent green fill color
                        .strokeWidth(2f)
                )

            } else {
                manualLayout.visibility=View.GONE
                automaticLayout.visibility=View.VISIBLE
                slider.visibility=View.GONE
                mMap.clear()
                onMapReady(map)
            }
        }



        //suggested location provider
        var longitude: Double = 0.0
        var latitude: Double = 0.0





        slider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener{
            override fun onSlideComplete(view: SlideToActView) {

                if(slider.isReversed) {
                    //slided back
                    Toast.makeText(context, "Manual Chekout Recorded", Toast.LENGTH_SHORT).show()
                    slider.outerColor = context?.let { ContextCompat.getColor(it, R.color.white) }!!
                    slider.innerColor = context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!
                    slider.text = "       Check in >>"
                    slider.textColor = context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!
                    slider.iconColor = context?.let { ContextCompat.getColor(it, R.color.white) }!!
                }else{
                    //slided forward
                    Toast.makeText(context, "Manual ChekIn Recorded", Toast.LENGTH_SHORT).show()
                    slider.innerColor = context?.let { ContextCompat.getColor(it, R.color.white) }!!
                    slider.outerColor = context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!
                    slider.text = " << Check out      "
                    slider.textColor = context?.let { ContextCompat.getColor(it, R.color.white) }!!
                    slider.iconColor = context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!
                }
            }
        }
    }

    private fun startUpdatingTime() {
        handler.post(object : Runnable {
            override fun run() {
                // Get current system time
                val currentTime = System.currentTimeMillis()
                val timeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
                val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
                val formattedTime = timeFormat.format(Date(currentTime))
                val formattedDate = dateFormat.format(Date(currentTime))

                // Update the TextView with the current time
                homeTime.text = formattedTime
                homeDate.text = formattedDate

                // Repeat this runnable code block every 1 second (1000 milliseconds)
                handler.postDelayed(this, 1000)
            }
        })


    }

    @SuppressLint("MissingPermission")
    private fun checkMockLocation() {
        // Request the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                if (isMockLocation(location)) {
                    // Mock location is enabled
                    stat.text = "Turn on Service"
                    stat.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
                    startbtn.setImageResource(R.drawable.start)
                    stopLocationService()
                    Toast.makeText(requireContext(), "Fake location detected!", Toast.LENGTH_LONG).show()
                    showFakeDialog()
//                    openDeveloperOptions()
                } else {
                    // Location is not mocked
                   // Toast.makeText(requireContext(), "Location is not Faked", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isMockLocation(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            location.isFromMockProvider
        } else
            false


    }

    private fun openDeveloperOptions() {
        // Open developer options settings
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where developer options are not available
            Toast.makeText(requireContext(), "Developer options not found", Toast.LENGTH_SHORT).show()
        }
    }





    private fun startLocationService() {

        Toast.makeText(context, "Location service started", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(),
            LocationService::class.java)
        intent.putExtra("email",userId)
        requireContext().startService(intent)


    }

    private fun stopLocationService() {
        // This method should stop your location service
       // Toast.makeText(context, "Location service stopped", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(),
            LocationService::class.java)

        requireContext().stopService(intent)
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "GeoPunch Channel"
            val descriptionText = "Channel for GeoPunch notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("GeoPunchChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // Add your circle and marker here

        for( location in locations){
            mMap.addMarker(
                MarkerOptions()
                    .position(location.latLng)
                    .title(location.name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker)))
            mMap.addCircle(
                CircleOptions()
                    .center(location.latLng)
                    .radius(location.Radius)
                    .strokeColor(0xFFFF0000.toInt())
                    .fillColor(0x22FF0000.toInt())
                    .strokeWidth(2f)
            )
        }
    }
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    updateMapAndCoordinates(currentLatLng)
                } else {
                    Toast.makeText(context, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                }
            }
    }


    fun updateMapAndCoordinates(latLng: LatLng) {


        // Remove the previous marker if it exists
        currentMarker?.remove()




        // Add marker to the map
        currentMarker =mMap.addMarker(MarkerOptions().position(latLng).title("You are here"))

        // Update the map to show the new marker
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

        // Update the TextView with the current coordinates
        checkMockLocation()

    }

    @SuppressLint("MissingInflatedId")
    private fun showFakeDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.mock_location, null)

        // Create and configure the dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)

        // Set up the close button
        val closeButton = dialogView.findViewById<TextView>(R.id.declinebtn)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        val openbtn = dialogView.findViewById<TextView>(R.id.acceptBtn)
        openbtn.setOnClickListener {
            openDeveloperOptions()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // Show the dialog
        dialog.show()





    }

    private fun fetchUserData() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Get current user ID
         userId = auth.currentUser?.email!!
//        Toast.makeText(requireContext(), "uid is  $userId", Toast.LENGTH_SHORT).show()

        if (userId != null) {
            // Fetch user data from Firestore
            db.collection("Employees Data").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                         userName = document.getString("userName")!!
                        val department = document.getString("department")

                        val settxt ="Welcome,"+userName
                        // Update UI with fetched data
                        userNameTextView.text = settxt

                    } else {
                        Toast.makeText(activity, "No such document", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Error fetching document: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
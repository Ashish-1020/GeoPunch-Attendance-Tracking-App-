package com.example.geopunch.admin

import android.location.Geocoder
import android.location.Address
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.example.geopunch.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng

import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddOffice.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddOfficeFragment : Fragment() , OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var ad: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_office, container, false)


        val mapFragment = childFragmentManager.findFragmentById(R.id.mapoffice) as SupportMapFragment
        mapFragment.getMapAsync(this)

        ad = view.findViewById(R.id.address)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val tvValue = view.findViewById<TextView>(R.id.tvValue)
        val add = view.findViewById<Button>(R.id.addbut)

        add.setOnClickListener{
            Toast.makeText(context, "New office location added", Toast.LENGTH_SHORT).show()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update TextView with the current progress
                tvValue.text = "$progress m"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do something when user starts interacting
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do something when user stops interacting
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddOffice.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddOfficeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(mMap: GoogleMap) {

        val location = LatLng(26.850238, 80.941498)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 15f)
        mMap.animateCamera(cameraUpdate)

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            val office = getAddressFromLatLng(latLng.latitude, latLng.longitude)
            ad.text = office
        }

    }

    private fun getAddressFromLatLng(lat: Double, lng: Double): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            // Get the address list from the Geocoder object
            val addresses: MutableList<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (addresses?.isNotEmpty() == true) {
                val address: Address = addresses[0]
                address.getAddressLine(0) // Get the full address
            } else {
                "No address found for the given location."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error getting address: ${e.message}"
        }
    }}

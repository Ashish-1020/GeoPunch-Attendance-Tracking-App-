package com.example.geopunch

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.geopunch.PermissionUtil.Companion.permissionIndex
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() , PermissionUtil.PermissionsCallBack {

    private var activeFragment: Fragment? = null
    private lateinit var homeFragment: Home_Fragment
    private lateinit var recordFragment: Record_Fragment
    private lateinit var profileFragment: Profile_Fragment
    private var backPressedTime: Long = 0
    private lateinit var toast: Toast


    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,

            Manifest.permission.CAMERA,


            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else { arrayOf( Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        requestPermissions();
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)



        // Initialize fragments
        homeFragment = Home_Fragment()
        recordFragment = Record_Fragment()
        profileFragment = Profile_Fragment()

        // Set the initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, profileFragment, "3").hide(profileFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, recordFragment, "2").hide(recordFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, homeFragment, "1")
                .commit()
            activeFragment = homeFragment
        } else {
            homeFragment = supportFragmentManager.findFragmentByTag("1") as Home_Fragment
            recordFragment = supportFragmentManager.findFragmentByTag("2") as Record_Fragment
            profileFragment = supportFragmentManager.findFragmentByTag("3") as Profile_Fragment
            activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        }

        // Handle bottom navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.navigation_records -> {
                    switchFragment(recordFragment)
                    true
                }
                R.id.navigation_profile -> {
                    switchFragment(profileFragment)
                    true
                }
                else -> false
            }
        }

    }

    private fun switchFragment(targetFragment: Fragment) {
        if (activeFragment != targetFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment!!)
                .show(targetFragment)
                .commit()
            activeFragment = targetFragment
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Show exit confirmation dialog
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            toast.cancel()
            finishAffinity()  // Close the app completely
        } else {
            toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT)
            toast.show()
        }
        backPressedTime = System.currentTimeMillis()
    }



    private fun requestPermissions() {

        PermissionUtil.requestPermissionsConsecutively(this, permissions)
        if(permissionIndex == permissions.size){

            recreate()
            permissionIndex++
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this)
    }

    override fun permissionsGranted() {
        Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
        requestPermissions()

    }

    override fun permissionsDenied() {
        Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_SHORT).show()
        requestPermissions()
    }




    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

}

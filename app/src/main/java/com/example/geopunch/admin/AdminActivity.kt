package com.example.geopunch.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.geopunch.Home_Fragment
import com.example.geopunch.Profile_Fragment
import com.example.geopunch.R
import com.example.geopunch.Record_Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

//    private lateinit var userIdInput: EditText
//    private lateinit var usernameInput: EditText
//    private lateinit var departmentInput: EditText
//    private lateinit var passwordInput: EditText



    private lateinit var homeFragment: AnalyticsFragment
    private var activeFragment: Fragment? = null
    private lateinit var OfficeFragment: AddOfficeFragment
    private lateinit var EmployeeFragment:  AddEmployeeFragment
    private lateinit var leaveFragment: LeaveManagementFragment
    private var backPressedTime: Long = 0
    private lateinit var toast: Toast

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.admin_nav)

        // Initialize fragments
        homeFragment = AnalyticsFragment()
        OfficeFragment = AddOfficeFragment()
         EmployeeFragment= AddEmployeeFragment()
        leaveFragment= LeaveManagementFragment()

        // Set the initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, leaveFragment, "4").hide(leaveFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, OfficeFragment, "3").hide(OfficeFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, EmployeeFragment, "2").hide(EmployeeFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, homeFragment, "1")
                .commit()
            activeFragment = homeFragment
        } else {
            homeFragment = supportFragmentManager.findFragmentByTag("1") as AnalyticsFragment
            EmployeeFragment= supportFragmentManager.findFragmentByTag("2") as AddEmployeeFragment
            OfficeFragment = supportFragmentManager.findFragmentByTag("3") as AddOfficeFragment
            leaveFragment = supportFragmentManager.findFragmentByTag("4") as LeaveManagementFragment
            activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        }

        // Handle bottom navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_analytics-> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.navigation_add_employee -> {
                    switchFragment(EmployeeFragment)
                    true
                }
                R.id.navigation_add_office -> {
                    switchFragment(OfficeFragment)
                    true
                }
                R.id.navigation_leave_management -> {
                    switchFragment(leaveFragment)
                    true
                }

                else -> false
            }
        }

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        userIdInput = findViewById(R.id.userId_input)
//        usernameInput = findViewById(R.id.userName_input)
//        departmentInput = findViewById(R.id.department_input)
//        passwordInput = findViewById(R.id.password_input)
//
//        findViewById<Button>(R.id.upload).setOnClickListener {
//            val inputEmail = userIdInput.text.toString().trim()
//            val inputName = usernameInput.text.toString().trim()
//            val inputDepartment = departmentInput.text.toString().trim()
//            val inputPassword = passwordInput.text.toString().trim()
//
//            if (inputEmail.isNotEmpty() && inputPassword.isNotEmpty() && inputName.isNotEmpty() && inputDepartment.isNotEmpty()) {
//                registerEmployee(inputEmail, inputPassword, inputDepartment, inputName)
//                Toast.makeText(this, "Function initiated", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
//            }
//        }
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

//    private fun registerEmployee(email: String, password: String, department: String, userName: String) {
//        val auth = FirebaseAuth.getInstance()
//        val db = FirebaseFirestore.getInstance()
//        Toast.makeText(this, "email is $email", Toast.LENGTH_SHORT).show()
//
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Retrieve the UID of the newly created user
//                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
//
//                    val employee = Employee(
//                        Department = department,
//                        ImageUrl = "",
//                        Unique = email,
//                        UserId = email,
//                        UserName = userName
//                    )
//
//                    Toast.makeText(this, "User employee ${employee.UserId}", Toast.LENGTH_SHORT).show()
//
//                    // Use UID as the document ID in Firestore
//                    db.collection("Employees Data")
//                        .document(employee.Unique)
//                        .set(employee)
//                        .addOnSuccessListener {
//                            Toast.makeText(this, "User is registered", Toast.LENGTH_SHORT).show()
//                        }
//                        .addOnFailureListener { e ->
//                            Toast.makeText(this, "Failed to create document in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
//                        }
//                } else {
//                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
}

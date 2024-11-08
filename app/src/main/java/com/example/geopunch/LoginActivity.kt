package com.example.geopunch

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.geopunch.admin.AdminActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    // UI elements
    private lateinit var editemail: EditText
    private lateinit var editpass: EditText
    private lateinit var loginBtn: TextView

    // Firebase variables
    private lateinit var auth: FirebaseAuth
    private var storedId: String? = null
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Apply window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize the EditText and Button
        editemail = findViewById(R.id.editTxtemail)
        editpass = findViewById(R.id.editTxtpassword)
        loginBtn = findViewById(R.id.loginBtn)

        // Set custom background for the login button
        loginBtn.setBackgroundResource(R.drawable.btnshape)

        // Set click listener for login button
        loginBtn.setOnClickListener {
            val inputemail = editemail.text.toString().trim()
            val inputpassword = editpass.text.toString().trim()

            // Check if email and password are not empty
            if (inputemail.isNotEmpty() && inputpassword.isNotEmpty()) {
                // Attempt to sign in with email and password
                auth.signInWithEmailAndPassword(inputemail, inputpassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.email

                            if (inputemail.equals("admingail@gmail.com", ignoreCase = true)) {
                                // Navigate to AdminActivity if admin logs in
                                val intent = Intent(this, AdminActivity::class.java)
                                intent.putExtra("UserID", inputemail)
                                startActivity(intent)
                            } else {
                                if (userId != null) {
                                    // Fetch user data from Firestore based on email
                                    db.collection("Employees Data").document(userId).get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                // Get DeviceId from Firestore
                                                storedId = document.getString("DeviceId")
                                                val androidId: String = Settings.Secure.getString(
                                                    contentResolver, Settings.Secure.ANDROID_ID
                                                )

                                                // Check if device is recognized
                                                if (storedId.isNullOrEmpty()) {
                                                    // If no device is registered, update Firestore with new DeviceId
                                                    db.collection("Employees Data").document(userId)
                                                        .update("DeviceId", androidId)
                                                        .addOnSuccessListener {
                                                            // Navigate to MainActivity on successful update
                                                            val intent = Intent(this, MainActivity::class.java)
                                                            intent.putExtra("UserID", inputemail)
                                                            startActivity(intent)
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            Toast.makeText(
                                                                this, "Failed to update DeviceId: ${exception.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                } else if (androidId == storedId) {
                                                    // Device recognized, navigate to MainActivity
                                                    val intent = Intent(this, MainActivity::class.java)
                                                    intent.putExtra("UserID", inputemail)
                                                    startActivity(intent)
                                                }

                                                   // Inside your existing code, replace the Toast for device not recognized with this:
                                                else {
                                                    // Device not recognized, show dialog to request admin access
                                                    val builder = AlertDialog.Builder(this)
                                                    builder.setTitle("New Device Detected")
                                                    builder.setMessage("You are trying to log in from a new device. Request access from the admin?")

                                                    // Set positive button "Yes"
                                                    builder.setPositiveButton("Yes") { dialog, which ->
                                                        // Handle request access action here
                                                        //sendAdminRequest(userId)  // Function to send request to admin (to be implemented)

                                                        // Show a toast saying the request has been sent
                                                        Toast.makeText(this, "Request sent. You will be notified once accepted by the admin.", Toast.LENGTH_LONG).show()
                                                    }

                                                    // Set negative button "No"
                                                    builder.setNegativeButton("No") { dialog, which ->
                                                        dialog.dismiss()  // Close the dialog
                                                    }

                                                    // Show the dialog
                                                    builder.show()
                                                }

                                            } else {
                                                Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(
                                                this, "Error fetching document: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Sign-in failed
                            Toast.makeText(this, "Please enter correct email and password", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already signed in
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email

            // If admin is signed in, navigate to AdminActivity
            if (email.equals("admingail@gmail.com", ignoreCase = true)) {
                val intent = Intent(this, AdminActivity::class.java)
                intent.putExtra("UserID", email)
                startActivity(intent)
            } else {
                if (email != null) {
                    // Fetch user data from Firestore
                    db.collection("Employees Data").document(email).get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                // Get stored DeviceId from Firestore
                                storedId = document.getString("DeviceId")
                                val androidId: String = Settings.Secure.getString(
                                    contentResolver, Settings.Secure.ANDROID_ID
                                )

                                // Check if the stored DeviceId matches the current device's ID
                                if (androidId == storedId) {
                                    // Device recognized, navigate to MainActivity
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("UserID", email)
                                    startActivity(intent)
                                } else {

                                }
                            } else {
                                Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this, "Error fetching document: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

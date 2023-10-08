package com.example.vk

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private var lastClickTime: Long = 0


    @RequiresApi(Build.VERSION_CODES.O)

    override fun onStart() {
        super.onStart()
        askNotificationPermission()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val checkButton = findViewById<Button>(R.id.Check_data)

        checkButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= 2000) { // Prevent clicks within 2 second
                lastClickTime = currentTime
                Log.d("MainActivity", "checkButton clicked")
                checkPhoneNumberInDatabase()
            }
        }

//        val serviceIntent = Intent(this, NotificationService::class.java)
//        startService(serviceIntent)

    }

    override fun onResume() {
        super.onResume()
        checkAuth()
        registerFCM()
    }

    private fun registerFCM() {
        FirebaseMessaging.getInstance()
            .token
            .addOnSuccessListener {
            Log.d("fcm", "Token: $it")
            FirebaseMessaging.getInstance().subscribeToTopic("booking").addOnSuccessListener {
                Log.d("fcm", "Subscribed to topic: booking")
            }
        }
    }
    private fun checkAuth() {
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }
    private fun checkPhoneNumberInDatabase() {
        val phoneEditText = findViewById<EditText>(R.id.Input_c)
        val ph_no = phoneEditText.text.toString()

        Log.d("MainActivity", "Checking phone number: +91$ph_no")

        val db = FirebaseFirestore.getInstance()
        val customersRef = db.collection("customerDetails")

        customersRef.whereEqualTo("ph_no", "+91$ph_no")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (documentSnapshot in querySnapshot.documents) {
                        val name = documentSnapshot.getString("name")
                        Log.d("MainActivity", "Phone number found in database: $ph_no, Name: $name")

                        // Phone number is present in the database, proceed to OTP_Page activity
                        val intent = Intent(this@MainActivity, OTP_Page::class.java)
                        intent.putExtra("ph_no", ph_no)
                        intent.putExtra("name", name)
                        startActivity(intent)
                    }
                } else {
                    // Phone number is not registered in the database, show a toast message
                    Log.d("MainActivity", "Formatted phone number: +91$ph_no")
                    Toast.makeText(applicationContext, "Non Registered +91$ph_no", Toast.LENGTH_LONG).show()
                    Log.d("MainActivity", "Phone number not found in the database")
                }
            }
    }

    private fun askNotificationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("notification", "Successfully added notifications")
            } else {
                Log.d("notification", "Failed to get permission for notifications")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("notification", "Permission already granted")
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

package com.example.vk

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var logoutButton: ImageView
    private lateinit var customBottomNavigationView: bottomNavigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)



        mAuth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logout)

        logoutButton.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }

        // Retrieve the user's name and phone number from SharedPreferences
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val userName = sharedPref.getString("user_name", "")
        val phoneNumber = sharedPref.getString("phone_number", "")

        // Find the TextViews in the activity_profile.xml layout
        val nameTextView = findViewById<TextView>(R.id.profileName)
        val phNoTextView = findViewById<TextView>(R.id.ph_no)
        // Log the retrieved values (for debugging)
        Log.d("txt1", "NAME: $userName")
        Log.d("txt1", "NUMBER: $phoneNumber")

        // Set the user's name and phone number in the TextViews
        nameTextView.text = userName
        phNoTextView.text = phoneNumber

        // Selected item for ProfileActivity is 3
        customBottomNavigationView = bottomNavigation(this, 3)
    }

    override fun onBackPressed() {
        if (!customBottomNavigationView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
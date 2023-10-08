package com.example.vk

import android.animation.Animator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class OTP_Page : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var phone: String
    private lateinit var name: String
    private val SHARED_PREFS_NAME = "customer-data"

    private lateinit var btnverify: Button
    private lateinit var verificationID: String
    private lateinit var Back: LottieAnimationView
    private var backButtonPressed = false
    private val EDGE_SWIPE_THRESHOLD = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OTP_Page", "onCreate called")
        setContentView(R.layout.activity_otp_page)

        mAuth = FirebaseAuth.getInstance()

        phone = intent.getStringExtra("ph_no").toString()
        name = intent.getStringExtra("name").toString()

        Log.d("OTP_Page", "Received phone number: $phone, Name: $name")

        val ph_no = findViewById<TextView>(R.id.ph_no)
        ph_no.append(" $phone")

        btnverify = findViewById(R.id.check)

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_SHORT).show()
        } else {
            val number = phone
            sendVerificationCode(number)
        }

        val otpEditTexts = arrayOf(
            findViewById<EditText>(R.id.otp1),
            findViewById<EditText>(R.id.otp2),
            findViewById<EditText>(R.id.otp3),
            findViewById<EditText>(R.id.otp4),
            findViewById<EditText>(R.id.otp5),
            findViewById<EditText>(R.id.otp6)
        )

        for (i in otpEditTexts.indices) {
            otpEditTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (before == 1 && s?.isEmpty() == true && i > 0) {
                        otpEditTexts[i - 1].requestFocus()
                    }
                    if (s?.length == 1 && i < otpEditTexts.size - 1) {
                        otpEditTexts[i + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        btnverify.setOnClickListener {
            val otp = otpEditTexts.joinToString("") { it.text.toString() }

            if (otp.length < 6) {
                Toast.makeText(this@OTP_Page, "Enter the complete OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifycode(otp)
            }
        }

        Back = findViewById(R.id.BackButton)

        Back.setOnClickListener {
            if (!backButtonPressed) {
                navigateToLoginLayout()
            }
        }

        val rootView = findViewById<View>(android.R.id.content)
        rootView.setOnTouchListener { _, event ->
            val x = event.x

            if (x < EDGE_SWIPE_THRESHOLD) {
                // Handle edge swipe, e.g., navigate back to MainActivity
                navigateToLoginLayout()
                true // Consume the touch event
            } else {
                false // Don't consume the touch event
            }
        }
    }

    private fun navigateToLoginLayout() {
        backButtonPressed = true

        Log.d("OTP_Page", "Navigating back to MainActivity")

        Back.setMinAndMaxProgress(0.5f, 1.0f)
        Back.playAnimation()
        Back.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(animation: Animator) {
                // Not needed for now
            }

            override fun onAnimationEnd(animation: Animator) {
                val intent = Intent(this@OTP_Page, MainActivity::class.java)
                startActivity(intent)
                finish() // Close the current OTP_Page activity
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Not needed for now
            }

            override fun onAnimationStart(animation: Animator) {
                // Not needed for now
            }
        })
    }

    private fun sendVerificationCode(phonenumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$phonenumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code = credential.smsCode


            if (code != null) {
                verifycode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // Handle verification failure
            Toast.makeText(this@OTP_Page, "Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(s: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(s, token)
            verificationID = s // Initialize verificationID here
        }
    }

    private fun verifycode(code: String) {
        val id = verificationID // Retrieve the verificationID
        if (id != null) {

            val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("phone", phone)
            editor.apply()

            val credential = PhoneAuthProvider.getCredential(id, code)
            signinbyCredentials(credential)
        } else {
            Toast.makeText(this, "Verification ID not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signinbyCredentials(credentials: PhoneAuthCredential) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithCredential(credentials)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    saveUserDataToFirestore()
                } else {
                    Toast.makeText(this, "Verification Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDataToFirestore() {
        val user = mAuth.currentUser
        val db = FirebaseFirestore.getInstance()

        if (user != null) {
            val userId = user.uid

            val userData = hashMapOf(
                "Name" to name,
                "ph_no" to phone
                // Add other user data fields as needed
            )

            db.collection("Login_Info").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this@OTP_Page, "Verification Successful", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@OTP_Page, Home::class.java)
                    intent.putExtra("name", name)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@OTP_Page, "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("OTP_Page", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("OTP_Page", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("OTP_Page", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("OTP_Page", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("OTP_Page", "onDestroy called")
    }

    override fun onBackPressed() {
        if (!backButtonPressed) {
            navigateToLoginLayout()
        }
    }
}

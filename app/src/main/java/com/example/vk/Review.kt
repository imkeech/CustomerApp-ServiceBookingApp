package com.example.vk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Review : AppCompatActivity() {
    private val reviewCollection = Firebase.firestore.collection("reviews")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        val reviewInput = findViewById<EditText>(R.id.reviewInput)
        val reviewBtn = findViewById<Button>(R.id.submitBtn)

        reviewBtn.setOnClickListener {
            val review = reviewInput.text.toString()
            val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "";
            // do analysis on the text
            Log.d("review", "Saving review: ${review} from phone ${phoneNumber}");
            val data = mapOf("customerPhone" to phoneNumber, "comment" to review);
            reviewCollection.add(data).addOnSuccessListener {
                Log.d("review", "Saved review")
                val goToHome = Intent(this, Home::class.java)
                startActivity(goToHome)
            }
        }
    }
}
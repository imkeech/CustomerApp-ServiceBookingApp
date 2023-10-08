package com.example.vk

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Notification : AppCompatActivity() {
    private lateinit var customBottomNavigationView: bottomNavigation
    private lateinit var notificationLinearLayout: LinearLayout
    private val notifDbRef = FirebaseFirestore.getInstance().collection("notifications")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        customBottomNavigationView = bottomNavigation(this, 2)
        notificationLinearLayout = findViewById(R.id.notificationLinearLayout)
        loadNotifications()
    }

    private fun loadNotifications() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        notifDbRef
            .whereEqualTo("phone", user.phoneNumber)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { doc ->
                    val cardView = layoutInflater.inflate(
                        R.layout.notification_card,
                        notificationLinearLayout,
                        false
                    )
                    Log.d("notification", "$notifDbRef")
                    val description = doc.getString("description")
                    val fromApp = doc.getString("app")
                    val cardDesc = cardView.findViewById<TextView>(R.id.notificationDescription)
                    val clearButton = cardView.findViewById<ImageView>(R.id.clearButton)

                    cardDesc.text = if (fromApp !="Engineer") {
                        description + "Resolution time: 48 hrs \n  "} else {description}

                    // Set a unique tag for the card view
                    cardView.tag = doc.id

                    // Set a unique tag for the clear button
                    clearButton.tag = doc.id

                    clearButton.setOnClickListener { view ->
                        val notificationId = view.tag as String
                        clearNotification(notificationId)
                    }

                    notificationLinearLayout.addView(cardView)
                }
            }
    }

    private fun clearNotification(notificationId: String) {
        // Remove the view from the UI
        val cardView = notificationLinearLayout.findViewWithTag<View>(notificationId)
        if (cardView != null) {
            notificationLinearLayout.removeView(cardView)
        }

        // Delete the notification document from Firestore
        notifDbRef.document(notificationId)
            .delete()
            .addOnSuccessListener {
                // Successfully deleted from Firestore
                // You can add any additional logic here
            }
            .addOnFailureListener { e ->
                // Handle the failure to delete from Firestore
                Log.e(TAG, "Error deleting notification: $e")
                // You may want to display an error message or handle the error differently
            }
    }


    override fun onBackPressed() {
        if (!customBottomNavigationView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}

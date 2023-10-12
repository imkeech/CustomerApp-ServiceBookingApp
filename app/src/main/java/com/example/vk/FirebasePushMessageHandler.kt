package com.example.vk

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebasePushMessageHandler : FirebaseMessagingService() {
    private val CHANNEL_ID = "app-booking-notifier"

    override fun onNewToken(token: String) {
        Log.d("fcm", "New Token: $token")
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("fcm", "Message received")

        val mAuth = FirebaseAuth.getInstance()
        val currentPhone = mAuth.currentUser?.phoneNumber
        val phone = remoteMessage.data["phone"]
        Log.d(
            "fcm",
            "Current phone number: '$currentPhone', received event for '$phone', " +
                    "should send: ${currentPhone == phone} ",
        )

        if (phone == currentPhone) {
            Log.d("fcm", "SEnding notification")
            showNotification(remoteMessage)
        }
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        createNotificationChannel()

        val notificationType = remoteMessage.data["type"]

        val notificationIntent: Intent;
        var notificationTitle = "";
        var notificationContent = "";

        if (notificationType == "statusUpdate") {
            notificationTitle = "Service status Update"
            notificationContent = "Your service status was updated to ${remoteMessage.data["status"]}. Click to submit a review."
            notificationIntent = Intent(this, Review::class.java)
        } else {

            val app = remoteMessage.data["app"]
            val content = remoteMessage.data["description"]

            notificationTitle = remoteMessage.data["title"] ?: "Booking Acceptd";
            notificationContent = if (app == "Engineer") {
                content.orEmpty()
            } else "Your service was assigned to Service Enginner\nFor more info click here"

            notificationIntent = Intent(this, Notification::class.java)
        }

        val notifIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(notificationTitle)
            .setContentIntent(notifIntent)
            .setContentText(notificationContent)
            .setAutoCancel(true)
            .build()

        val notificationId = 1234;

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@FirebasePushMessageHandler,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            } else {
                notify(notificationId, notification)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Service Requests"
            val descriptionText = "Alerts the user when a service request is accepted"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
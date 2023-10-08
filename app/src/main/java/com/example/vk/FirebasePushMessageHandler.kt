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
        val app = remoteMessage.data["app"]
        val content = remoteMessage.data["description"]
        val title = remoteMessage.data["title"]

        Log.d(
            "fcm",
            "Current phone number: '$currentPhone', received event for '$phone', " +
                    "should send: ${currentPhone == phone} ",
        )

        if (phone == currentPhone) {
            Log.d("fcm", "SEnding notification")
            showNotification(app, title, content)
        }
    }

    private fun showNotification(app: String?, title: String?, content: String?) {
        createNotificationChannel()

        val goToNotifications = Intent(this, Notification::class.java)

        val notifIntent = PendingIntent.getActivity(
            this,
            0,
            goToNotifications,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notifContent =  if (app == "Engineer") { content } else "Your service was assigned to Service Enginner\nFor more info click here"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title ?: "Booking Accepted")
            .setContentIntent(notifIntent)
            .setContentText(notifContent)
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
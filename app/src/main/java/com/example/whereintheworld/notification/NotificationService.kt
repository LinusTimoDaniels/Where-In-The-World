package com.example.whereintheworld.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.whereintheworld.R
import com.example.whereintheworld.home.MainActivity

class NotificationService : Service() {

    private val channelId = "default_channel"
    private val notificationId = 1
    private val handler = Handler()
    private val notificationRunnable = object : Runnable {
        override fun run() {
            sendNotification()
            handler.postDelayed(this, 10 * 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        handler.post(notificationRunnable)
    }

    private fun sendNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val vibrationPattern = longArrayOf(0, 100, 1000)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Good Morning!")
            .setContentText("This is your periodic notification.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVibrate(vibrationPattern)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Background Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for background notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(notificationRunnable)
    }
}

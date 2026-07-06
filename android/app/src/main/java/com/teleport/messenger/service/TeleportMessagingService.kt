package com.teleport.messenger.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.teleport.messenger.R

class TeleportMessagingService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL_ID, "Teleport Messages", NotificationManager.IMPORTANCE_DEFAULT)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Teleport")
            .setContentText("Синхронизация сообщений")
            .setSmallIcon(R.drawable.ic_launcher)
            .build()
        startForeground(1, notification)
        stopSelf()
        return START_NOT_STICKY
    }

    companion object {
        const val CHANNEL_ID = "teleport_messages"
    }
}

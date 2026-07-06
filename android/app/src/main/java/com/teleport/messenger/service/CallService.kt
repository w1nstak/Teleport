package com.teleport.messenger.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.teleport.messenger.MainActivity
import com.teleport.messenger.R
import com.teleport.messenger.TeleportApplication

class CallService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL_ID, "Teleport Calls", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = intent?.getStringExtra("type") ?: "voice"
        val chatId = intent?.getStringExtra("chatId") ?: ""
        val app = application as TeleportApplication
        app.webRtc.setSignalSender { payload -> app.realtime.sendCallSignal(payload) }
        app.webRtc.startCall(type == "video", chatId)

        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Teleport — ${if (type == "video") "Видеозвонок" else "Звонок"}")
            .setContentText("Звонок активен")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(open)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_CALL)
            .build()
        startForeground(2, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        (application as TeleportApplication).webRtc.endCall()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "teleport_calls"
    }
}

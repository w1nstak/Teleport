package com.teleport.messenger.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teleport.messenger.TeleportApplication
import com.teleport.messenger.util.NotificationHelper

class TeleportFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val app = applicationContext as TeleportApplication
        app.registerFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Teleport"
        val body = message.notification?.body ?: message.data["body"] ?: "Новое сообщение"
        NotificationHelper.showMessage(this, title, body)
    }

    companion object {
        private const val TAG = "TeleportFCM"
    }
}

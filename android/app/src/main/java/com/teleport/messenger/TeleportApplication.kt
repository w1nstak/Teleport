package com.teleport.messenger

import android.app.Application
import android.util.Log
import com.teleport.messenger.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.teleport.messenger.auth.DeviceSmsSender
import com.teleport.messenger.data.TeleportDatabase
import com.teleport.messenger.data.api.ApiClient
import com.teleport.messenger.data.api.FcmTokenRequest
import com.teleport.messenger.data.repository.TeleportRepository
import com.teleport.messenger.data.sync.RealtimeClient
import com.teleport.messenger.data.sync.toEntity
import com.teleport.messenger.util.NotificationHelper
import com.teleport.messenger.util.SyncScheduler
import com.teleport.messenger.webrtc.WebRtcCallManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class TeleportApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy { TeleportDatabase.get(this) }
    val smsSender by lazy { DeviceSmsSender(this) }
    val repository by lazy { TeleportRepository(database, smsSender) }
    val webRtc by lazy { WebRtcCallManager(this) }

    val realtime by lazy {
        RealtimeClient(
            onMessage = { dto ->
                appScope.launch {
                    repository.applyRemoteMessage(dto.toEntity())
                    NotificationHelper.showMessage(
                        this@TeleportApplication,
                        "Teleport",
                        dto.text.ifEmpty { "Новое сообщение" },
                    )
                }
            },
            onMessageUpdated = { dto ->
                appScope.launch { repository.applyRemoteMessage(dto.toEntity()) }
            },
            onCallSignal = { payload ->
                appScope.launch { webRtc.handleSignal(payload) }
            },
        )
    }

    var pendingYandexToken: String? = null

    override fun onCreate() {
        super.onCreate()
        SyncScheduler.schedule(this)
        appScope.launch {
            repository.activeAccount.distinctUntilChanged().collect { account ->
                if (account == null) {
                    realtime.disconnect()
                    return@collect
                }
                val token = repository.ensureBackendSession()
                if (token != null) {
                    realtime.connect(token)
                    repository.syncChatsFromServer()
                    repository.syncMessagesFromServer()
                    registerFcmIfAvailable()
                }
            }
        }
    }

    fun registerFcmToken(token: String) {
        appScope.launch {
            runCatching {
                val auth = repository.ensureBackendSession() ?: return@launch
                ApiClient.api.registerFcm("Bearer $auth", FcmTokenRequest(token))
            }.onFailure { Log.w("TeleportApp", "FCM register failed", it) }
        }
    }

    private fun registerFcmIfAvailable() {
        if (!BuildConfig.FIREBASE_SMS) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { registerFcmToken(it) }
    }
}

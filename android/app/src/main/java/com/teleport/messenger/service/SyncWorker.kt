package com.teleport.messenger.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teleport.messenger.TeleportApplication

class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as TeleportApplication
        return try {
            app.repository.syncMessagesFromServer()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

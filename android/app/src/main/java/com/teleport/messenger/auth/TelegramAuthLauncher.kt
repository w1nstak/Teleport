package com.teleport.messenger.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.json.Json

@Composable
fun rememberTelegramAuthLauncher(
    onSuccess: (TelegramUser) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val json = result.data?.getStringExtra(TelegramAuthActivity.EXTRA_USER_JSON)
                if (json.isNullOrBlank()) {
                    onError("Пустой ответ Telegram")
                } else {
                    runCatching { Json.decodeFromString<TelegramUser>(json) }
                        .onSuccess(onSuccess)
                        .onFailure { onError("Не удалось разобрать ответ Telegram") }
                }
            }
            else -> onError(
                result.data?.getStringExtra(TelegramAuthActivity.EXTRA_ERROR) ?: "Вход через Telegram отменён",
            )
        }
    }
    return {
        if (!TelegramAuthConfig.isConfigured()) {
            onError(
                "Telegram не настроен.\n" +
                    "Запустите SETUP_TELEGRAM.bat\n" +
                    "(@BotFather → бот → /setdomain)",
            )
        } else {
            launcher.launch(Intent(context, TelegramAuthActivity::class.java))
        }
    }
}

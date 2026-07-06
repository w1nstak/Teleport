package com.teleport.messenger.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** OAuth Telegram через Custom Tabs → callback com.teleport.messenger://telegram/callback */
class TelegramAuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let { uri ->
            if (uri.scheme == "com.teleport.messenger" && uri.host == "telegram") {
                finishWithCallback(uri)
                return
            }
        }
        if (!TelegramAuthConfig.isConfigured()) {
            setResult(Activity.RESULT_CANCELED, Intent().putExtra(EXTRA_ERROR, "Telegram не настроен"))
            finish()
            return
        }
        if (savedInstanceState == null) {
            openOAuth()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { finishWithCallback(it) }
    }

    private fun openOAuth() {
        val tabs = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        tabs.launchUrl(this, Uri.parse(TelegramAuthHelper.buildOAuthUrl()))
    }

    private fun finishWithCallback(uri: Uri) {
        TelegramAuthHelper.parseAndVerify(uri)
            .onSuccess { user ->
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(EXTRA_USER_JSON, Json.encodeToString(user)),
                )
            }
            .onFailure { e ->
                setResult(
                    Activity.RESULT_CANCELED,
                    Intent().putExtra(EXTRA_ERROR, e.message ?: "Ошибка Telegram"),
                )
            }
        finish()
    }

    companion object {
        const val EXTRA_USER_JSON = "telegram_user_json"
        const val EXTRA_ERROR = "telegram_error"
    }
}

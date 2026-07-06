package com.teleport.messenger.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.teleport.messenger.BuildConfig
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk

object YandexAuthConfig {
    fun isConfigured(): Boolean = BuildConfig.YANDEX_CLIENT_ID.isNotBlank()
}

/** Официальный Yandex Auth SDK — вход через настоящий аккаунт Яндекса */
@Composable
fun rememberYandexAuthLauncher(
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val sdk = androidx.compose.runtime.remember { YandexAuthSdk.create(YandexAuthOptions(context)) }
    val launcher = rememberLauncherForActivityResult(sdk.contract) { result ->
        when (result) {
            is YandexAuthResult.Success -> onSuccess(result.token.value)
            is YandexAuthResult.Failure -> onError(result.exception.message ?: "Ошибка Яндекс ID")
            YandexAuthResult.Cancelled -> onError("Вход отменён")
        }
    }
    return {
        if (!YandexAuthConfig.isConfigured()) {
            onError(
                "Яндекс ID не настроен.\n" +
                    "Запустите SETUP_YANDEX.bat в папке teleport\n" +
                    "(oauth.yandex.ru → Android → com.teleport.messenger)",
            )
        } else {
            launcher.launch(YandexAuthLoginOptions())
        }
    }
}

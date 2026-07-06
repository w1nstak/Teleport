package com.teleport.messenger.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest

@Serializable
data class YandexUserInfo(
    val id: String? = null,
    val login: String? = null,
    val display_name: String? = null,
    val real_name: String? = null,
    val default_email: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
)

object YandexAuthHelper {
    private val http = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun stableIdFromEmail(email: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(email.trim().lowercase().toByteArray())
            .take(8)
            .joinToString("") { "%02x".format(it) }

    suspend fun fetchUserInfo(accessToken: String): Result<YandexUserInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://login.yandex.ru/info?format=json")
                .header("Authorization", "OAuth $accessToken")
                .build()
            val response = http.newCall(request).execute()
            if (!response.isSuccessful) throw IllegalStateException("Ошибка авторизации (${response.code})")
            val body = response.body?.string() ?: throw IllegalStateException("Пустой ответ")
            json.decodeFromString<YandexUserInfo>(body).also {
                require(!it.id.isNullOrBlank()) { "Профиль не найден" }
            }
        }
    }

    fun displayName(info: YandexUserInfo): String =
        info.display_name?.takeIf { it.isNotBlank() }
            ?: info.real_name?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(info.first_name, info.last_name).joinToString(" ").ifBlank { info.login ?: "Яндекс" }
}

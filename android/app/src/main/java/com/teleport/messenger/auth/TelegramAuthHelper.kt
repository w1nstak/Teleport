package com.teleport.messenger.auth

import android.net.Uri
import com.teleport.messenger.BuildConfig
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Serializable
data class TelegramUser(
    val id: Long,
    val first_name: String,
    val last_name: String? = null,
    val username: String? = null,
    val photo_url: String? = null,
    val auth_date: Long,
)

object TelegramAuthConfig {
    fun isConfigured(): Boolean =
        BuildConfig.TELEGRAM_BOT_TOKEN.isNotBlank() && BuildConfig.TELEGRAM_BOT_USERNAME.isNotBlank()

    fun botId(): String =
        BuildConfig.TELEGRAM_BOT_TOKEN.substringBefore(":").takeIf { it.all(Char::isDigit) } ?: ""
}

object TelegramAuthHelper {
    private const val CALLBACK = "com.teleport.messenger://telegram/callback"
    private const val MAX_AUTH_AGE_SEC = 86_400L

    fun buildOAuthUrl(): String {
        val botId = TelegramAuthConfig.botId()
        val origin = URLEncoder.encode(BuildConfig.TELEGRAM_AUTH_ORIGIN, Charsets.UTF_8.name())
        val returnTo = URLEncoder.encode(CALLBACK, Charsets.UTF_8.name())
        return "https://oauth.telegram.org/auth" +
            "?bot_id=$botId" +
            "&origin=$origin" +
            "&return_to=$returnTo" +
            "&request_access=write"
    }

    fun parseAndVerify(uri: Uri): Result<TelegramUser> = runCatching {
        val params = uri.queryParameterNames.associateWith { uri.getQueryParameter(it).orEmpty() }
        if (params.isEmpty() || !params.containsKey("hash")) {
            throw IllegalArgumentException("Нет данных авторизации Telegram")
        }
        if (!verifyHash(params, BuildConfig.TELEGRAM_BOT_TOKEN)) {
            throw IllegalArgumentException("Подпись Telegram не прошла проверку")
        }
        val authDate = params["auth_date"]?.toLongOrNull()
            ?: throw IllegalArgumentException("Некорректная дата авторизации")
        val now = System.currentTimeMillis() / 1000
        if (now - authDate > MAX_AUTH_AGE_SEC) {
            throw IllegalArgumentException("Сессия Telegram устарела, войдите снова")
        }
        val id = params["id"]?.toLongOrNull()
            ?: throw IllegalArgumentException("Некорректный ID Telegram")
        val firstName = params["first_name"].orEmpty().ifBlank { "Telegram" }
        TelegramUser(
            id = id,
            first_name = firstName,
            last_name = params["last_name"]?.takeIf { it.isNotBlank() },
            username = params["username"]?.takeIf { it.isNotBlank() },
            photo_url = params["photo_url"]?.takeIf { it.isNotBlank() },
            auth_date = authDate,
        )
    }

    fun displayName(user: TelegramUser): String =
        listOfNotNull(user.first_name, user.last_name).joinToString(" ").ifBlank { user.username ?: "Telegram" }

    private fun verifyHash(data: Map<String, String>, botToken: String): Boolean {
        val hash = data["hash"] ?: return false
        val checkString = data
            .filterKeys { it != "hash" }
            .toSortedMap()
            .entries
            .joinToString("\n") { "${it.key}=${it.value}" }
        val secretKey = MessageDigest.getInstance("SHA-256")
            .digest(botToken.toByteArray(Charsets.UTF_8))
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey, "HmacSHA256"))
        val calculated = mac.doFinal(checkString.toByteArray(Charsets.UTF_8))
        val calculatedHex = calculated.joinToString("") { "%02x".format(it) }
        return calculatedHex == hash
    }
}

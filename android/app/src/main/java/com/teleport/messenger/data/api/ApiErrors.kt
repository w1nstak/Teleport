package com.teleport.messenger.data.api

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrors {
    private val json = Json { ignoreUnknownKeys = true }

    fun message(error: Throwable): String = when (error) {
        is HttpException -> httpMessage(error)
        is ConnectException, is UnknownHostException ->
            "Сервер недоступен. Запустите server (python main.py) и проверьте api.base.url в local.properties"
        is SocketTimeoutException -> "Таймаут соединения с сервером"
        is IOException -> "Ошибка сети: ${error.message ?: "нет связи"}"
        else -> error.message ?: "Неизвестная ошибка"
    }

    private fun httpMessage(error: HttpException): String {
        val raw = error.response()?.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return "Ошибка сервера (${error.code()})"
        return runCatching {
            val detail = json.decodeFromString<ErrorBody>(raw).detail
            when (detail) {
                is kotlinx.serialization.json.JsonPrimitive -> detail.content
                is kotlinx.serialization.json.JsonArray ->
                    detail.joinToString("\n") { it.toString().trim('"') }
                else -> detail.toString()
            }
        }.getOrElse { raw }
    }
}

@kotlinx.serialization.Serializable
private data class ErrorBody(val detail: kotlinx.serialization.json.JsonElement? = null)

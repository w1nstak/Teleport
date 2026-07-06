package com.teleport.messenger.data.sync

import com.teleport.messenger.data.api.ApiClient
import com.teleport.messenger.data.api.MessageDto
import com.teleport.messenger.data.entity.MessageEntity
import com.teleport.messenger.data.entity.MessageType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class RealtimeClient(
    private val onMessage: (MessageDto) -> Unit,
    private val onMessageUpdated: (MessageDto) -> Unit,
    private val onCallSignal: (JsonObject) -> Unit,
) {
    private val json = ApiClient.json
    private var webSocket: WebSocket? = null

    fun connect(token: String) {
        disconnect()
        val request = Request.Builder().url(ApiClient.wsUrl(token)).build()
        webSocket = ApiClient.httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val root = json.parseToJsonElement(text).jsonObject
                    when (root["event"]?.jsonPrimitive?.content) {
                        "message" -> {
                            val payload = root["payload"]?.jsonObject ?: return
                            onMessage(json.decodeFromJsonElement(MessageDto.serializer(), payload))
                        }
                        "message_updated" -> {
                            val payload = root["payload"]?.jsonObject ?: return
                            onMessageUpdated(json.decodeFromJsonElement(MessageDto.serializer(), payload))
                        }
                        "call_signal" -> root["payload"]?.jsonObject?.let(onCallSignal)
                        else -> Unit
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = Unit
        })
    }

    fun sendCallSignal(payload: JsonObject) {
        webSocket?.send("""{"event":"call_signal","payload":$payload}""")
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
    }
}

fun MessageDto.toEntity(): MessageEntity = MessageEntity(
    id = id,
    chatId = chatId,
    senderId = senderId,
    type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.TEXT),
    text = text,
    mediaUri = mediaUri,
    replyToId = replyToId,
    forwardFromId = forwardFromId,
    isEdited = isEdited,
    editedAt = editedAt,
    createdAt = createdAt,
)

fun MessageEntity.toDto(): MessageDto = MessageDto(
    id = id,
    chatId = chatId,
    senderId = senderId,
    type = type.name,
    text = text,
    mediaUri = mediaUri,
    replyToId = replyToId,
    forwardFromId = forwardFromId,
    isEdited = isEdited,
    editedAt = editedAt,
    createdAt = createdAt,
)

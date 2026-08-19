package com.teleport.messenger.data.api

import kotlinx.serialization.Serializable

@Serializable
data class WebRegisterRequest(val displayName: String, val username: String, val password: String)

@Serializable
data class AuthRequest(val phone: String, val password: String, val displayName: String? = null)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val accountId: String,
    val phone: String? = null,
    val displayName: String? = null,
    val username: String? = null,
)

@Serializable
data class UsernameLoginRequest(val username: String, val password: String)

@Serializable
data class QrAuthRequest(val qrToken: String, val deviceName: String)

@Serializable
data class UsernameCheckResponse(val available: Boolean)

@Serializable
data class UserDto(
    val id: String,
    val displayName: String,
    val username: String?,
    val bio: String = "",
    val status: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0,
    val isPremium: Boolean = false,
)

@Serializable
data class SendMessageRequest(
    val id: String? = null,
    val chatId: String,
    val type: String,
    val text: String = "",
    val mediaUri: String? = null,
    val replyToId: String? = null,
    val forwardFromId: String? = null,
)

@Serializable
data class MessageDto(
    val id: String,
    val chatId: String,
    val senderId: String,
    val type: String,
    val text: String = "",
    val mediaUri: String? = null,
    val replyToId: String? = null,
    val forwardFromId: String? = null,
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val createdAt: Long,
)

@Serializable
data class OpenChatRequest(val otherUserId: String)

@Serializable
data class OpenChatResponse(
    val chatId: String,
    val title: String,
    val type: String,
    val members: List<String> = emptyList(),
    val peer: UserDto? = null,
)

@Serializable
data class ChatListItemDto(
    val chatId: String,
    val title: String,
    val type: String,
    val members: List<String> = emptyList(),
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val status: String? = null,
)

@Serializable
data class SyncResponse(val messages: List<MessageDto> = emptyList())

@Serializable
data class EditMessageRequest(val text: String)

@Serializable
data class ForwardMessageRequest(val toChatId: String)

@Serializable
data class FcmTokenRequest(val token: String)

@Serializable
data class MediaUploadResponse(val url: String)

@Serializable
data class SmsSendRequest(val phone: String)

@Serializable
data class SmsVerifyRequest(val phone: String, val code: String)

@Serializable
data class SmsSendResponse(val ok: Boolean, val devCode: String? = null, val retryAfter: Int = 60)

@Serializable
data class SmsVerifyResponse(val ok: Boolean)

@Serializable
data class AdminStatsDto(
    val usersTotal: Int = 0,
    val messagesTotal: Int = 0,
    val messagesToday: Int = 0,
    val chatsTotal: Int = 0,
    val accountsTotal: Int = 0,
    val onlineNow: Int = 0,
    val wsConnections: Int = 0,
    val lastMessageAt: Long? = null,
    val publicUrl: String? = null,
    val ownerUsername: String? = null,
)

@Serializable
data class AdminCheckDto(val isOwner: Boolean, val ownerUsername: String? = null)

@Serializable
data class WsEnvelope(val event: String, val payload: kotlinx.serialization.json.JsonObject? = null)

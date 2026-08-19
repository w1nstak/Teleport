package com.teleport.messenger.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val phone: String,
    val email: String?,
    val passwordHash: String,
    val authToken: String?,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)],
)
data class UserEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val displayName: String,
    val username: String?,
    val bio: String = "",
    val status: String = "",
    val avatarUri: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val isPremium: Boolean = false,
    val starsBalance: Long = 0L,
    val privacyLastSeen: String = "everyone",
    val privacyPhone: String = "contacts",
    val privacyPhoto: String = "everyone",
    val anonymousMode: Boolean = false,
    val anonymousAlias: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val deviceName: String,
    val platform: String = "Android",
    val ipAddress: String?,
    val lastActive: Long,
    val isCurrent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class ChatType { PRIVATE, GROUP, CHANNEL, SAVED }

@Entity(
    tableName = "chats",
    indices = [Index("folderId"), Index("updatedAt")],
)
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: ChatType,
    val title: String,
    val avatarUri: String? = null,
    val description: String = "",
    val memberCount: Int = 0,
    val lastMessagePreview: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val folderId: String? = null,
    val wallpaperUri: String? = null,
    val wallpaperAnimated: Boolean = false,
    val muteUntil: Long = 0L,
    val notificationSound: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "chat_folders")
data class ChatFolderEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val name: String,
    val icon: String = "folder",
    val colorArgb: Int = 0xFF1565FF.toInt(),
    val sortOrder: Int = 0,
    val maxChats: Int = 100,
)

@Entity(
    tableName = "chat_members",
    primaryKeys = ["chatId", "userId"],
)
data class ChatMemberEntity(
    val chatId: String,
    val userId: String,
    val role: String = "member",
    val joinedAt: Long = System.currentTimeMillis(),
)

enum class MessageType {
    TEXT, PHOTO, VIDEO, VOICE, VIDEO_NOTE, GIF, DOCUMENT, FILE,
    MUSIC, CONTACT, LOCATION, STICKER, GIFT, SYSTEM,
}

@Entity(
    tableName = "messages",
    indices = [Index("chatId"), Index("senderId"), Index("createdAt")],
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val type: MessageType,
    val text: String = "",
    val mediaUri: String? = null,
    val mediaThumbUri: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0L,
    val durationMs: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val giftId: String? = null,
    val replyToId: String? = null,
    val forwardFromId: String? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val hasSpoiler: Boolean = false,
    val albumId: String? = null,
    val transcription: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
)

@Entity(
    tableName = "pinned_messages",
    primaryKeys = ["chatId", "messageId"],
)
data class PinnedMessageEntity(
    val chatId: String,
    val messageId: String,
    val pinnedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "reactions",
    primaryKeys = ["messageId", "userId", "emoji"],
)
data class ReactionEntity(
    val messageId: String,
    val userId: String,
    val emoji: String,
    val isAnimated: Boolean = false,
    val isPremium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "gifts")
data class GiftEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val imageUri: String,
    val animationUri: String? = null,
    val priceStars: Long,
    val rarity: String,
    val category: String,
    val isLimited: Boolean = false,
    val isCollectible: Boolean = false,
    val stockRemaining: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "user_gifts")
data class UserGiftEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val giftId: String,
    val fromUserId: String?,
    val messageId: String?,
    val acquiredAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "star_transactions")
data class StarTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Long,
    val type: String,
    val description: String,
    val relatedId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "marketplace_listings")
data class MarketplaceListingEntity(
    @PrimaryKey val id: String,
    val sellerId: String,
    val giftId: String,
    val priceStars: Long,
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis(),
    val soldAt: Long? = null,
)

@Entity(tableName = "marketplace_favorites")
data class MarketplaceFavoriteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val listingId: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "marketplace_trades")
data class MarketplaceTradeEntity(
    @PrimaryKey val id: String,
    val listingId: String,
    val buyerId: String,
    val sellerId: String,
    val priceStars: Long,
    val completedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "blocked_users",
    primaryKeys = ["accountId", "blockedUserId"],
)
data class BlockedUserEntity(
    val accountId: String,
    val blockedUserId: String,
    val blockedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val targetUserId: String?,
    val targetMessageId: String?,
    val reason: String,
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val initiatorId: String,
    val type: String,
    val isGroup: Boolean = false,
    val startedAt: Long,
    val endedAt: Long? = null,
    val status: String = "active",
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val accountId: String,
    val themeMode: String = "system",
    val colorThemeId: String = "violet",
    val useDynamicColor: Boolean = false,
    val useAnimatedEmoji: Boolean = true,
    val chatWallpaperId: String = "dark",
    val largeChatFont: Boolean = false,
    val bubbleAnimations: Boolean = true,
    val pinHash: String? = null,
    val biometricEnabled: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val twoFactorSecret: String? = null,
    val appLockEnabled: Boolean = false,
    val appLockTimeoutSec: Int = 60,
    val notificationsEnabled: Boolean = true,
    val silentMode: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val notificationSound: String = "default",
    val premiumUntil: Long = 0L,
    val maxPinnedChats: Int = 5,
    val maxFolders: Int = 3,
    val hideReadReceipts: Boolean = false,
    val hideOnlineStatus: Boolean = false,
    val lastSyncAt: Long = 0L,
    val localeOverridesJson: String = "{}",
    val powerSavingEnabled: Boolean = false,
)

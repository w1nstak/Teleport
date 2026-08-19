package com.teleport.messenger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.teleport.messenger.data.dao.*
import com.teleport.messenger.data.entity.*

class Converters {
    @TypeConverter fun fromChatType(v: ChatType) = v.name
    @TypeConverter fun toChatType(v: String) = ChatType.valueOf(v)
    @TypeConverter fun fromMessageType(v: MessageType) = v.name
    @TypeConverter fun toMessageType(v: String) = MessageType.valueOf(v)
}

@Database(
    entities = [
        AccountEntity::class, UserEntity::class, SessionEntity::class,
        ChatEntity::class, ChatFolderEntity::class, ChatMemberEntity::class,
        MessageEntity::class, PinnedMessageEntity::class, ReactionEntity::class,
        GiftEntity::class, UserGiftEntity::class, StarTransactionEntity::class,
        MarketplaceListingEntity::class, MarketplaceFavoriteEntity::class,
        MarketplaceTradeEntity::class, BlockedUserEntity::class, ReportEntity::class,
        CallEntity::class, AppSettingsEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TeleportDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun chatDao(): ChatDao
    abstract fun chatFolderDao(): ChatFolderDao
    abstract fun chatMemberDao(): ChatMemberDao
    abstract fun messageDao(): MessageDao
    abstract fun pinnedMessageDao(): PinnedMessageDao
    abstract fun reactionDao(): ReactionDao
    abstract fun giftDao(): GiftDao
    abstract fun userGiftDao(): UserGiftDao
    abstract fun starTransactionDao(): StarTransactionDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun blockedUserDao(): BlockedUserDao
    abstract fun reportDao(): ReportDao
    abstract fun callDao(): CallDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile private var instance: TeleportDatabase? = null

        fun get(context: Context): TeleportDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TeleportDatabase::class.java,
                    "teleport.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}

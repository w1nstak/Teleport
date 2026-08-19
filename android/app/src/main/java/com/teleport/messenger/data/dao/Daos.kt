package com.teleport.messenger.data.dao

import androidx.room.*
import com.teleport.messenger.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<AccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: AccountEntity)

    @Query("UPDATE accounts SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE accounts SET isActive = 1, authToken = :token WHERE id = :id")
    suspend fun activate(id: String, token: String?)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE accountId = :accountId LIMIT 1")
    fun observeByAccount(accountId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<UserEntity>

    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%' LIMIT 50")
    suspend fun search(query: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE users SET starsBalance = starsBalance + :delta WHERE id = :userId")
    suspend fun adjustStars(userId: String, delta: Long)

    @Query("SELECT COUNT(*) FROM users WHERE username = :username COLLATE NOCASE AND id != :excludeId")
    suspend fun countUsername(username: String, excludeId: String = ""): Int
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE accountId = :accountId ORDER BY lastActive DESC")
    fun observeByAccount(accountId: String): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE isArchived = 0 ORDER BY isPinned DESC, lastMessageTime DESC")
    fun observeActive(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE isArchived = 1 ORDER BY lastMessageTime DESC")
    fun observeArchived(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE folderId = :folderId ORDER BY isPinned DESC, lastMessageTime DESC")
    fun observeByFolder(folderId: String): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE type = 'SAVED' LIMIT 1")
    fun observeSaved(): Flow<ChatEntity?>

    @Query("SELECT * FROM chats WHERE id = :id")
    fun observeById(id: String): Flow<ChatEntity?>

    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun getById(id: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE isArchived = 0")
    suspend fun getAllActive(): List<ChatEntity>

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chat: ChatEntity)

    @Query("UPDATE chats SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE chats SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("UPDATE chats SET folderId = :folderId WHERE id = :id")
    suspend fun setFolder(id: String, folderId: String?)

    @Query("UPDATE chats SET wallpaperUri = :uri, wallpaperAnimated = :animated WHERE id = :id")
    suspend fun setWallpaper(id: String, uri: String?, animated: Boolean)

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE chats SET unreadCount = 0 WHERE isArchived = 0")
    suspend fun markAllRead()

    @Query("UPDATE chats SET lastMessagePreview = :preview, lastMessageTime = :time, unreadCount = unreadCount + :inc WHERE id = :id")
    suspend fun updateLastMessage(id: String, preview: String, time: Long, inc: Int = 0)
}

@Dao
interface ChatFolderDao {
    @Query("SELECT * FROM chat_folders WHERE accountId = :accountId ORDER BY sortOrder")
    fun observeByAccount(accountId: String): Flow<List<ChatFolderEntity>>

    @Query("SELECT * FROM chat_folders WHERE id = :id")
    suspend fun getById(id: String): ChatFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(folder: ChatFolderEntity)

    @Query("UPDATE chat_folders SET name = :name WHERE id = :id")
    suspend fun rename(id: String, name: String)

    @Query("DELETE FROM chat_folders WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isDeleted = 0 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPage(chatId: String, limit: Int, offset: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun observeByChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Query("UPDATE messages SET text = :text, isEdited = 1, editedAt = :editedAt WHERE id = :id")
    suspend fun edit(id: String, text: String, editedAt: Long)

    @Query("UPDATE messages SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isDeleted = 0 AND text LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchText(chatId: String, query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isDeleted = 0 AND type = :type ORDER BY createdAt DESC")
    suspend fun searchByType(chatId: String, type: MessageType): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isDeleted = 0 AND createdAt BETWEEN :from AND :to ORDER BY createdAt DESC")
    suspend fun searchByDate(chatId: String, from: Long, to: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isDeleted = 0 AND text LIKE '%http%' ORDER BY createdAt DESC")
    suspend fun searchLinks(chatId: String): List<MessageEntity>

    @Query("UPDATE messages SET isDeleted = 1 WHERE chatId = :chatId")
    suspend fun softDeleteAllInChat(chatId: String)

    @Query("UPDATE messages SET senderId = :newId WHERE senderId = :oldId")
    suspend fun updateSenderId(oldId: String, newId: String)

    @Query("UPDATE messages SET transcription = :text WHERE id = :id")
    suspend fun setTranscription(id: String, text: String)
}

@Dao
interface PinnedMessageDao {
    @Query("SELECT m.* FROM messages m INNER JOIN pinned_messages p ON m.id = p.messageId WHERE p.chatId = :chatId")
    fun observeByChat(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pin(entity: PinnedMessageEntity)

    @Query("DELETE FROM pinned_messages WHERE chatId = :chatId AND messageId = :messageId")
    suspend fun unpin(chatId: String, messageId: String)
}

@Dao
interface ReactionDao {
    @Query("SELECT * FROM reactions WHERE messageId = :messageId")
    fun observeByMessage(messageId: String): Flow<List<ReactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(reaction: ReactionEntity)

    @Query("DELETE FROM reactions WHERE messageId = :messageId AND userId = :userId AND emoji = :emoji")
    suspend fun remove(messageId: String, userId: String, emoji: String)
}

@Dao
interface GiftDao {
    @Query("SELECT * FROM gifts ORDER BY priceStars")
    fun observeAll(): Flow<List<GiftEntity>>

    @Query("SELECT * FROM gifts WHERE category = :category")
    fun observeByCategory(category: String): Flow<List<GiftEntity>>

    @Query("SELECT * FROM gifts WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<GiftEntity>

    @Query("SELECT * FROM gifts WHERE id = :id")
    suspend fun getById(id: String): GiftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(gift: GiftEntity)
}

@Dao
interface UserGiftDao {
    @Query("SELECT g.* FROM gifts g INNER JOIN user_gifts ug ON g.id = ug.giftId WHERE ug.userId = :userId ORDER BY ug.acquiredAt DESC")
    fun observeCollection(userId: String): Flow<List<GiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(userGift: UserGiftEntity)
}

@Dao
interface StarTransactionDao {
    @Query("SELECT * FROM star_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: String): Flow<List<StarTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: StarTransactionEntity)
}

@Dao
interface MarketplaceDao {
    @Query("SELECT * FROM marketplace_listings WHERE status = 'active' ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<MarketplaceListingEntity>>

    @Query("SELECT * FROM marketplace_listings WHERE sellerId = :userId OR id IN (SELECT listingId FROM marketplace_favorites WHERE userId = :userId)")
    fun observeUserRelated(userId: String): Flow<List<MarketplaceListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListing(listing: MarketplaceListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: MarketplaceFavoriteEntity)

    @Query("DELETE FROM marketplace_favorites WHERE userId = :userId AND listingId = :listingId")
    suspend fun removeFavorite(userId: String, listingId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: MarketplaceTradeEntity)

    @Query("SELECT * FROM marketplace_trades WHERE buyerId = :userId OR sellerId = :userId ORDER BY completedAt DESC")
    fun observeTrades(userId: String): Flow<List<MarketplaceTradeEntity>>
}

@Dao
interface BlockedUserDao {
    @Query("SELECT * FROM blocked_users WHERE accountId = :accountId")
    fun observeByAccount(accountId: String): Flow<List<BlockedUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun block(entity: BlockedUserEntity)

    @Query("DELETE FROM blocked_users WHERE accountId = :accountId AND blockedUserId = :userId")
    suspend fun unblock(accountId: String, userId: String)
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ReportEntity)
}

@Dao
interface CallDao {
    @Query("SELECT * FROM calls WHERE chatId = :chatId ORDER BY startedAt DESC LIMIT 20")
    fun observeByChat(chatId: String): Flow<List<CallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(call: CallEntity)

    @Query("UPDATE calls SET endedAt = :endedAt, status = 'ended' WHERE chatId = :chatId AND status = 'active'")
    suspend fun endActiveForChat(chatId: String, endedAt: Long)

    @Query("SELECT * FROM calls ORDER BY startedAt DESC LIMIT 50")
    fun observeRecent(): Flow<List<CallEntity>>
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE accountId = :accountId")
    fun observe(accountId: String): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)
}

@Dao
interface ChatMemberDao {
    @Query("SELECT u.* FROM users u INNER JOIN chat_members cm ON u.id = cm.userId WHERE cm.chatId = :chatId")
    suspend fun getMembers(chatId: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: ChatMemberEntity)

    @Query("UPDATE chat_members SET userId = :newId WHERE userId = :oldId")
    suspend fun updateUserId(oldId: String, newId: String)
}

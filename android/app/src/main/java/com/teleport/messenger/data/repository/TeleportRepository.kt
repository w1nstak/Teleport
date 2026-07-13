package com.teleport.messenger.data.repository

import com.teleport.messenger.auth.DeviceSmsSender
import com.teleport.messenger.data.TeleportDatabase
import com.teleport.messenger.data.api.ApiClient
import com.teleport.messenger.data.api.AuthRequest
import com.teleport.messenger.data.api.EditMessageRequest
import com.teleport.messenger.data.api.ForwardMessageRequest
import com.teleport.messenger.data.api.OpenChatRequest
import com.teleport.messenger.data.api.QrAuthRequest
import com.teleport.messenger.data.api.SendMessageRequest
import com.teleport.messenger.data.api.SmsSendRequest
import com.teleport.messenger.data.api.SmsVerifyRequest
import com.teleport.messenger.data.api.AdminStatsDto
import com.teleport.messenger.data.api.ApiErrors
import com.teleport.messenger.data.api.UpdateProfileRequest
import com.teleport.messenger.data.api.UserDto
import com.teleport.messenger.data.api.AuthResponse
import com.teleport.messenger.data.api.UsernameLoginRequest
import com.teleport.messenger.data.sync.toDto
import com.teleport.messenger.data.sync.toEntity
import com.teleport.messenger.data.entity.*
import com.teleport.messenger.util.PrivacyHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.security.MessageDigest
import java.util.UUID

class TeleportRepository(
    private val db: TeleportDatabase,
    private val smsSender: DeviceSmsSender,
) {
    companion object {
        private val pendingSmsCodes = mutableMapOf<String, String>()
        const val TELEPORT_SYSTEM_USER_ID = "teleport_system"
    }
    private val accountDao = db.accountDao()
    private val userDao = db.userDao()
    private val sessionDao = db.sessionDao()
    private val chatDao = db.chatDao()
    private val chatFolderDao = db.chatFolderDao()
    private val chatMemberDao = db.chatMemberDao()
    private val messageDao = db.messageDao()
    private val pinnedDao = db.pinnedMessageDao()
    private val reactionDao = db.reactionDao()
    private val giftDao = db.giftDao()
    private val userGiftDao = db.userGiftDao()
    private val starDao = db.starTransactionDao()
    private val marketDao = db.marketplaceDao()
    private val blockedDao = db.blockedUserDao()
    private val reportDao = db.reportDao()
    private val callDao = db.callDao()
    private val settingsDao = db.appSettingsDao()

    val activeAccount: Flow<AccountEntity?> = accountDao.observeActive()
    val activeChats: Flow<List<ChatEntity>> = activeAccount.flatMapLatest { account ->
        if (account == null) {
            chatDao.observeActive()
        } else {
            combine(
                chatDao.observeActive(),
                blockedDao.observeByAccount(account.id),
                userDao.observeByAccount(account.id),
            ) { chats, blocked, user ->
                val blockedIds = blocked.map { it.blockedUserId }.toSet()
                val userId = user?.id
                chats.filterNot { chatInvolvesBlocked(it, blockedIds) }
                    .filter { isVisibleInChatList(it, userId) }
            }
        }
    }

    private fun isVisibleInChatList(chat: ChatEntity, userId: String?): Boolean {
        if (chat.isArchived || chat.type == ChatType.SAVED) return false
        if (userId == null) return true
        if (chat.id == "welcome_$userId") return true
        if (chat.id.startsWith("p_")) return true
        return false
    }
    val archivedChats: Flow<List<ChatEntity>> = chatDao.observeArchived()
    val allGifts: Flow<List<GiftEntity>> = giftDao.observeAll()
    val activeListings: Flow<List<MarketplaceListingEntity>> = marketDao.observeActive()

    fun observeUser(accountId: String) = userDao.observeByAccount(accountId)
    fun observeSettings(accountId: String) = settingsDao.observe(accountId)
    fun observeSessions(accountId: String) = sessionDao.observeByAccount(accountId)
    fun observeFolders(accountId: String) = chatFolderDao.observeByAccount(accountId)
    fun observeMessages(chatId: String) = messageDao.observeByChat(chatId)
    fun observePinned(chatId: String) = pinnedDao.observeByChat(chatId)
    fun observeReactions(messageId: String) = reactionDao.observeByMessage(messageId)
    fun observeChat(chatId: String) = chatDao.observeById(chatId)
    fun observeStarHistory(userId: String) = starDao.observeByUser(userId)
    fun observeGiftCollection(userId: String) = userGiftDao.observeCollection(userId)
    fun observeTrades(userId: String) = marketDao.observeTrades(userId)
    fun observeBlocked(accountId: String) = blockedDao.observeByAccount(accountId)

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        refreshLocalChats()
        if (giftDao.search("").isNotEmpty()) return@withContext
        seedGifts()
        seedMarketplace()
    }

    private suspend fun seedMarketplace() {
        marketDao.upsertListing(MarketplaceListingEntity("ml1", "u_demo3", "g1", 75, "active"))
        marketDao.upsertListing(MarketplaceListingEntity("ml2", "u_demo2", "g4", 250, "active"))
        marketDao.upsertListing(MarketplaceListingEntity("ml3", "u_demo1", "g2", 120, "active"))
    }

    private suspend fun seedGifts() {
        val gifts = listOf(
            GiftEntity("g1", "Dolphin Leap", "Animated dolphin gift", "gift_dolphin", "anim_dolphin", 50, "rare", "animated", isCollectible = true),
            GiftEntity("g2", "Star Burst", "Premium star explosion", "gift_star", "anim_star", 100, "epic", "premium", isLimited = true, stockRemaining = 500),
            GiftEntity("g3", "Blue Wave", "Ocean wave animation", "gift_wave", "anim_wave", 25, "common", "nature"),
            GiftEntity("g4", "Crystal Heart", "Collectible crystal", "gift_heart", null, 200, "legendary", "collectible", isCollectible = true, isLimited = true, stockRemaining = 100),
            GiftEntity("g5", "Teleport Spark", "Signature spark gift", "gift_spark", "anim_spark", 75, "rare", "signature"),
        )
        gifts.forEach { giftDao.upsert(it) }
    }

    private fun hashPassword(password: String): String =
        MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }

    suspend fun sendSmsViaServer(fullPhone: String): Result<String?> = withContext(Dispatchers.IO) {
        runCatching {
            val resp = try {
                ApiClient.api.sendSms(SmsSendRequest(fullPhone))
            } catch (e: HttpException) {
                val detail = e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
                throw IllegalStateException(detail ?: "Сервер не отправил СМС (${e.code()})")
            }
            if (!resp.ok) throw IllegalStateException("Сервер не отправил СМС")
            resp.devCode
        }
    }

    suspend fun verifySmsViaServer(fullPhone: String, code: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            ApiClient.api.verifySms(SmsVerifyRequest(fullPhone, code.trim())).ok
        }.getOrDefault(false)
    }

    suspend fun sendSmsCode(fullPhone: String, smsPermissionGranted: Boolean): Result<String?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val code = (100000..999999).random().toString()
                pendingSmsCodes[fullPhone] = code
                when (smsSender.send(fullPhone, code, smsPermissionGranted).getOrThrow()) {
                    com.teleport.messenger.auth.SmsDelivery.SENT -> null
                    com.teleport.messenger.auth.SmsDelivery.ON_SCREEN -> code
                }
            }
        }

    suspend fun verifySmsCode(fullPhone: String, code: String): Boolean = withContext(Dispatchers.IO) {
        val ok = pendingSmsCodes[fullPhone] == code.trim()
        if (ok) pendingSmsCodes.remove(fullPhone)
        ok
    }

    suspend fun findAccountByPhone(fullPhone: String): AccountEntity? = withContext(Dispatchers.IO) {
        accountDao.observeAll().first().find { it.phone == fullPhone }
    }

    suspend fun accountExists(fullPhone: String): Boolean = findAccountByPhone(fullPhone) != null

    suspend fun loginByPhone(fullPhone: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val local = findAccountByPhone(fullPhone)
            val localUser = local?.let { userDao.observeByAccount(it.id).first() }
            val resp = runCatching {
                backendLoginOrRegister(fullPhone, localUser?.displayName ?: "User", register = false)
            }.getOrNull()
            if (resp != null) {
                applyServerAuth(resp, fullPhone, localUser?.displayName ?: "User", createDemo = local == null)
                userDao.getById(resp.userId)!!
            } else {
                localLoginByPhone(fullPhone)
            }
        }
    }

    suspend fun registerByPhone(fullPhone: String, displayName: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (findAccountByPhone(fullPhone) != null) throw IllegalArgumentException("Already registered")
                val resp = runCatching {
                    backendLoginOrRegister(fullPhone, displayName, register = true)
                }.getOrNull()
                if (resp != null) {
                    applyServerAuth(resp, fullPhone, displayName, createDemo = true)
                    userDao.getById(resp.userId)!!
                } else {
                    localRegisterByPhone(fullPhone, displayName)
                }
            }
        }

    private suspend fun localLoginByPhone(fullPhone: String): UserEntity {
        val account = findAccountByPhone(fullPhone)
            ?: throw IllegalArgumentException("Аккаунт не найден. Зарегистрируйтесь с этим номером")
        accountDao.deactivateAll()
        accountDao.activate(account.id, account.authToken ?: UUID.randomUUID().toString())
        pendingSmsCodes.remove(fullPhone)
        return userDao.observeByAccount(account.id).first()!!
    }

    private suspend fun localRegisterByPhone(fullPhone: String, displayName: String): UserEntity {
        val accountId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val token = UUID.randomUUID().toString()
        accountDao.deactivateAll()
        accountDao.upsert(
            AccountEntity(accountId, fullPhone, null, hashPassword(fullPhone), token, true),
        )
        val user = UserEntity(userId, accountId, displayName, null, starsBalance = 100)
        userDao.upsert(user)
        settingsDao.upsert(AppSettingsEntity(accountId))
        sessionDao.upsert(
            SessionEntity(
                UUID.randomUUID().toString(),
                accountId,
                "This device",
                ipAddress = null,
                lastActive = System.currentTimeMillis(),
                isCurrent = true,
            ),
        )
        createDefaultChats(userId, displayName)
        pendingSmsCodes.remove(fullPhone)
        return user
    }

    suspend fun loginByUsername(username: String, password: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val clean = username.removePrefix("@").trim()
                if (clean.isBlank()) throw IllegalArgumentException("Введите @username")
                val resp = try {
                    ApiClient.api.loginByUsername(UsernameLoginRequest(clean, password))
                } catch (e: Exception) {
                    throw IllegalStateException(ApiErrors.message(e), e)
                }
                val phone = resp.phone ?: throw IllegalStateException("Аккаунт не найден")
                val displayName = resp.displayName ?: clean
                applyServerAuth(resp, phone, displayName, createDemo = false, password = password)
                userDao.getById(resp.userId)?.let { u ->
                    userDao.upsert(u.copy(username = resp.username ?: u.username))
                }
                userDao.getById(resp.userId)!!
            }
        }

    suspend fun registerByUsername(displayName: String, username: String, password: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val clean = username.removePrefix("@").trim()
                if (clean.length < 3) throw IllegalArgumentException("Имя пользователя: минимум 3 символа")
                if (password.length < 8) throw IllegalArgumentException("Пароль: минимум 8 символов")
                val resp = try {
                    ApiClient.api.registerWeb(
                        com.teleport.messenger.data.api.WebRegisterRequest(displayName.trim(), clean, password),
                    )
                } catch (e: Exception) {
                    throw IllegalStateException(ApiErrors.message(e), e)
                }
                val phone = resp.phone ?: "web:$clean"
                applyServerAuth(resp, phone, displayName.trim(), createDemo = false, password = password)
                userDao.getById(resp.userId)?.let { u ->
                    userDao.upsert(u.copy(username = resp.username ?: clean))
                }
                userDao.getById(resp.userId)!!
            }
        }

    fun observeRecentCalls(): Flow<List<CallEntity>> = callDao.observeRecent()

    suspend fun loginWithTelegram(
        telegramId: Long,
        displayName: String,
        username: String?,
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val telegramKey = "telegram:$telegramId"
            val accounts = accountDao.observeAll().first()
            val existing = accounts.find { it.email == telegramKey || it.phone == telegramKey }
            if (existing != null) {
                accountDao.deactivateAll()
                accountDao.activate(existing.id, existing.authToken ?: UUID.randomUUID().toString())
                val user = userDao.observeByAccount(existing.id).first()!!
                if (username != null && user.username.isNullOrBlank()) {
                    userDao.upsert(user.copy(username = username))
                    return@runCatching user.copy(username = username)
                }
                return@runCatching user
            }
            val accountId = UUID.randomUUID().toString()
            val userId = UUID.randomUUID().toString()
            val token = UUID.randomUUID().toString()
            accountDao.deactivateAll()
            accountDao.upsert(AccountEntity(accountId, telegramKey, telegramKey, hashPassword(token), token, true))
            val name = displayName.ifBlank { username?.let { "@$it" } ?: "Telegram" }
            val user = UserEntity(userId, accountId, name, username, starsBalance = 100)
            userDao.upsert(user)
            settingsDao.upsert(AppSettingsEntity(accountId))
            sessionDao.upsert(
                SessionEntity(
                    UUID.randomUUID().toString(),
                    accountId,
                    "This device",
                    ipAddress = null,
                    lastActive = System.currentTimeMillis(),
                    isCurrent = true,
                ),
            )
            createDefaultChats(userId, name)
            user
        }
    }

    suspend fun loginWithYandex(yandexId: String, displayName: String, email: String?): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val yandexKey = "yandex:$yandexId"
                val accounts = accountDao.observeAll().first()
                val existing = accounts.find { it.email == yandexKey || it.phone == yandexKey }
                if (existing != null) {
                    accountDao.deactivateAll()
                    accountDao.activate(existing.id, existing.authToken ?: UUID.randomUUID().toString())
                    return@runCatching userDao.observeByAccount(existing.id).first()!!
                }
                val accountId = UUID.randomUUID().toString()
                val userId = UUID.randomUUID().toString()
                val token = UUID.randomUUID().toString()
                accountDao.deactivateAll()
                accountDao.upsert(AccountEntity(accountId, yandexKey, yandexKey, hashPassword(token), token, true))
                val name = displayName.ifBlank { email?.substringBefore("@") ?: "Яндекс" }
                val user = UserEntity(userId, accountId, name, null, starsBalance = 100)
                userDao.upsert(user)
                settingsDao.upsert(AppSettingsEntity(accountId))
                sessionDao.upsert(SessionEntity(UUID.randomUUID().toString(), accountId, "This device", ipAddress = null, lastActive = System.currentTimeMillis(), isCurrent = true))
                createDefaultChats(userId, name)
                user
            }
        }

    suspend fun register(phone: String, password: String, displayName: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val accountId = UUID.randomUUID().toString()
                val userId = UUID.randomUUID().toString()
                val token = try {
                    ApiClient.api.register(AuthRequest(phone, password, displayName)).token
                } catch (_: Exception) {
                    UUID.randomUUID().toString()
                }
                accountDao.deactivateAll()
                accountDao.upsert(AccountEntity(accountId, phone, null, hashPassword(password), token, true))
                val user = UserEntity(userId, accountId, displayName, null, starsBalance = 100)
                userDao.upsert(user)
                settingsDao.upsert(AppSettingsEntity(accountId))
                sessionDao.upsert(SessionEntity(UUID.randomUUID().toString(), accountId, "This device", ipAddress = null, lastActive = System.currentTimeMillis(), isCurrent = true))
                createDefaultChats(userId, displayName)
                user
            }
        }

    suspend fun login(phone: String, password: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val accounts = accountDao.observeAll().first()
                val account = accounts.find { it.phone == phone && it.passwordHash == hashPassword(password) }
                    ?: throw IllegalArgumentException("Invalid credentials")
                val token = try {
                    ApiClient.api.login(AuthRequest(phone, password)).token
                } catch (_: Exception) {
                    account.authToken ?: UUID.randomUUID().toString()
                }
                accountDao.deactivateAll()
                accountDao.activate(account.id, token)
                userDao.observeByAccount(account.id).first()!!
            }
        }

    suspend fun qrLogin(qrToken: String, deviceName: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resp = try {
                    ApiClient.api.qrLogin(QrAuthRequest(qrToken, deviceName))
                } catch (_: Exception) {
                    val acc = accountDao.observeActive().first() ?: throw IllegalStateException("No account")
                    com.teleport.messenger.data.api.AuthResponse(acc.authToken ?: "", userDao.observeByAccount(acc.id).first()!!.id, acc.id)
                }
                accountDao.deactivateAll()
                accountDao.activate(resp.accountId, resp.token)
                userDao.getById(resp.userId)!!
            }
        }

    suspend fun recover(phone: String, newPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                try { ApiClient.api.recover(AuthRequest(phone, newPassword)) } catch (_: Exception) {}
                val accounts = accountDao.observeAll().first()
                val account = accounts.find { it.phone == phone } ?: throw IllegalArgumentException("Account not found")
                accountDao.upsert(account.copy(passwordHash = hashPassword(newPassword)))
            }
        }

    suspend fun logout(accountId: String) = withContext(Dispatchers.IO) {
        accountDao.deactivateAll()
    }

    suspend fun switchAccount(accountId: String) = withContext(Dispatchers.IO) {
        val account = accountDao.observeAll().first().find { it.id == accountId }
            ?: throw IllegalArgumentException("Аккаунт не найден")
        accountDao.deactivateAll()
        accountDao.activate(accountId, account.authToken)
    }

    suspend fun refreshLocalChats() = withContext(Dispatchers.IO) {
        val account = accountDao.observeActive().first() ?: return@withContext
        val user = userDao.observeByAccount(account.id).first() ?: return@withContext
        ensureSavedChat(user.id)
        ensureWelcomeChat(user.id, user.displayName)
        pruneUnknownChats(user.id)
    }

    suspend fun ensureSavedChatForUser(userId: String): String = withContext(Dispatchers.IO) {
        ensureSavedChat(userId)
        "saved_$userId"
    }

    private suspend fun ensureSavedChat(userId: String) {
        val savedId = "saved_$userId"
        if (chatDao.getById(savedId) != null) return
        chatDao.upsert(
            ChatEntity(
                savedId,
                ChatType.SAVED,
                "Избранное",
                lastMessagePreview = "Сохраняйте сообщения",
                lastMessageTime = System.currentTimeMillis(),
            ),
        )
        chatMemberDao.upsert(ChatMemberEntity(savedId, userId, "owner"))
    }

    private suspend fun ensureWelcomeChat(userId: String, name: String) {
        userDao.upsert(
            UserEntity(
                id = TELEPORT_SYSTEM_USER_ID,
                accountId = "system",
                displayName = "Teleport",
                username = "teleport",
                bio = "Официальный аккаунт Teleport",
                isPremium = true,
            ),
        )
        val welcomeChatId = "welcome_$userId"
        if (chatDao.getById(welcomeChatId) != null) return
        val greeting = name.trim().ifBlank { "друг" }
        val welcomeText = buildString {
            append("Добро пожаловать в Teleport, $greeting!\n\n")
            append("Teleport — быстрый и защищённый мессенджер.\n\n")
            append("• Найдите друзей через поиск (@username)\n")
            append("• Настройте анонимность в «Конфиденциальность»\n")
            append("• Нажмите карандаш, чтобы начать новый чат\n\n")
            append("Приятного общения!")
        }
        chatDao.upsert(
            ChatEntity(
                id = welcomeChatId,
                type = ChatType.PRIVATE,
                title = "Teleport",
                lastMessagePreview = "Добро пожаловать в Teleport!",
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = 1,
                isPinned = true,
            ),
        )
        chatMemberDao.upsert(ChatMemberEntity(welcomeChatId, userId))
        chatMemberDao.upsert(ChatMemberEntity(welcomeChatId, TELEPORT_SYSTEM_USER_ID))
        messageDao.upsert(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = welcomeChatId,
                senderId = TELEPORT_SYSTEM_USER_ID,
                type = MessageType.TEXT,
                text = welcomeText,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    private suspend fun pruneUnknownChats(userId: String) {
        val keep = mutableSetOf("welcome_$userId", "saved_$userId")
        chatDao.getAllActive().forEach { chat ->
            if (chat.id.startsWith("p_")) keep.add(chat.id)
        }
        chatDao.getAllActive().forEach { chat ->
            if (chat.id !in keep) {
                chatDao.deleteById(chat.id)
            }
        }
    }

    private suspend fun createDefaultChats(userId: String, name: String) {
        ensureSavedChat(userId)
        ensureWelcomeChat(userId, name)
        pruneUnknownChats(userId)
    }

    fun observeAllAccounts() = accountDao.observeAll()

    suspend fun openOrCreatePrivateChat(currentUserId: String, otherUserId: String): String =
        withContext(Dispatchers.IO) {
            upsertRemoteUserLocally(otherUserId)
            val token = ensureBackendSession()
            if (token != null) {
                runCatching {
                    val resp = ApiClient.api.openChat("Bearer $token", OpenChatRequest(otherUserId))
                    resp.peer?.let { upsertUserDto(it) }
                    val canonical = canonicalPrivateChatId(currentUserId, otherUserId)
                    val title = resp.title.ifBlank {
                        resp.peer?.displayName ?: userDao.getById(otherUserId)?.displayName ?: "Чат"
                    }
                    chatDao.upsert(
                        ChatEntity(
                            id = canonical,
                            type = ChatType.PRIVATE,
                            title = title,
                        ),
                    )
                    chatMemberDao.upsert(ChatMemberEntity(canonical, currentUserId))
                    chatMemberDao.upsert(ChatMemberEntity(canonical, otherUserId))
                    return@withContext canonical
                }
            }
            val canonical = canonicalPrivateChatId(currentUserId, otherUserId)
            chatDao.getById(canonical)?.id ?: run {
                val other = userDao.getById(otherUserId)
                    ?: throw IllegalArgumentException("User not found")
                chatDao.upsert(
                    ChatEntity(
                        id = canonical,
                        type = ChatType.PRIVATE,
                        title = PrivacyHelper.displayName(other, false, false),
                    ),
                )
                chatMemberDao.upsert(ChatMemberEntity(canonical, currentUserId))
                chatMemberDao.upsert(ChatMemberEntity(canonical, otherUserId))
                canonical
            }
        }

    private fun canonicalPrivateChatId(a: String, b: String): String {
        val (x, y) = if (a < b) a to b else b to a
        return "p_${x}_$y"
    }

    private suspend fun backendLoginOrRegister(phone: String, displayName: String, register: Boolean): AuthResponse {
        val pwd = phone
        return if (register) {
            ApiClient.api.register(AuthRequest(phone, pwd, displayName))
        } else {
            runCatching { ApiClient.api.login(AuthRequest(phone, pwd)) }
                .getOrElse { ApiClient.api.register(AuthRequest(phone, pwd, displayName)) }
        }
    }

    private suspend fun applyServerAuth(
        resp: AuthResponse,
        phone: String,
        displayName: String,
        createDemo: Boolean,
        password: String = phone,
    ) {
        val existing = findAccountByPhone(phone)
        val localUser = existing?.let { userDao.observeByAccount(it.id).first() }
        if (localUser != null && localUser.id != resp.userId) {
            migrateUserId(localUser.id, resp.userId)
        }
        accountDao.deactivateAll()
        accountDao.upsert(
            AccountEntity(
                id = resp.accountId,
                phone = phone,
                email = existing?.email,
                passwordHash = hashPassword(password),
                authToken = resp.token,
                isActive = true,
            ),
        )
        val user = UserEntity(
            id = resp.userId,
            accountId = resp.accountId,
            displayName = resp.displayName ?: displayName,
            username = resp.username ?: localUser?.username,
            bio = localUser?.bio ?: "",
            starsBalance = localUser?.starsBalance ?: 100,
            isPremium = localUser?.isPremium == true,
        )
        userDao.upsert(user)
        if (settingsDao.observe(resp.accountId).first() == null) {
            settingsDao.upsert(AppSettingsEntity(resp.accountId))
        }
        sessionDao.upsert(
            SessionEntity(
                UUID.randomUUID().toString(),
                resp.accountId,
                "This device",
                ipAddress = null,
                lastActive = System.currentTimeMillis(),
                isCurrent = true,
            ),
        )
        if (createDemo) {
            ensureSavedChat(resp.userId)
        }
        ensureWelcomeChat(resp.userId, displayName)
        pruneUnknownChats(resp.userId)
        syncChatsFromServer()
    }

    private suspend fun migrateUserId(oldId: String, newId: String) {
        if (oldId == newId) return
        userDao.getById(oldId)?.let { user ->
            userDao.delete(oldId)
            userDao.upsert(user.copy(id = newId))
        }
        chatMemberDao.updateUserId(oldId, newId)
        messageDao.updateSenderId(oldId, newId)
    }

    private suspend fun upsertUserDto(dto: UserDto) {
        userDao.upsert(
            UserEntity(
                id = dto.id,
                accountId = "remote",
                displayName = dto.displayName,
                username = dto.username,
                bio = dto.bio,
                isOnline = dto.isOnline,
                lastSeen = dto.lastSeen,
                isPremium = dto.isPremium,
            ),
        )
    }

    private suspend fun upsertRemoteUserLocally(userId: String) {
        if (userDao.getById(userId) != null) return
        val token = ensureBackendSession() ?: return
        runCatching {
            ApiClient.api.getUser("Bearer $token", userId)
        }.onSuccess { upsertUserDto(it) }
    }

    suspend fun syncChatsFromServer() = withContext(Dispatchers.IO) {
        val token = ensureBackendSession() ?: return@withContext
        val account = accountDao.observeActive().first() ?: return@withContext
        val me = userDao.observeByAccount(account.id).first() ?: return@withContext
        runCatching { ApiClient.api.listChats("Bearer $token") }.getOrNull()?.forEach { item ->
            if (chatDao.getById(item.chatId) == null) return@forEach
            chatDao.upsert(
                ChatEntity(
                    id = item.chatId,
                    type = runCatching { ChatType.valueOf(item.type) }.getOrDefault(ChatType.PRIVATE),
                    title = item.title,
                ),
            )
            item.members.forEach { memberId ->
                chatMemberDao.upsert(ChatMemberEntity(item.chatId, memberId))
                if (memberId != me.id) upsertRemoteUserLocally(memberId)
            }
        }
    }

    suspend fun updateProfile(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.upsert(user)
        val token = ensureBackendSession()
        if (token != null) {
            runCatching {
                ApiClient.api.updateMe(
                    "Bearer $token",
                    UpdateProfileRequest(user.displayName, user.username, user.bio),
                )
            }
        }
    }

    suspend fun checkUsernameAvailable(username: String, excludeId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                ApiClient.api.checkUsername(username, excludeId).available
            } catch (_: Exception) {
                userDao.countUsername(username, excludeId) == 0
            }
        }

    suspend fun searchUsers(query: String): List<UserEntity> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val account = accountDao.observeActive().first()
        val blockedIds = account?.let { blockedDao.observeByAccount(it.id).first().map { b -> b.blockedUserId }.toSet() } ?: emptySet()
        val token = ensureBackendSession()
        val results = if (token != null) {
            runCatching {
                ApiClient.api.searchUsers("Bearer $token", query).map { dto ->
                    UserEntity(
                        dto.id, "remote", dto.displayName, dto.username, dto.bio,
                        isOnline = dto.isOnline, lastSeen = dto.lastSeen, isPremium = dto.isPremium,
                    )
                }
            }.getOrElse { userDao.search(query) }
        } else {
            userDao.search(query)
        }
        val filtered = results.filterNot { blockedIds.contains(it.id) }
        filtered.forEach { userDao.upsert(it) }
        filtered
    }

    suspend fun loadUsersByIds(ids: List<String>): List<UserEntity> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext emptyList()
        userDao.getByIds(ids)
    }

    private fun chatInvolvesBlocked(chat: ChatEntity, blockedIds: Set<String>): Boolean {
        if (chat.type != ChatType.PRIVATE || blockedIds.isEmpty()) return false
        return blockedIds.any { id -> chat.id.contains(id) }
    }

    suspend fun ensureBackendSession(): String? = withContext(Dispatchers.IO) {
        val account = accountDao.observeActive().first() ?: return@withContext null
        val user = userDao.observeByAccount(account.id).first() ?: return@withContext null
        val phone = account.phone
        val tokenOnly = phone.isBlank() ||
            phone.startsWith("yandex:") ||
            phone.startsWith("telegram:") ||
            phone.startsWith("web:")
        if (tokenOnly) {
            val token = account.authToken?.takeIf { it.isNotBlank() } ?: return@withContext null
            runCatching { ApiClient.api.syncMessages("Bearer $token", 0) }
            return@withContext token
        }
        runCatching {
            val token = account.authToken?.takeIf { it.isNotBlank() }
            if (token != null) {
                runCatching { ApiClient.api.syncMessages("Bearer $token", 0) }.onSuccess { return@withContext token }
            }
            val resp = runCatching {
                ApiClient.api.login(AuthRequest(phone, phone))
            }.getOrElse {
                ApiClient.api.register(AuthRequest(phone, phone, user.displayName))
            }
            if (user.id != resp.userId) migrateUserId(user.id, resp.userId)
            accountDao.deactivateAll()
            accountDao.upsert(
                account.copy(id = resp.accountId, authToken = resp.token, isActive = true),
            )
            userDao.upsert(user.copy(id = resp.userId, accountId = resp.accountId))
            resp.token
        }.getOrNull()
    }

    suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        accountDao.observeActive().first()?.authToken
    }

    suspend fun syncMessagesFromServer(): Int = withContext(Dispatchers.IO) {
        val token = ensureBackendSession() ?: return@withContext 0
        val account = accountDao.observeActive().first() ?: return@withContext 0
        val settings = settingsDao.observe(account.id).first()
        val since = settings?.lastSyncAt ?: 0L
        val remote = runCatching {
            ApiClient.api.syncMessages("Bearer $token", since).messages
        }.getOrElse { return@withContext 0 }
        remote.forEach { dto ->
            applyRemoteMessage(dto.toEntity())
        }
        if (remote.isNotEmpty()) {
            val newSyncAt = remote.maxOf { it.createdAt }
            settingsDao.upsert((settings ?: AppSettingsEntity(account.id)).copy(lastSyncAt = newSyncAt))
        }
        remote.size
    }

    suspend fun applyRemoteMessage(msg: MessageEntity) = withContext(Dispatchers.IO) {
        if (chatDao.getById(msg.chatId) == null) {
            ensureChatForMessage(msg)
        }
        messageDao.upsert(msg)
        val preview = previewFor(msg)
        chatDao.updateLastMessage(msg.chatId, preview, msg.createdAt)
    }

    private suspend fun ensureChatForMessage(msg: MessageEntity) {
        syncChatsFromServer()
        if (chatDao.getById(msg.chatId) != null) return
        val account = accountDao.observeActive().first() ?: return
        val me = userDao.observeByAccount(account.id).first() ?: return
        upsertRemoteUserLocally(msg.senderId)
        val sender = userDao.getById(msg.senderId)
        chatDao.upsert(
            ChatEntity(
                id = msg.chatId,
                type = ChatType.PRIVATE,
                title = sender?.displayName ?: "Контакт",
            ),
        )
        chatMemberDao.upsert(ChatMemberEntity(msg.chatId, me.id))
        if (msg.senderId != me.id) {
            chatMemberDao.upsert(ChatMemberEntity(msg.chatId, msg.senderId))
        }
    }

    suspend fun uploadMediaFile(file: File, mime: String): String? = withContext(Dispatchers.IO) {
        val token = ensureBackendSession() ?: return@withContext null
        runCatching { ApiClient.uploadFile(token, file, mime) }.getOrNull()
    }

    private fun previewFor(msg: MessageEntity): String = when (msg.type) {
        MessageType.TEXT -> msg.text.take(100)
        MessageType.PHOTO -> "📷 Photo"
        MessageType.VIDEO -> "🎬 Video"
        MessageType.VOICE -> "🎤 Voice"
        else -> msg.text.ifEmpty { msg.type.name }
    }

    private suspend fun pushMessageToServer(msg: MessageEntity) {
        val token = ensureBackendSession() ?: return
        runCatching {
            ApiClient.api.sendMessage("Bearer $token", SendMessageRequest(
                id = msg.id,
                chatId = msg.chatId,
                type = msg.type.name,
                text = msg.text,
                mediaUri = msg.mediaUri,
                replyToId = msg.replyToId,
                forwardFromId = msg.forwardFromId,
            ))
        }
    }

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        type: MessageType,
        text: String = "",
        mediaUri: String? = null,
        replyToId: String? = null,
        durationMs: Long = 0L,
        hasSpoiler: Boolean = false,
    ): MessageEntity = withContext(Dispatchers.IO) {
        val chat = chatDao.getById(chatId) ?: throw IllegalStateException("Chat not found")
        val account = accountDao.observeActive().first()
        if (account != null) {
            val blockedIds = blockedDao.observeByAccount(account.id).first().map { it.blockedUserId }.toSet()
            if (chatInvolvesBlocked(chat, blockedIds)) {
                throw IllegalStateException("Пользователь заблокирован")
            }
        }
        val msg = MessageEntity(
            UUID.randomUUID().toString(), chatId, senderId, type, text, mediaUri,
            replyToId = replyToId, durationMs = durationMs, hasSpoiler = hasSpoiler,
        )
        messageDao.upsert(msg)
        chatDao.updateLastMessage(chatId, previewFor(msg), msg.createdAt)
        pushMessageToServer(msg)
        msg
    }

    suspend fun editMessage(messageId: String, newText: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        messageDao.edit(messageId, newText, now)
        val token = ensureBackendSession()
        if (token != null) {
            runCatching {
                ApiClient.api.editMessage("Bearer $token", messageId, EditMessageRequest(newText))
            }
        }
    }

    suspend fun forwardMessage(messageId: String, toChatId: String, senderId: String) = withContext(Dispatchers.IO) {
        val original = messageDao.getById(messageId) ?: return@withContext
        val msg = MessageEntity(
            UUID.randomUUID().toString(), toChatId, senderId, original.type,
            original.text, original.mediaUri, forwardFromId = messageId,
        )
        messageDao.upsert(msg)
        chatDao.updateLastMessage(toChatId, previewFor(msg), msg.createdAt)
        val token = ensureBackendSession()
        if (token != null) {
            runCatching {
                ApiClient.api.forwardMessage("Bearer $token", messageId, ForwardMessageRequest(toChatId))
            }
        }
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        messageDao.softDelete(messageId)
    }

    suspend fun pinMessage(chatId: String, messageId: String) = withContext(Dispatchers.IO) {
        pinnedDao.pin(PinnedMessageEntity(chatId, messageId))
    }

    suspend fun unpinMessage(chatId: String, messageId: String) = withContext(Dispatchers.IO) {
        pinnedDao.unpin(chatId, messageId)
    }

    suspend fun addReaction(messageId: String, userId: String, emoji: String, animated: Boolean = false, premium: Boolean = false) =
        withContext(Dispatchers.IO) {
            reactionDao.add(ReactionEntity(messageId, userId, emoji, animated, premium))
        }

    suspend fun removeReaction(messageId: String, userId: String, emoji: String) = withContext(Dispatchers.IO) {
        reactionDao.remove(messageId, userId, emoji)
    }

    suspend fun searchMessages(chatId: String, query: String, filter: String): List<MessageEntity> =
        withContext(Dispatchers.IO) {
            when (filter) {
                "photos" -> messageDao.searchByType(chatId, MessageType.PHOTO)
                "videos" -> messageDao.searchByType(chatId, MessageType.VIDEO)
                "voice" -> messageDao.searchByType(chatId, MessageType.VOICE)
                "files" -> messageDao.searchByType(chatId, MessageType.DOCUMENT) + messageDao.searchByType(chatId, MessageType.FILE)
                "links" -> messageDao.searchLinks(chatId)
                "date" -> messageDao.searchByDate(chatId, 0, System.currentTimeMillis())
                else -> messageDao.searchText(chatId, query)
            }
        }

    suspend fun transcribeVoice(messageId: String, userId: String): String? = withContext(Dispatchers.IO) {
        val settings = settingsDao.observe(userDao.getById(userId)?.accountId ?: return@withContext null).first()
        val isPremium = settings?.premiumUntil?.let { it > System.currentTimeMillis() } ?: false
        if (!isPremium) return@withContext null
        val text = "Расшифровка голосового сообщения (Premium)"
        messageDao.setTranscription(messageId, text)
        text
    }

    suspend fun pinChat(chatId: String, pinned: Boolean) = withContext(Dispatchers.IO) {
        chatDao.setPinned(chatId, pinned)
    }

    suspend fun archiveChat(chatId: String, archived: Boolean) = withContext(Dispatchers.IO) {
        chatDao.setArchived(chatId, archived)
    }

    suspend fun setChatWallpaper(chatId: String, uri: String?, animated: Boolean) = withContext(Dispatchers.IO) {
        chatDao.setWallpaper(chatId, uri, animated)
    }

    suspend fun createFolder(accountId: String, name: String, color: Int) = withContext(Dispatchers.IO) {
        chatFolderDao.upsert(ChatFolderEntity(UUID.randomUUID().toString(), accountId, name, colorArgb = color))
    }

    suspend fun moveChatToFolder(chatId: String, folderId: String?) = withContext(Dispatchers.IO) {
        chatDao.setFolder(chatId, folderId)
    }

    suspend fun purchasePremium(accountId: String, months: Int) = withContext(Dispatchers.IO) {
        val until = System.currentTimeMillis() + months * 30L * 24 * 3600 * 1000
        val s = settingsDao.observe(accountId).first() ?: AppSettingsEntity(accountId)
        settingsDao.upsert(s.copy(premiumUntil = until, maxPinnedChats = 20, maxFolders = 20))
        val user = userDao.observeByAccount(accountId).first() ?: return@withContext
        userDao.upsert(user.copy(isPremium = true))
    }

    suspend fun buyStars(userId: String, amount: Long) = withContext(Dispatchers.IO) {
        userDao.adjustStars(userId, amount)
        starDao.insert(StarTransactionEntity(UUID.randomUUID().toString(), userId, amount, "purchase", "Куплено $amount искр"))
    }

    suspend fun transferStars(fromUserId: String, toUserId: String, amount: Long): Boolean = withContext(Dispatchers.IO) {
        val from = userDao.getById(fromUserId) ?: return@withContext false
        if (from.starsBalance < amount) return@withContext false
        userDao.adjustStars(fromUserId, -amount)
        userDao.adjustStars(toUserId, amount)
        starDao.insert(StarTransactionEntity(UUID.randomUUID().toString(), fromUserId, -amount, "transfer_out", "Перевод искр"))
        starDao.insert(StarTransactionEntity(UUID.randomUUID().toString(), toUserId, amount, "transfer_in", "Получено искр"))
        true
    }

    suspend fun sendGift(fromUserId: String, toUserId: String, giftId: String, chatId: String): Boolean =
        withContext(Dispatchers.IO) {
            val gift = giftDao.getById(giftId) ?: return@withContext false
            val from = userDao.getById(fromUserId) ?: return@withContext false
            if (from.starsBalance < gift.priceStars) return@withContext false
            userDao.adjustStars(fromUserId, -gift.priceStars)
            starDao.insert(StarTransactionEntity(UUID.randomUUID().toString(), fromUserId, -gift.priceStars, "gift", "Sent ${gift.name}"))
            userGiftDao.add(UserGiftEntity(UUID.randomUUID().toString(), toUserId, giftId, fromUserId, null))
            sendMessage(chatId, fromUserId, MessageType.GIFT, gift.name, giftId)
            true
        }

    suspend fun listOnMarketplace(sellerId: String, giftId: String, price: Long) = withContext(Dispatchers.IO) {
        marketDao.upsertListing(MarketplaceListingEntity(UUID.randomUUID().toString(), sellerId, giftId, price))
    }

    suspend fun buyFromMarketplace(buyerId: String, listingId: String): Boolean = withContext(Dispatchers.IO) {
        val listings = marketDao.observeActive().first()
        val listing = listings.find { it.id == listingId && it.status == "active" } ?: return@withContext false
        if (!transferStars(buyerId, listing.sellerId, listing.priceStars)) return@withContext false
        marketDao.upsertListing(listing.copy(status = "sold", soldAt = System.currentTimeMillis()))
        userGiftDao.add(UserGiftEntity(UUID.randomUUID().toString(), buyerId, listing.giftId, listing.sellerId, null))
        marketDao.insertTrade(MarketplaceTradeEntity(UUID.randomUUID().toString(), listingId, buyerId, listing.sellerId, listing.priceStars))
        true
    }

    suspend fun toggleMarketplaceFavorite(userId: String, listingId: String, add: Boolean) = withContext(Dispatchers.IO) {
        if (add) marketDao.addFavorite(MarketplaceFavoriteEntity(UUID.randomUUID().toString(), userId, listingId))
        else marketDao.removeFavorite(userId, listingId)
    }

    suspend fun startCall(chatId: String, initiatorId: String, type: String, isGroup: Boolean) = withContext(Dispatchers.IO) {
        callDao.upsert(CallEntity(UUID.randomUUID().toString(), chatId, initiatorId, type, isGroup, System.currentTimeMillis()))
    }

    suspend fun endCall(callId: String) = withContext(Dispatchers.IO) {
        // Call records updated via CallService
    }

    suspend fun blockUser(accountId: String, userId: String) = withContext(Dispatchers.IO) {
        blockedDao.block(BlockedUserEntity(accountId, userId))
    }

    suspend fun unblockUser(accountId: String, userId: String) = withContext(Dispatchers.IO) {
        blockedDao.unblock(accountId, userId)
    }

    suspend fun report(reporterId: String, targetUserId: String?, messageId: String?, reason: String, details: String) =
        withContext(Dispatchers.IO) {
            reportDao.insert(ReportEntity(UUID.randomUUID().toString(), reporterId, targetUserId, messageId, reason, details))
        }

    suspend fun updateSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.upsert(settings)
    }

    suspend fun terminateSession(sessionId: String) = withContext(Dispatchers.IO) {
        sessionDao.delete(sessionId)
    }

    suspend fun markChatRead(chatId: String) = withContext(Dispatchers.IO) {
        chatDao.markRead(chatId)
    }

    suspend fun markAllChatsRead() = withContext(Dispatchers.IO) {
        chatDao.markAllRead()
    }

    suspend fun loadMessagesPage(chatId: String, page: Int, pageSize: Int = 50): List<MessageEntity> =
        withContext(Dispatchers.IO) {
            messageDao.getPage(chatId, pageSize, page * pageSize)
        }

    suspend fun getContactForChat(chatId: String, currentUserId: String): UserEntity? = withContext(Dispatchers.IO) {
        chatMemberDao.getMembers(chatId).firstOrNull { it.id != currentUserId }
    }

    suspend fun muteChat(chatId: String, muted: Boolean) = withContext(Dispatchers.IO) {
        val chat = chatDao.getById(chatId) ?: return@withContext
        chatDao.upsert(chat.copy(muteUntil = if (muted) Long.MAX_VALUE else 0L))
    }

    suspend fun clearChat(chatId: String) = withContext(Dispatchers.IO) {
        messageDao.softDeleteAllInChat(chatId)
        chatDao.getById(chatId)?.let { chat ->
            chatDao.upsert(chat.copy(lastMessagePreview = "", unreadCount = 0))
        }
    }

    suspend fun adminCheck(): Boolean = withContext(Dispatchers.IO) {
        val account = accountDao.observeActive().first() ?: return@withContext false
        val user = userDao.observeByAccount(account.id).first()
        val localOwner = isOwnerUsername(user?.username)
        val token = getAuthToken() ?: return@withContext localOwner
        runCatching { ApiClient.api.adminCheck("Bearer $token").isOwner }.getOrDefault(localOwner) || localOwner
    }

    private fun isOwnerUsername(username: String?): Boolean =
        username?.trim()?.removePrefix("@")?.lowercase() == "w1nst"

    suspend fun adminStats(): Result<AdminStatsDto> = withContext(Dispatchers.IO) {
        val account = accountDao.observeActive().first()
        val user = account?.let { userDao.observeByAccount(it.id).first() }
        val localOwner = isOwnerUsername(user?.username)
        val token = getAuthToken()
        if (token != null) {
            runCatching { ApiClient.api.adminStats("Bearer $token") }
                .onSuccess { return@withContext Result.success(it) }
        }
        if (localOwner) {
            return@withContext Result.success(buildLocalAdminStats())
        }
        Result.failure(IllegalStateException("Нет авторизации"))
    }

    private suspend fun buildLocalAdminStats(): AdminStatsDto {
        val accounts = accountDao.observeAll().first()
        val chats = chatDao.getAllActive()
        return AdminStatsDto(
            usersTotal = accounts.size.coerceAtLeast(1),
            accountsTotal = accounts.size,
            chatsTotal = chats.size,
            messagesTotal = chats.count { it.lastMessagePreview.isNotEmpty() },
            messagesToday = 0,
            onlineNow = 1,
            wsConnections = 0,
            lastMessageAt = chats.maxOfOrNull { it.lastMessageTime }?.takeIf { it > 0 },
            publicUrl = "Локально (сервер недоступен)",
            ownerUsername = "w1nst",
        )
    }
}

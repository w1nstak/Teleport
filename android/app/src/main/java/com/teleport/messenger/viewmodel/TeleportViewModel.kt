package com.teleport.messenger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teleport.messenger.auth.PhoneAuthHandler
import com.teleport.messenger.auth.SmsAuthMode
import com.teleport.messenger.auth.YandexAuthHelper
import com.teleport.messenger.data.entity.*
import com.teleport.messenger.data.repository.TeleportRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeleportViewModel(
    private val repo: TeleportRepository,
    private val phoneAuth: PhoneAuthHandler?,
) : ViewModel() {
    val activeAccount = repo.activeAccount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val chats = repo.activeChats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val archivedChats = repo.archivedChats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val gifts = repo.allGifts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val listings = repo.activeListings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAccounts = repo.observeAllAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _lastSmsCode = MutableStateFlow<String?>(null)
    val lastSmsCode: StateFlow<String?> = _lastSmsCode

    private var pendingPhone: String? = null
    private var smsMode: SmsAuthMode = SmsAuthMode.LOCAL

    init {
        viewModelScope.launch { repo.initializeIfNeeded() }
    }

    fun currentUser(): Flow<UserEntity?> = activeAccount.flatMapLatest { acc ->
        if (acc == null) flowOf(null) else repo.observeUser(acc.id)
    }

    fun settings(): Flow<AppSettingsEntity?> = activeAccount.flatMapLatest { acc ->
        if (acc == null) flowOf(null) else repo.observeSettings(acc.id)
    }

    fun sessions(): Flow<List<SessionEntity>> = activeAccount.flatMapLatest { acc ->
        if (acc == null) flowOf(emptyList()) else repo.observeSessions(acc.id)
    }

    fun folders(): Flow<List<ChatFolderEntity>> = activeAccount.flatMapLatest { acc ->
        if (acc == null) flowOf(emptyList()) else repo.observeFolders(acc.id)
    }

    fun messages(chatId: String) = repo.observeMessages(chatId)
    fun chat(chatId: String) = repo.observeChat(chatId)
    fun pinned(chatId: String) = repo.observePinned(chatId)
    fun starHistory(userId: String) = repo.observeStarHistory(userId)
    fun giftCollection(userId: String) = repo.observeGiftCollection(userId)
    fun trades(userId: String) = repo.observeTrades(userId)
    fun blocked() = activeAccount.flatMapLatest { acc ->
        if (acc == null) flowOf(emptyList()) else repo.observeBlocked(acc.id)
    }

    fun loginByUsername(username: String, password: String, onSuccess: () -> Unit) = launch {
        repo.loginByUsername(username, password)
            .onSuccess { onSuccess() }
            .onFailure { _error.value = it.message ?: "Ошибка входа" }
    }

    fun registerByUsername(displayName: String, username: String, password: String, onSuccess: () -> Unit) = launch {
        _loading.value = true
        repo.registerByUsername(displayName, username, password)
            .onSuccess { onSuccess() }
            .onFailure { _error.value = it.message ?: "Ошибка регистрации" }
        _loading.value = false
    }

    val recentCalls = repo.observeRecentCalls().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun register(phone: String, password: String, name: String, onSuccess: () -> Unit) = launch {
        repo.register(phone, password, name).onSuccess { onSuccess() }.onFailure { _error.value = it.message }
    }

    fun sendPhoneCode(
        fullPhone: String,
        smsPermissionGranted: Boolean,
        onSent: () -> Unit,
        onExistingUser: () -> Unit = onSent,
        onNewUser: () -> Unit = onSent,
    ) = launchSms {
        _error.value = null
        _lastSmsCode.value = null
        pendingPhone = fullPhone

        val auth = phoneAuth
        if (auth?.isAvailable == true) {
            auth.sendVerificationCode(fullPhone)
                .onSuccess {
                    smsMode = SmsAuthMode.FIREBASE
                    if (auth.isAutoVerified()) {
                        completeSmsVerification(fullPhone, onExistingUser, onNewUser)
                    } else {
                        onSent()
                    }
                    return@launchSms
                }
        }

        repo.sendSmsViaServer(fullPhone)
            .onSuccess { devCode ->
                smsMode = SmsAuthMode.SERVER
                _lastSmsCode.value = devCode
                onSent()
                return@launchSms
            }

        smsMode = SmsAuthMode.LOCAL
        repo.sendSmsCode(fullPhone, smsPermissionGranted)
            .onSuccess { onScreenCode ->
                _lastSmsCode.value = onScreenCode
                onSent()
            }
            .onFailure { _error.value = it.message ?: "Не удалось отправить СМС" }
    }

    fun verifyPhoneCode(
        fullPhone: String,
        code: String,
        onExisting: () -> Unit,
        onNewUser: () -> Unit,
    ) = launchSms {
        _error.value = null
        when (smsMode) {
            SmsAuthMode.FIREBASE -> {
                val auth = phoneAuth
                    ?: run {
                        _error.value = "Firebase недоступен"
                        return@launchSms
                    }
                auth.verifyCode(code)
                    .onFailure {
                        _error.value = it.message ?: "Неверный код"
                        return@launchSms
                    }
            }
            SmsAuthMode.SERVER -> {
                if (!repo.verifySmsViaServer(fullPhone, code)) {
                    _error.value = "Неверный код. Проверьте СМС и попробуйте снова."
                    return@launchSms
                }
            }
            SmsAuthMode.LOCAL -> {
                if (!repo.verifySmsCode(fullPhone, code)) {
                    _error.value = "Неверный код. Проверьте СМС и попробуйте снова."
                    return@launchSms
                }
            }
        }
        completeSmsVerification(fullPhone, onExisting, onNewUser)
    }

    private suspend fun completeSmsVerification(
        fullPhone: String,
        onExisting: () -> Unit,
        onNewUser: () -> Unit,
    ) {
        if (repo.accountExists(fullPhone)) {
            repo.loginByPhone(fullPhone)
                .onSuccess { onExisting() }
                .onFailure { _error.value = it.message ?: "Ошибка входа" }
        } else {
            pendingPhone = fullPhone
            onNewUser()
        }
    }

    fun resendPhoneCode(fullPhone: String, smsPermissionGranted: Boolean) = launchSms {
        _error.value = null
        _lastSmsCode.value = null
        when (smsMode) {
            SmsAuthMode.FIREBASE -> {
                phoneAuth?.sendVerificationCode(fullPhone, forceResend = true)
                    ?.onFailure { _error.value = it.message ?: "Не удалось отправить СМС повторно" }
            }
            SmsAuthMode.SERVER -> {
                repo.sendSmsViaServer(fullPhone)
                    .onSuccess { _lastSmsCode.value = it }
                    .onFailure { _error.value = it.message ?: "Не удалось отправить СМС повторно" }
            }
            SmsAuthMode.LOCAL -> {
                repo.sendSmsCode(fullPhone, smsPermissionGranted)
                    .onSuccess { _lastSmsCode.value = it }
                    .onFailure { _error.value = it.message ?: "Не удалось отправить СМС повторно" }
            }
        }
    }

    fun completePhoneRegistration(fullPhone: String, name: String, onSuccess: () -> Unit) = launch {
        _error.value = null
        repo.registerByPhone(fullPhone, name)
            .onSuccess { onSuccess() }
            .onFailure { _error.value = it.message ?: "Ошибка регистрации" }
    }

    fun loginWithTelegram(user: com.teleport.messenger.auth.TelegramUser, onSuccess: () -> Unit) = launch {
        _error.value = null
        repo.loginWithTelegram(
            user.id,
            com.teleport.messenger.auth.TelegramAuthHelper.displayName(user),
            user.username,
        )
            .onSuccess { onSuccess() }
            .onFailure { _error.value = it.message ?: "Ошибка входа через Telegram" }
    }

    fun loginWithYandexProfile(yandexId: String, name: String, email: String?, onSuccess: () -> Unit) = launch {
        _error.value = null
        repo.loginWithYandex(yandexId, name, email)
            .onSuccess { onSuccess() }
            .onFailure { _error.value = it.message ?: "Ошибка входа через Яндекс" }
    }

    fun loginWithYandexToken(token: String, onSuccess: () -> Unit) = launch {
        _error.value = null
        YandexAuthHelper.fetchUserInfo(token)
            .onSuccess { info ->
                repo.loginWithYandex(
                    info.id!!,
                    YandexAuthHelper.displayName(info),
                    info.default_email,
                ).onSuccess { onSuccess() }
                    .onFailure { _error.value = it.message ?: "Ошибка входа" }
            }
            .onFailure { _error.value = "Не удалось авторизоваться через Яндекс ID" }
    }

    fun login(phone: String, password: String, onSuccess: () -> Unit) = launch {
        repo.login(phone, password).onSuccess { onSuccess() }.onFailure { _error.value = it.message }
    }

    fun qrLogin(token: String, onSuccess: () -> Unit) = launch {
        repo.qrLogin(token, "Android Device").onSuccess { onSuccess() }.onFailure { _error.value = it.message }
    }

    fun recover(phone: String, password: String, onSuccess: () -> Unit) = launch {
        repo.recover(phone, password).onSuccess { onSuccess() }.onFailure { _error.value = it.message }
    }

    fun logout(onDone: () -> Unit) = launch {
        activeAccount.value?.let { repo.logout(it.id) }
        onDone()
    }

    fun sendText(chatId: String, userId: String, text: String, replyTo: String? = null, hasSpoiler: Boolean = false) = launch {
        repo.sendMessage(chatId, userId, MessageType.TEXT, text, replyToId = replyTo, hasSpoiler = hasSpoiler)
    }

    fun sendMedia(
        chatId: String,
        userId: String,
        type: MessageType,
        mediaUrl: String,
        caption: String = "",
        durationMs: Long = 0L,
        replyTo: String? = null,
    ) = launch {
        repo.sendMessage(chatId, userId, type, caption, mediaUrl, replyToId = replyTo, durationMs = durationMs)
    }

    fun uploadAndSendMedia(
        chatId: String,
        userId: String,
        file: File,
        mime: String,
        type: MessageType,
        caption: String = "",
        durationMs: Long = 0L,
        replyTo: String? = null,
    ) = launch {
        val url = repo.uploadMediaFile(file, mime) ?: file.toURI().toString()
        repo.sendMessage(chatId, userId, type, caption, url, replyToId = replyTo, durationMs = durationMs)
    }

    fun forwardMessage(messageId: String, toChatId: String, senderId: String) = launch {
        repo.forwardMessage(messageId, toChatId, senderId)
    }

    fun syncMessages() = launch {
        repo.syncChatsFromServer()
        repo.syncMessagesFromServer()
    }

    fun editMessage(id: String, text: String) = launch { repo.editMessage(id, text) }
    fun deleteMessage(id: String) = launch { repo.deleteMessage(id) }
    fun pinMessage(chatId: String, msgId: String) = launch { repo.pinMessage(chatId, msgId) }
    fun addReaction(msgId: String, userId: String, emoji: String) = launch { repo.addReaction(msgId, userId, emoji) }
    fun pinChat(id: String, pinned: Boolean) = launch { repo.pinChat(id, pinned) }
    fun archiveChat(id: String, archived: Boolean) = launch { repo.archiveChat(id, archived) }
    fun markRead(chatId: String) = launch { repo.markChatRead(chatId) }
    fun markAllChatsRead() = launch { repo.markAllChatsRead() }

    fun updateProfile(user: UserEntity) = launch { repo.updateProfile(user) }

    fun checkUsername(username: String, excludeId: String, callback: (Boolean) -> Unit) = launch {
        callback(repo.checkUsernameAvailable(username, excludeId))
    }

    fun searchUsers(query: String, callback: (List<UserEntity>) -> Unit) = launch {
        callback(repo.searchUsers(query))
    }

    fun searchMessages(chatId: String, query: String, filter: String, callback: (List<MessageEntity>) -> Unit) = launch {
        callback(repo.searchMessages(chatId, query, filter))
    }

    fun buyPremium(months: Int) = launch {
        activeAccount.value?.id?.let { repo.purchasePremium(it, months) }
    }

    fun buyStars(amount: Long) = launch {
        currentUser().first()?.let { repo.buyStars(it.id, amount) }
    }

    fun sendGift(toUserId: String, giftId: String, chatId: String, callback: (Boolean) -> Unit) = launch {
        val user = currentUser().first() ?: return@launch
        callback(repo.sendGift(user.id, toUserId, giftId, chatId))
    }

    fun buyListing(listingId: String, callback: (Boolean) -> Unit) = launch {
        val user = currentUser().first() ?: return@launch
        callback(repo.buyFromMarketplace(user.id, listingId))
    }

    fun listGift(giftId: String, price: Long) = launch {
        currentUser().first()?.let { repo.listOnMarketplace(it.id, giftId, price) }
    }

    fun updateSettings(settings: AppSettingsEntity) = launch { repo.updateSettings(settings) }
    fun blockUser(userId: String) = launch { activeAccount.value?.id?.let { repo.blockUser(it, userId) } }
    fun unblockUser(userId: String) = launch { activeAccount.value?.id?.let { repo.unblockUser(it, userId) } }
    fun loadUsers(ids: List<String>, callback: (List<UserEntity>) -> Unit) = launch {
        callback(repo.loadUsersByIds(ids))
    }
    fun report(userId: String?, msgId: String?, reason: String) = launch {
        currentUser().first()?.let { repo.report(it.id, userId, msgId, reason, "") }
    }

    fun getContactForChat(chatId: String, currentUserId: String, callback: (UserEntity?) -> Unit) = launch {
        callback(repo.getContactForChat(chatId, currentUserId))
    }

    fun muteChat(chatId: String, muted: Boolean) = launch { repo.muteChat(chatId, muted) }
    fun clearChat(chatId: String) = launch { repo.clearChat(chatId) }

    fun switchAccount(accountId: String, onDone: () -> Unit) = launch {
        repo.switchAccount(accountId)
        onDone()
    }

    fun terminateSession(sessionId: String) = launch { repo.terminateSession(sessionId) }

    fun openPrivateChat(otherUserId: String, callback: (String) -> Unit) = launch {
        val me = currentUser().first() ?: return@launch
        callback(repo.openOrCreatePrivateChat(me.id, otherUserId))
    }

    fun createFolder(name: String, color: Int = 0xFF1565FF.toInt()) = launch {
        activeAccount.value?.id?.let { repo.createFolder(it, name, color) }
    }

    fun moveChatToFolder(chatId: String, folderId: String?) = launch {
        repo.moveChatToFolder(chatId, folderId)
    }

    fun startCall(chatId: String, type: String, isGroup: Boolean = false) = launch {
        currentUser().first()?.let { repo.startCall(chatId, it.id, type, isGroup) }
    }

    fun transcribe(msgId: String, callback: (String?) -> Unit) = launch {
        val user = currentUser().first() ?: return@launch
        callback(repo.transcribeVoice(msgId, user.id))
    }

    fun clearError() { _error.value = null }
    fun setError(message: String) { _error.value = message }

    private fun launchSms(block: suspend () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                block()
            } finally {
                _loading.value = false
            }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try { block() } finally { _loading.value = false }
        }
    }

    class Factory(
        private val repo: TeleportRepository,
        private val phoneAuth: PhoneAuthHandler?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TeleportViewModel(repo, phoneAuth) as T
    }
}

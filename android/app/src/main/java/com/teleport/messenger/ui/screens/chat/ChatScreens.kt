package com.teleport.messenger.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.ChatType
import com.teleport.messenger.data.entity.MessageType
import com.teleport.messenger.ui.components.*
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr
import com.teleport.messenger.ui.theme.TeleportAppTheme
import com.teleport.messenger.viewmodel.TeleportViewModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    vm: TeleportViewModel,
    onChatClick: (String) -> Unit,
    onChats: () -> Unit,
    onContacts: () -> Unit,
    onSettings: () -> Unit,
    onArchive: () -> Unit,
    onSearch: () -> Unit,
    onCalls: () -> Unit = {},
) {
    val chats by vm.chats.collectAsState()
    val folders by vm.folders().collectAsState(initial = emptyList())
    val me by vm.currentUser().collectAsState(initial = null)
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChatListFilter.All) }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val filtered = remember(chats, query, filter, selectedFolderId) {
        filterChatsV6(chats, query, filter).let { list ->
            if (selectedFolderId == null) list else list.filter { it.folderId == selectedFolderId }
        }
    }
    var menuChat by remember { mutableStateOf<com.teleport.messenger.data.entity.ChatEntity?>(null) }
    var onlineByChat by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    LaunchedEffect(filtered.map { it.id }.joinToString(), me?.id) {
        val myId = me?.id ?: return@LaunchedEffect
        val map = mutableMapOf<String, Boolean>()
        filtered.filter { it.type == ChatType.PRIVATE }.take(40).forEach { chat ->
            val user = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                vm.getContactForChat(chat.id, myId) { peer ->
                    if (cont.isActive) cont.resume(peer) {}
                }
            }
            if (user?.isOnline == true) map[chat.id] = true
        }
        onlineByChat = map
    }

    Scaffold(
        containerColor = ChatListV6Palette.Bg,
        bottomBar = {
            AppFloatingBottomNav(
                selected = MainTab.Chats,
                onChats = onChats,
                onContacts = onContacts,
                onSettings = onSettings,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ChatListV6Palette.Bg),
        ) {
            if (selectionMode) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            "${selectedIds.size} выбрано",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChatListV6Palette.TextPrimary,
                        )
                        TextButton(onClick = { selectionMode = false; selectedIds = emptySet() }) {
                            Text(appStr(AppStringKey.DONE), color = ChatListV6Palette.NavActive)
                        }
                    }
                    if (selectedIds.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = {
                                selectedIds.forEach { vm.archiveChat(it, true) }
                                selectionMode = false
                                selectedIds = emptySet()
                            }) { Text("В архив") }
                            TextButton(onClick = {
                                selectedIds.forEach { vm.muteChat(it, true) }
                                selectionMode = false
                                selectedIds = emptySet()
                            }) { Text("Без звука") }
                            TextButton(onClick = {
                                selectedIds.forEach { vm.markRead(it) }
                                selectionMode = false
                                selectedIds = emptySet()
                            }) { Text("Прочитано") }
                        }
                    }
                }
            } else {
                ChatListV6TopBar(
                    query = query,
                    onQueryChange = { query = it },
                    onCompose = onSearch,
                    onArchive = onArchive,
                    onCalls = onCalls,
                )
                ChatListV6Filters(
                    selected = filter,
                    onSelect = {
                        filter = it
                        if (it != ChatListFilter.All) selectedFolderId = null
                    },
                    folders = folders,
                    selectedFolderId = selectedFolderId,
                    onFolderSelect = { id ->
                        selectedFolderId = id
                        if (id != null) filter = ChatListFilter.All
                    },
                )
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 24.dp),
            ) {
                items(filtered.size, key = { filtered[it].id }) { index ->
                    val chat = filtered[index]
                    val selected = chat.id in selectedIds
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        if (selectionMode) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) selectedIds + chat.id else selectedIds - chat.id
                                },
                                modifier = Modifier.padding(start = 4.dp),
                                colors = CheckboxDefaults.colors(checkedColor = ChatListV6Palette.NavActive),
                            )
                        }
                        ChatListV6Item(
                            chat = chat,
                            index = index,
                            isOnline = onlineByChat[chat.id] == true,
                            onClick = {
                                if (selectionMode) {
                                    selectedIds = if (selected) selectedIds - chat.id else selectedIds + chat.id
                                } else {
                                    onChatClick(chat.id)
                                }
                            },
                            onLongClick = if (!selectionMode) {{ menuChat = chat }} else null,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    menuChat?.let { chat ->
        ModalBottomSheet(
            onDismissRequest = { menuChat = null },
            containerColor = ChatListV6Palette.NavBg,
        ) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Text(
                    chat.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ChatListV6Palette.TextPrimary,
                )
                listOf(
                    (if (chat.isPinned) "Открепить" else "Закрепить") to {
                        vm.pinChat(chat.id, !chat.isPinned); menuChat = null
                    },
                    "В архив" to { vm.archiveChat(chat.id, true); menuChat = null },
                    appStr(AppStringKey.SELECT_CHATS) to {
                        selectionMode = true; selectedIds = setOf(chat.id); menuChat = null
                    },
                    appStr(AppStringKey.READ_ALL) to { vm.markAllChatsRead(); menuChat = null },
                ).forEach { (label, action) ->
                    ListItem(
                        headlineContent = { Text(label, color = ChatListV6Palette.TextPrimary) },
                        modifier = Modifier.clickable { action() },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
                folders.forEach { folder ->
                    ListItem(
                        headlineContent = { Text("В папку «${folder.name}»", color = ChatListV6Palette.TextPrimary) },
                        modifier = Modifier.clickable {
                            vm.moveChatToFolder(chat.id, folder.id)
                            menuChat = null
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
                if (folders.isNotEmpty()) {
                    ListItem(
                        headlineContent = { Text("Убрать из папки", color = ChatListV6Palette.TextMuted) },
                        modifier = Modifier.clickable {
                            vm.moveChatToFolder(chat.id, null)
                            menuChat = null
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: TeleportViewModel,
    chatId: String,
    onBack: () -> Unit,
    onInfo: () -> Unit,
    onSearch: () -> Unit,
    onGallery: () -> Unit,
    onCall: (String) -> Unit,
) {
    val chat by vm.chat(chatId).collectAsState(initial = null)
    val messages by vm.messages(chatId).collectAsState(initial = emptyList())
    val chats by vm.chats.collectAsState()
    val user by vm.currentUser().collectAsState(initial = null)
    var text by remember { mutableStateOf("") }
    var showActions by remember { mutableStateOf(false) }
    var showForward by remember { mutableStateOf(false) }
    var showAttach by remember { mutableStateOf(false) }
    var selectedMsg by remember { mutableStateOf<String?>(null) }
    var replyTo by remember { mutableStateOf<com.teleport.messenger.data.entity.MessageEntity?>(null) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var recording by remember { mutableStateOf(false) }
    var sendAsSpoiler by remember { mutableStateOf(false) }
    var contact by remember { mutableStateOf<com.teleport.messenger.data.entity.UserEntity?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val voiceRecorder = remember { com.teleport.messenger.util.VoiceRecorder(context) }

    val pickImage = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        user ?: return@rememberLauncherForActivityResult
        val file = com.teleport.messenger.util.MediaHelper.copyUriToCache(context, uri, ".jpg") ?: return@rememberLauncherForActivityResult
        vm.uploadAndSendMedia(chatId, user!!.id, file, "image/jpeg", MessageType.PHOTO, replyTo = replyTo?.id)
        replyTo = null
        showAttach = false
    }
    val pickVideo = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        user ?: return@rememberLauncherForActivityResult
        val file = com.teleport.messenger.util.MediaHelper.copyUriToCache(context, uri, ".mp4") ?: return@rememberLauncherForActivityResult
        vm.uploadAndSendMedia(chatId, user!!.id, file, "video/mp4", MessageType.VIDEO, replyTo = replyTo?.id)
        replyTo = null
        showAttach = false
    }
    val pickFile = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        user ?: return@rememberLauncherForActivityResult
        val name = runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && idx >= 0) c.getString(idx) else null
            }
        }.getOrNull() ?: "file_${System.currentTimeMillis()}"
        val suffix = if (name.contains('.')) "." + name.substringAfterLast('.') else ".bin"
        val file = com.teleport.messenger.util.MediaHelper.copyUriToCache(context, uri, suffix)
            ?: return@rememberLauncherForActivityResult
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        vm.uploadAndSendMedia(
            chatId, user!!.id, file, mime, MessageType.DOCUMENT,
            replyTo = replyTo?.id, fileName = name,
        )
        replyTo = null
        showAttach = false
    }
    val locationPermission = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            android.widget.Toast.makeText(context, "Нужен доступ к геолокации", android.widget.Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        val u = user ?: return@rememberLauncherForActivityResult
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        val loc = runCatching {
            @Suppress("MissingPermission")
            lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
        }.getOrNull()
        if (loc == null) {
            android.widget.Toast.makeText(context, "Не удалось получить координаты", android.widget.Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        vm.sendLocation(chatId, u.id, loc.latitude, loc.longitude, replyTo?.id)
        replyTo = null
        showAttach = false
    }

    LaunchedEffect(chatId) {
        vm.markRead(chatId)
        vm.syncMessages()
    }
    LaunchedEffect(chatId, user?.id) {
        user?.id?.let { id -> vm.getContactForChat(chatId, id) { contact = it } }
    }

    val chatTitle = remember(chat, contact) {
        contact?.let { com.teleport.messenger.util.PrivacyHelper.displayName(it, false, true) }
            ?: chat?.title ?: "Чат"
    }
    val online = contact?.isOnline == true
    val statusText = when {
        online -> "в сети"
        contact != null -> {
            val raw = com.teleport.messenger.util.PrivacyHelper.onlineStatus(contact!!, false, true, false)
            when (raw) {
                "online" -> "в сети"
                "offline" -> "не в сети"
                else -> raw
            }
        }
        else -> "не в сети"
    }
    val initials = chatTitle.trim().firstOrNull()?.uppercase() ?: "?"
    val settings by vm.settings().collectAsState(initial = null)
    val wallpaperBrush = remember(settings?.chatWallpaperId) {
        com.teleport.messenger.ui.theme.ChatWallpapers
            .find { it.id == (settings?.chatWallpaperId ?: "dark") }
            ?.brush
    }
    val largeFont = settings?.largeChatFont == true
    val bubbleAnim = settings?.bubbleAnimations != false
    var historySeeded by remember(chatId) { mutableStateOf(false) }
    var knownMessageIds by remember(chatId) { mutableStateOf(setOf<String>()) }

    LaunchedEffect(chatId, messages) {
        if (!historySeeded) {
            knownMessageIds = messages.map { it.id }.toSet()
            historySeeded = true
        }
    }

    val micPermission = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            voiceRecorder.start()
            recording = true
        }
    }

    fun sendTextMessage() {
        val u = user ?: return
        if (text.isBlank()) return
        if (editingId != null) {
            vm.editMessage(editingId!!, text.trim())
            editingId = null
        } else {
            vm.sendText(chatId, u.id, text.trim(), replyTo = replyTo?.id, hasSpoiler = sendAsSpoiler)
            replyTo = null
            sendAsSpoiler = false
        }
        text = ""
        scope.launch { listState.animateScrollToItem(0) }
    }

    Column(Modifier.fillMaxSize().background(ConversationPalette.Bg)) {
        ChatDetailTopBar(
            title = chatTitle,
            subtitle = statusText,
            online = online,
            initials = initials,
            onBack = onBack,
            onInfo = onInfo,
            onSearch = onSearch,
            onCall = { onCall("voice") },
            onVideoCall = { onCall("video") },
        )
        replyTo?.let { r ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(ConversationPalette.Card)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Ответ", fontSize = 12.sp, color = ConversationPalette.Accent)
                    Text(r.text.ifEmpty { r.type.name }, maxLines = 1, color = ConversationPalette.TextDim, fontSize = 13.sp)
                }
                IconButton(onClick = { replyTo = null }) {
                    Icon(Icons.Default.Close, null, tint = ConversationPalette.TextDim)
                }
            }
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(
                    if (wallpaperBrush != null) Modifier.background(wallpaperBrush)
                    else Modifier.background(ConversationPalette.Bg),
                ),
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                reverseLayout = true,
                state = listState,
                contentPadding = PaddingValues(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    val idx = messages.indexOf(msg)
                    val older = messages.getOrNull(idx + 1)
                    if (older == null || !isSameDay(msg.createdAt, older.createdAt)) {
                        ChatDateDivider(dateDividerLabel(msg.createdAt))
                    }
                    val replyPreview = msg.replyToId?.let { id -> messages.find { it.id == id } }
                    val isNew = historySeeded && msg.id !in knownMessageIds
                    MessageBubble(
                        message = msg,
                        isOwn = msg.senderId == user?.id,
                        replyTo = replyPreview,
                        onLongClick = {
                            selectedMsg = msg.id
                            showActions = true
                        },
                        largeFont = largeFont,
                        animateEnter = bubbleAnim && isNew,
                    )
                    if (isNew) {
                        LaunchedEffect(msg.id) {
                            knownMessageIds = knownMessageIds + msg.id
                        }
                    }
                }
            }
        }
        ChatInputBar(
            text = text,
            onTextChange = { text = it },
            onAttach = { showAttach = true },
            onSend = { sendTextMessage() },
            enabled = text.isNotBlank() || editingId != null,
        )
    }

    val colors = TeleportAppTheme.colors

    if (showAttach) {
        ModalBottomSheet(
            onDismissRequest = { showAttach = false },
            containerColor = Color(0xFF13131F),
            contentColor = Color(0xFFF0EFFF),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Вложение",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFFF0EFFF),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                ListItem(
                    headlineContent = { Text("Фото") },
                    leadingContent = { Icon(Icons.Default.Image, null, tint = colors.accentBlue) },
                    modifier = Modifier.clickableNoRipple { pickImage.launch("image/*") },
                )
                ListItem(
                    headlineContent = { Text("Видео") },
                    leadingContent = { Icon(Icons.Default.Videocam, null, tint = colors.accentBlue) },
                    modifier = Modifier.clickableNoRipple { pickVideo.launch("video/*") },
                )
                ListItem(
                    headlineContent = { Text("Файл") },
                    leadingContent = { Icon(Icons.Default.AttachFile, null, tint = colors.accentBlue) },
                    modifier = Modifier.clickableNoRipple { pickFile.launch("*/*") },
                )
                ListItem(
                    headlineContent = { Text("Геолокация") },
                    leadingContent = { Icon(Icons.Default.LocationOn, null, tint = colors.accentBlue) },
                    modifier = Modifier.clickableNoRipple {
                        locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                )
                ListItem(
                    headlineContent = { Text("Галерея") },
                    leadingContent = { Icon(Icons.Default.Collections, null, tint = colors.accentBlue) },
                    modifier = Modifier.clickableNoRipple { showAttach = false; onGallery() },
                )
                ListItem(
                    headlineContent = { Text(if (recording) "Остановить запись" else "Голосовое") },
                    leadingContent = { Icon(if (recording) Icons.Default.Stop else Icons.Default.Mic, null, tint = colors.accentBlue) },
                    modifier = Modifier.clickableNoRipple {
                        if (recording) {
                            val u = user ?: return@clickableNoRipple
                            voiceRecorder.stop()?.let { (file, duration) ->
                                vm.uploadAndSendMedia(chatId, u.id, file, "audio/mp4", MessageType.VOICE, durationMs = duration, replyTo = replyTo?.id)
                                replyTo = null
                            }
                            recording = false
                            showAttach = false
                        } else {
                            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO,
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                voiceRecorder.start()
                                recording = true
                            } else {
                                micPermission.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                )
                ListItem(
                    headlineContent = { Text(if (sendAsSpoiler) "Спойлер: вкл" else "Отправить как спойлер") },
                    leadingContent = { Icon(Icons.Default.VisibilityOff, null, tint = colors.accentBlue) },
                    modifier = Modifier.clickableNoRipple {
                        sendAsSpoiler = !sendAsSpoiler
                        showAttach = false
                    },
                )
            }
        }
    }

    if (showActions && selectedMsg != null) {
        ModalBottomSheet(onDismissRequest = { showActions = false }) {
            val msg = messages.find { it.id == selectedMsg }
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf("👍", "❤️", "😂", "🔥", "😮").forEach { emoji ->
                        Text(
                            emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .clickableNoRipple {
                                    val u = user ?: return@clickableNoRipple
                                    msg?.let { vm.addReaction(it.id, u.id, emoji) }
                                    showActions = false
                                }
                                .padding(8.dp),
                        )
                    }
                }
                ListItem(
                    headlineContent = { Text("Ответить") },
                    leadingContent = { Icon(Icons.Default.Reply, null) },
                    modifier = Modifier.clickableNoRipple { replyTo = msg; showActions = false },
                )
                ListItem(
                    headlineContent = { Text("Переслать") },
                    leadingContent = { Icon(Icons.Default.Forward, null) },
                    modifier = Modifier.clickableNoRipple { showForward = true; showActions = false },
                )
                ListItem(
                    headlineContent = { Text("Копировать") },
                    leadingContent = { Icon(Icons.Default.ContentCopy, null) },
                    modifier = Modifier.clickableNoRipple {
                        val copy = msg?.text?.takeIf { it.isNotBlank() }
                            ?: msg?.transcription
                            ?: msg?.fileName
                            ?: msg?.mediaUri
                            ?: ""
                        val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("message", copy))
                        android.widget.Toast.makeText(context, "Скопировано", android.widget.Toast.LENGTH_SHORT).show()
                        showActions = false
                    },
                )
                ListItem(
                    headlineContent = { Text("Закрепить") },
                    leadingContent = { Icon(Icons.Default.PushPin, null) },
                    modifier = Modifier.clickableNoRipple {
                        msg?.let { vm.pinMessage(chatId, it.id) }
                        showActions = false
                    },
                )
                if (msg?.type == MessageType.VOICE) {
                    ListItem(
                        headlineContent = { Text("Расшифровать") },
                        leadingContent = { Icon(Icons.Default.Subtitles, null) },
                        modifier = Modifier.clickableNoRipple {
                            vm.transcribe(msg.id) { result ->
                                android.widget.Toast.makeText(
                                    context,
                                    if (result != null) "Готово" else "Доступно с Premium",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            }
                            showActions = false
                        },
                    )
                }
                if (msg?.senderId == user?.id && msg?.type == MessageType.TEXT) {
                    ListItem(
                        headlineContent = { Text("Редактировать") },
                        leadingContent = { Icon(Icons.Default.Edit, null) },
                        modifier = Modifier.clickableNoRipple {
                            editingId = msg.id
                            text = msg.text
                            showActions = false
                        },
                    )
                }
                ListItem(
                    headlineContent = { Text("Удалить") },
                    leadingContent = { Icon(Icons.Default.Delete, null) },
                    modifier = Modifier.clickableNoRipple { vm.deleteMessage(selectedMsg!!); showActions = false },
                )
            }
        }
    }

    if (showForward && selectedMsg != null) {
        ModalBottomSheet(onDismissRequest = { showForward = false }) {
            Text("Переслать в чат", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(chats.filter { it.id != chatId }) { c ->
                    ListItem(
                        headlineContent = { Text(c.title) },
                        modifier = Modifier.clickableNoRipple {
                            user?.let { vm.forwardMessage(selectedMsg!!, c.id, it.id) }
                            showForward = false
                        },
                    )
                }
            }
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))

@Composable
fun ChatSearchScreen(vm: TeleportViewModel, chatId: String, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("all") }
    var results by remember { mutableStateOf(emptyList<com.teleport.messenger.data.entity.MessageEntity>()) }
    var opened by remember { mutableStateOf<com.teleport.messenger.data.entity.MessageEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Поиск", onBack)
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "all" to "Все",
                "photos" to "Фото",
                "videos" to "Видео",
                "voice" to "Голос",
                "files" to "Файлы",
                "links" to "Ссылки",
            ).forEach { (id, label) ->
                FilterChip(
                    selected = filter == id,
                    onClick = {
                        filter = id
                        vm.searchMessages(chatId, query, id) { results = it }
                    },
                    label = { Text(label) },
                )
            }
        }
        TeleportTextField(
            query,
            {
                query = it
                vm.searchMessages(chatId, query, filter) { results = it }
            },
            "Поиск",
            Modifier.padding(16.dp),
        )
        LazyColumn {
            items(results, key = { it.id }) { msg ->
                ListItem(
                    headlineContent = { Text(msg.text.ifEmpty { msg.type.name }) },
                    supportingContent = {
                        Text(com.teleport.messenger.ui.screens.chat.formatMessageTime(msg.createdAt))
                    },
                    modifier = Modifier.clickable { opened = msg },
                )
            }
        }
    }
    opened?.let { msg ->
        AlertDialog(
            onDismissRequest = { opened = null },
            title = { Text(msg.type.name) },
            text = {
                Text(
                    buildString {
                        append(com.teleport.messenger.ui.screens.chat.formatMessageTime(msg.createdAt))
                        append("\n\n")
                        append(msg.text.ifEmpty { msg.mediaUri ?: "—" })
                    },
                )
            },
            confirmButton = { TextButton(onClick = { opened = null }) { Text("Закрыть") } },
        )
    }
}

@Composable
fun ChatGalleryScreen(vm: TeleportViewModel, chatId: String, onBack: () -> Unit) {
    var media by remember { mutableStateOf(emptyList<com.teleport.messenger.data.entity.MessageEntity>()) }
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(chatId) {
        vm.searchMessages(chatId, "", "photos") { photos ->
            vm.searchMessages(chatId, "", "videos") { videos ->
                media = (photos + videos).sortedByDescending { it.createdAt }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Галерея", onBack)
        if (media.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет медиа", color = TeleportAppTheme.colors.textMuted)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(media.chunked(3), key = { row -> row.joinToString { it.id } }) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { msg ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TeleportAppTheme.colors.inputBg)
                                    .clickable {
                                        msg.mediaUri?.let { uri ->
                                            runCatching {
                                                val view = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                                if (msg.type.name.contains("VIDEO")) {
                                                    val file = java.io.File(uri.removePrefix("file://"))
                                                    val data = if (file.exists()) {
                                                        androidx.core.content.FileProvider.getUriForFile(
                                                            context,
                                                            "${context.packageName}.fileprovider",
                                                            file,
                                                        )
                                                    } else android.net.Uri.parse(uri)
                                                    view.setDataAndType(data, "video/*")
                                                    view.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                } else {
                                                    view.setDataAndType(android.net.Uri.parse(uri), "image/*")
                                                    view.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(view)
                                            }
                                        }
                                    },
                            ) {
                                if (msg.type.name.contains("VIDEO")) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.PlayArrow, null, tint = TeleportAppTheme.colors.accentBlue)
                                    }
                                } else {
                                    coil.compose.AsyncImage(
                                        model = msg.mediaUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                        repeat(3 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveScreen(vm: TeleportViewModel, onBack: () -> Unit, onChatClick: (String) -> Unit) {
    val chats by vm.archivedChats.collectAsState()
    var menuChat by remember { mutableStateOf<com.teleport.messenger.data.entity.ChatEntity?>(null) }
    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Архив", onBack)
        LazyColumn {
            items(chats, key = { it.id }) { chat ->
                ChatListItem(
                    chat,
                    { onChatClick(chat.id) },
                    onLongClick = { menuChat = chat },
                )
            }
        }
    }
    menuChat?.let { chat ->
        AlertDialog(
            onDismissRequest = { menuChat = null },
            title = { Text(chat.title) },
            text = {
                TextButton(onClick = { vm.archiveChat(chat.id, false); menuChat = null }) {
                    Text("Вернуть из архива")
                }
            },
            confirmButton = { TextButton(onClick = { menuChat = null }) { Text("Закрыть") } },
        )
    }
}

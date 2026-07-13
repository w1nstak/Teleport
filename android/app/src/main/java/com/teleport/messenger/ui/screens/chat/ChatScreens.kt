package com.teleport.messenger.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@Composable
fun ChatListScreen(
    vm: TeleportViewModel,
    onChatClick: (String) -> Unit,
    onChats: () -> Unit,
    onContacts: () -> Unit,
    onSettings: () -> Unit,
    onArchive: () -> Unit,
    onSearch: () -> Unit,
) {
    val chats by vm.chats.collectAsState()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChatListFilter.All) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val filtered = remember(chats, query, filter) { filterChatsV6(chats, query, filter) }
    var menuChat by remember { mutableStateOf<com.teleport.messenger.data.entity.ChatEntity?>(null) }

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
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        appStr(AppStringKey.SELECT_CHATS),
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = ChatListV6Palette.TextPrimary,
                    )
                    TextButton(onClick = { selectionMode = false; selectedIds = emptySet() }) {
                        Text(appStr(AppStringKey.DONE), color = ChatListV6Palette.NavActive)
                    }
                }
            } else {
                ChatListV6TopBar(
                    query = query,
                    onQueryChange = { query = it },
                    onCompose = onSearch,
                )
                ChatListV6Filters(filter) { filter = it }
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
        val folders by vm.folders().collectAsState(initial = emptyList())
        AlertDialog(
            onDismissRequest = { menuChat = null },
            title = { Text(chat.title) },
            text = {
                Column {
                    TextButton(onClick = { vm.pinChat(chat.id, !chat.isPinned); menuChat = null }) {
                        Text(if (chat.isPinned) "Открепить" else "Закрепить")
                    }
                    TextButton(onClick = { vm.archiveChat(chat.id, true); menuChat = null }) {
                        Text("В архив")
                    }
                    TextButton(onClick = { selectionMode = true; menuChat = null }) {
                        Text(appStr(AppStringKey.SELECT_CHATS))
                    }
                    TextButton(onClick = { vm.markAllChatsRead(); menuChat = null }) {
                        Text(appStr(AppStringKey.READ_ALL))
                    }
                    if (folders.isNotEmpty()) {
                        folders.forEach { folder ->
                            TextButton(onClick = {
                                vm.moveChatToFolder(chat.id, folder.id)
                                menuChat = null
                            }) {
                                Text("→ ${folder.name}")
                            }
                        }
                        TextButton(onClick = { vm.moveChatToFolder(chat.id, null); menuChat = null }) {
                            Text("Убрать из папки")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { menuChat = null }) { Text("Закрыть") } },
        )
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

    val colors = TeleportAppTheme.colors

    Column(Modifier.fillMaxSize().background(colors.screenBg)) {
        ChatDetailTopBar(
            chatTitle,
            onBack = onBack,
            onInfo = onInfo,
            onSearch = onSearch,
            onCall = { onCall("voice") },
            onVideoCall = { onCall("video") },
        )
        replyTo?.let { r ->
            Surface(color = colors.cardBg) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Ответ", style = MaterialTheme.typography.labelMedium, color = colors.accentBlue)
                        Text(r.text.ifEmpty { r.type.name }, maxLines = 1, color = colors.textMuted)
                    }
                    IconButton(onClick = { replyTo = null }) { Icon(Icons.Default.Close, null) }
                }
            }
        }
        Box(Modifier.weight(1f).background(colors.screenBg)) {
            LazyColumn(
                Modifier.fillMaxSize(),
                reverseLayout = true,
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    val idx = messages.indexOf(msg)
                    val older = messages.getOrNull(idx + 1)
                    if (older == null || !isSameDay(msg.createdAt, older.createdAt)) {
                        ChatDateDivider(dateDividerLabel(msg.createdAt))
                    }
                    val replyPreview = msg.replyToId?.let { id -> messages.find { it.id == id } }
                    MessageBubble(msg, msg.senderId == user?.id, replyPreview, {
                        selectedMsg = msg.id
                        showActions = true
                    })
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

    if (showAttach) {
        ModalBottomSheet(onDismissRequest = { showAttach = false }) {
            Column(Modifier.padding(16.dp)) {
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
                            voiceRecorder.start()
                            recording = true
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

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Поиск", onBack)
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all" to "Все", "photos" to "Фото", "videos" to "Видео", "voice" to "Голос", "files" to "Файлы", "links" to "Ссылки", "date" to "Дата").forEach { (id, label) ->
                FilterChip(selected = filter == id, onClick = { filter = id; vm.searchMessages(chatId, query, id) { results = it } }, label = { Text(label) })
            }
        }
        TeleportTextField(query, { query = it; vm.searchMessages(chatId, query, filter) { results = it } }, "Поиск", Modifier.padding(16.dp))
        LazyColumn {
            items(results) { msg ->
                ListItem(headlineContent = { Text(msg.text.ifEmpty { msg.type.name }) }, supportingContent = { Text(msg.createdAt.toString()) })
            }
        }
    }
}

@Composable
fun ChatGalleryScreen(vm: TeleportViewModel, chatId: String, onBack: () -> Unit) {
    var media by remember { mutableStateOf(emptyList<com.teleport.messenger.data.entity.MessageEntity>()) }
    LaunchedEffect(chatId) { vm.searchMessages(chatId, "", "photos") { media = it } }

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Галерея", onBack)
        LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(media) { msg ->
                Card { Text("📷 ${msg.text.ifEmpty { "Media" }}", Modifier.padding(16.dp)) }
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

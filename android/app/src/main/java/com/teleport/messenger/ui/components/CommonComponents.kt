package com.teleport.messenger.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.ChatEntity
import com.teleport.messenger.data.entity.ChatType
import com.teleport.messenger.data.entity.MessageEntity
import com.teleport.messenger.data.entity.MessageType
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr
import com.teleport.messenger.ui.theme.LocalChatAccent
import com.teleport.messenger.ui.theme.TeleportAppTheme
import com.teleport.messenger.ui.theme.TeleportMotion
import com.teleport.messenger.ui.theme.PremiumGold
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun TeleportAvatar(
    name: String,
    isPremium: Boolean = false,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 52.dp,
) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    val colors = listOf(0xFF3D5A80, 0xFF5C4D7D, 0xFF2F4858, 0xFF4A6670, 0xFF6B5B95, 0xFF355070)
    val color = Color(colors[name.hashCode().absoluteValue % colors.size].toInt())
    Box(modifier = modifier.size(size)) {
        Box(
            Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (isOnline) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(TeleportAppTheme.colors.cardBg)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(TeleportAppTheme.colors.onlineGreen),
            )
        }
        if (isPremium) {
            Icon(
                Icons.Default.Star, null,
                modifier = Modifier.align(Alignment.TopEnd).size(14.dp),
                tint = PremiumGold,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    chat: ChatEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isOnline: Boolean = false,
) {
    val time = remember(chat.lastMessageTime) {
        com.teleport.messenger.ui.screens.chat.formatRelativeTime(chat.lastMessageTime)
    }
    Row(
        modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeleportAvatar(chat.title, isOnline = isOnline)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                chat.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TeleportAppTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                chat.lastMessagePreview.ifEmpty { appStr(AppStringKey.NO_MESSAGES) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TeleportAppTheme.colors.textMuted,
                fontSize = 14.sp,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(time, fontSize = 12.sp, color = TeleportAppTheme.colors.textMuted)
            if (chat.unreadCount > 0) {
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                        .clip(CircleShape)
                        .background(TeleportAppTheme.colors.accentBlue)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${chat.unreadCount}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    isOwn: Boolean,
    replyTo: MessageEntity? = null,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    largeFont: Boolean = false,
    animateEnter: Boolean = false,
) {
    var spoilerRevealed by remember { mutableStateOf(!message.hasSpoiler) }
    val palette = com.teleport.messenger.ui.screens.chat.ConversationPalette
    val accent = LocalChatAccent.current
    val bodySize = if (largeFont) 15.5.sp else 13.5.sp
    val time = remember(message.createdAt) {
        com.teleport.messenger.ui.screens.chat.formatMessageTime(message.createdAt)
    }
    val shape = if (isOwn) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp)
    }
    val fg = if (isOwn) Color.White else palette.Text

    val alpha = remember(message.id) { Animatable(if (animateEnter) 0f else 1f) }
    val offsetY = remember(message.id) { Animatable(if (animateEnter) 18f else 0f) }
    LaunchedEffect(message.id, animateEnter) {
        if (animateEnter) {
            launch {
                alpha.animateTo(1f, animationSpec = TeleportMotion.normal())
            }
            launch {
                offsetY.animateTo(0f, animationSpec = TeleportMotion.snappy())
            }
        } else {
            alpha.snapTo(1f)
            offsetY.snapTo(0f)
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha.value
                translationY = offsetY.value
            }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start,
    ) {
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .then(
                    if (isOwn) {
                        Modifier
                            .shadow(
                                elevation = 6.dp,
                                shape = shape,
                                ambientColor = accent.primary.copy(0.28f),
                                spotColor = accent.primary.copy(0.28f),
                            )
                            .clip(shape)
                            .background(
                                Brush.linearGradient(listOf(accent.primary, accent.secondary)),
                            )
                    } else {
                        Modifier
                            .clip(shape)
                            .background(palette.Card)
                            .border(1.dp, palette.Hairline, shape)
                    },
                )
                .clickable { onLongClick() }
                .padding(horizontal = 13.dp, vertical = 10.dp),
        ) {
            Column {
                if (message.forwardFromId != null) {
                    Text(
                        "Переслано",
                        fontSize = 11.sp,
                        color = fg.copy(0.7f),
                        fontFamily = com.teleport.messenger.ui.theme.ManropeFontFamily,
                    )
                }
                replyTo?.let {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(fg.copy(alpha = 0.12f))
                            .padding(8.dp),
                    ) {
                        Text(
                            it.text.ifEmpty { it.type.name },
                            maxLines = 2,
                            fontSize = 12.sp,
                            color = fg.copy(0.85f),
                            fontFamily = com.teleport.messenger.ui.theme.ManropeFontFamily,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
                when (message.type) {
                    MessageType.TEXT -> {
                        if (message.hasSpoiler && !spoilerRevealed) {
                            Text(
                                "Спойлер — нажмите",
                                color = fg,
                                fontSize = bodySize,
                                fontFamily = com.teleport.messenger.ui.theme.ManropeFontFamily,
                                modifier = Modifier.clickable { spoilerRevealed = true },
                            )
                        } else {
                            Text(
                                message.text,
                                color = fg,
                                fontSize = bodySize,
                                lineHeight = 20.sp,
                                fontFamily = com.teleport.messenger.ui.theme.ManropeFontFamily,
                            )
                        }
                    }
                    MessageType.PHOTO -> {
                        message.mediaUri?.let { uri ->
                            val ctx = androidx.compose.ui.platform.LocalContext.current
                            coil.compose.AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        runCatching {
                                            ctx.startActivity(
                                                android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    setDataAndType(android.net.Uri.parse(uri), "image/*")
                                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                },
                                            )
                                        }
                                    },
                            )
                        } ?: Text("Фото", color = fg, fontSize = 14.sp)
                        if (message.text.isNotEmpty()) {
                            Text(message.text, color = fg, fontSize = 13.5.sp)
                        }
                    }
                    MessageType.VOICE -> {
                        VoiceMessageContent(message.durationMs, isOwn, message.mediaUri)
                        message.transcription?.takeIf { it.isNotBlank() }?.let { text ->
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text,
                                color = fg.copy(0.85f),
                                fontSize = 12.5.sp,
                                fontFamily = com.teleport.messenger.ui.theme.ManropeFontFamily,
                            )
                        }
                    }
                    MessageType.VIDEO, MessageType.VIDEO_NOTE -> VideoMessageContent(message.mediaUri, isOwn)
                    MessageType.GIF -> Text("GIF", color = fg, fontSize = 14.sp)
                    MessageType.DOCUMENT, MessageType.FILE -> {
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        Row(
                            Modifier.clickable {
                                val uri = message.mediaUri ?: return@clickable
                                runCatching {
                                    ctx.startActivity(
                                        android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            setDataAndType(android.net.Uri.parse(uri), "*/*")
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        },
                                    )
                                }
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.InsertDriveFile, null, tint = fg, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(message.fileName ?: "Файл", color = fg, fontSize = 14.sp)
                        }
                    }
                    MessageType.MUSIC -> Text(message.text.ifEmpty { "Аудио" }, color = fg, fontSize = 14.sp)
                    MessageType.CONTACT -> Text(message.contactName ?: "Контакт", color = fg, fontSize = 14.sp)
                    MessageType.LOCATION -> {
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        val label = if (message.latitude != null && message.longitude != null) {
                            "📍 ${"%.4f".format(message.latitude)}, ${"%.4f".format(message.longitude)}"
                        } else {
                            message.text.ifEmpty { "Местоположение" }
                        }
                        Text(
                            label,
                            color = fg,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                val lat = message.latitude ?: return@clickable
                                val lon = message.longitude ?: return@clickable
                                runCatching {
                                    ctx.startActivity(
                                        android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse("geo:$lat,$lon?q=$lat,$lon"),
                                        ),
                                    )
                                }
                            },
                        )
                    }
                    MessageType.GIFT -> Text(message.text.ifEmpty { "Подарок" }, color = fg, fontSize = 14.sp)
                    else -> Text(message.text.ifEmpty { message.type.name }, color = fg, fontSize = 13.5.sp)
                }
                if (message.isEdited) {
                    Text(
                        "изменено",
                        fontSize = 10.sp,
                        color = fg.copy(0.7f),
                        fontFamily = com.teleport.messenger.ui.theme.ManropeFontFamily,
                    )
                }
                Text(
                    time,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    fontSize = 10.sp,
                    color = fg.copy(0.65f),
                    fontFamily = com.teleport.messenger.ui.theme.ManropeFontFamily,
                )
            }
        }
    }
}

@Composable
private fun VoiceMessageContent(durationMs: Long, isOwn: Boolean, mediaUri: String?) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var playing by remember { mutableStateOf(false) }
    val player = remember { android.media.MediaPlayer() }
    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                if (player.isPlaying) player.stop()
                player.release()
            }
        }
    }
    val tint = if (isOwn) Color.White else TeleportAppTheme.colors.accentBlue
    val mins = durationMs / 60_000
    val secs = (durationMs / 1000) % 60
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledIconButton(
            onClick = {
                if (mediaUri.isNullOrBlank()) return@FilledIconButton
                if (playing) {
                    player.stop()
                    player.reset()
                    playing = false
                } else {
                    runCatching {
                        player.reset()
                        player.setDataSource(
                            if (mediaUri.startsWith("/")) mediaUri
                            else java.io.File(context.cacheDir, mediaUri.substringAfterLast('/')).absolutePath.let { path ->
                                if (java.io.File(path).exists()) path else mediaUri
                            },
                        )
                        player.prepare()
                        player.start()
                        playing = true
                        player.setOnCompletionListener { playing = false }
                    }
                }
            },
            modifier = Modifier.size(36.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isOwn) Color.White.copy(0.2f) else TeleportAppTheme.colors.accentBlue.copy(0.12f),
            ),
        ) {
            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(12) { i ->
                Box(
                    Modifier
                        .width(3.dp)
                        .height((8 + (i % 4) * 4).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(tint.copy(alpha = 0.5f + (i % 3) * 0.15f)),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(String.format("%02d:%02d", mins, secs), color = tint, fontSize = 13.sp)
    }
}

@Composable
private fun VideoMessageContent(mediaUri: String?, isOwn: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tint = if (isOwn) Color.White else TeleportAppTheme.colors.accentBlue
    if (mediaUri.isNullOrBlank()) {
        Text("Видео", color = tint, fontSize = 14.sp)
        return
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(0.4f))
            .clickable {
                runCatching {
                    val file = java.io.File(mediaUri.removePrefix("file://"))
                    val uri = if (mediaUri.startsWith("content:") || mediaUri.startsWith("http")) {
                        android.net.Uri.parse(mediaUri)
                    } else if (file.exists()) {
                        androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file,
                        )
                    } else {
                        android.net.Uri.parse(mediaUri)
                    }
                    context.startActivity(
                        android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "video/*")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.PlayArrow, "Смотреть", tint = Color.White, modifier = Modifier.size(48.dp))
    }
}

@Composable
fun PremiumBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = PremiumGold.copy(0.2f),
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Premium", style = MaterialTheme.typography.labelMedium, color = PremiumGold, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeleportTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
fun TeleportTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, singleLine: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
fun TeleportButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
    ) { Text(text) }
}

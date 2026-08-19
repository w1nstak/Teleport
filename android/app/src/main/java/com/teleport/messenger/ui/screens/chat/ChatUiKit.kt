package com.teleport.messenger.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr
import com.teleport.messenger.ui.theme.LocalChatAccent
import com.teleport.messenger.ui.theme.ManropeFontFamily
import com.teleport.messenger.ui.theme.TeleportAppTheme
import com.teleport.messenger.ui.theme.UnboundedFontFamily

/** Colors from messenger_conversation.html */
object ConversationPalette {
    val Bg = Color(0xFF0E0D12)
    val Card = Color(0xFF17151C)
    val Accent = Color(0xFF5B5BF0)
    val Accent2 = Color(0xFF8B5CF6)
    val Online = Color(0xFF4ADE80)
    val Text = Color(0xFFF4F2FA)
    val TextDim = Color(0xFF9B96A8)
    val Hairline = Color(0xFF2A2732)
    val IconBtnBg = Color(0x0DFFFFFF)
    val IconBtnBorder = Color(0x14FFFFFF)
    val AccentGradient = Brush.linearGradient(listOf(Accent, Accent2))
}

@Composable
fun chatAccentGradient(): Brush {
    val accent = LocalChatAccent.current
    return Brush.linearGradient(listOf(accent.primary, accent.secondary))
}

@Composable
fun chatAccentPrimary(): Color = LocalChatAccent.current.primary


@Composable
internal fun ChatMessagesHeader(
    title: String,
    selectionMode: Boolean = false,
    onDone: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    showSearchField: Boolean = false,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
) {
    val colors = TeleportAppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.screenBg)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            if (selectionMode && onDone != null) {
                TextButton(onClick = onDone) {
                    Text(appStr(AppStringKey.DONE), color = colors.accentBlue, fontSize = 17.sp)
                }
            } else if (onMenu != null) {
                androidx.compose.material3.IconButton(onClick = onMenu) {
                    Icon(Icons.Filled.MoreHoriz, null, tint = colors.accentBlue, modifier = Modifier.size(28.dp))
                }
            }
        }
        if (showSearchField) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(appStr(AppStringKey.SEARCH), color = colors.textMuted) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = colors.textMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = colors.inputBg,
                    unfocusedContainerColor = colors.inputBg,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatListMenuSheet(
    onDismiss: () -> Unit,
    onSearch: () -> Unit,
    onArchive: () -> Unit,
    onReadAll: () -> Unit,
    onSelectChats: () -> Unit,
) {
    val colors = TeleportAppTheme.colors
    val items = listOf(
        Triple(Icons.Filled.Search, AppStringKey.SEARCH, onSearch),
        Triple(Icons.Filled.Archive, AppStringKey.ARCHIVE, onArchive),
        Triple(Icons.Filled.DoneAll, AppStringKey.READ_ALL, onReadAll),
        Triple(Icons.Filled.CheckCircle, AppStringKey.SELECT_CHATS, onSelectChats),
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.textMuted) },
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            items.forEach { (icon, key, action) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            action()
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon, null, tint = colors.accentBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(appStr(key), fontSize = 17.sp, color = colors.textPrimary)
                }
            }
        }
    }
}

@Composable
internal fun ChatDetailTopBar(
    title: String,
    subtitle: String,
    online: Boolean,
    initials: String,
    onBack: () -> Unit,
    onInfo: () -> Unit,
    onCall: () -> Unit = {},
    onSearch: () -> Unit = {},
    onVideoCall: () -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(ConversationPalette.Bg)
            .statusBarsPadding()
            .drawBehind {
                val y = size.height - 0.5f
                drawLine(
                    color = ConversationPalette.Hairline,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = ConversationPalette.TextDim,
                modifier = Modifier.size(19.dp),
            )
        }

        Row(
            Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onInfo,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.size(38.dp)) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(13.dp))
                        .background(chatAccentGradient()),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initials.take(1).ifEmpty { "?" }.uppercase(),
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White,
                    )
                }
                if (online) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(10.dp)
                            .border(2.dp, ConversationPalette.Bg, CircleShape)
                            .clip(CircleShape)
                            .background(ConversationPalette.Online),
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.5.sp,
                    color = ConversationPalette.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.5.sp,
                    color = if (online) ConversationPalette.Online else ConversationPalette.TextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(ConversationPalette.IconBtnBg)
                .border(1.dp, ConversationPalette.IconBtnBorder, RoundedCornerShape(11.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCall,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Phone,
                contentDescription = "Звонок",
                tint = ConversationPalette.TextDim,
                modifier = Modifier.size(16.dp),
            )
        }
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(ConversationPalette.IconBtnBg)
                .border(1.dp, ConversationPalette.IconBtnBorder, RoundedCornerShape(11.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onVideoCall,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Videocam,
                contentDescription = "Видеозвонок",
                tint = ConversationPalette.TextDim,
                modifier = Modifier.size(16.dp),
            )
        }
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(ConversationPalette.IconBtnBg)
                .border(1.dp, ConversationPalette.IconBtnBorder, RoundedCornerShape(11.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearch,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Поиск",
                tint = ConversationPalette.TextDim,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
internal fun ChatDateDivider(label: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontFamily = ManropeFontFamily,
            fontSize = 11.sp,
            color = ConversationPalette.TextDim,
        )
    }
}

@Composable
internal fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
) {
    val shape = RoundedCornerShape(22.dp)
    val sendEnabled = enabled && text.isNotBlank()
    Row(
        Modifier
            .fillMaxWidth()
            .background(ConversationPalette.Bg)
            .drawBehind {
                drawLine(
                    color = ConversationPalette.Hairline,
                    start = Offset(0f, 0.5f),
                    end = Offset(size.width, 0.5f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAttach,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.AttachFile,
                contentDescription = "Вложение",
                tint = ConversationPalette.TextDim,
                modifier = Modifier.size(20.dp),
            )
        }

        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 42.dp)
                .clip(shape)
                .background(ConversationPalette.Card)
                .border(1.dp, ConversationPalette.Hairline, shape)
                .padding(horizontal = 16.dp, vertical = 11.dp),
            textStyle = TextStyle(
                fontFamily = ManropeFontFamily,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = ConversationPalette.Text,
            ),
            maxLines = 4,
            cursorBrush = SolidColor(chatAccentPrimary()),
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            "Сообщение…",
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.5.sp,
                            color = ConversationPalette.TextDim,
                        )
                    }
                    inner()
                }
            },
        )

        val accent = LocalChatAccent.current
        Box(
            Modifier
                .size(38.dp)
                .shadow(
                    elevation = if (sendEnabled) 8.dp else 0.dp,
                    shape = RoundedCornerShape(13.dp),
                    ambientColor = accent.primary.copy(0.35f),
                    spotColor = accent.primary.copy(0.35f),
                )
                .clip(RoundedCornerShape(13.dp))
                .background(
                    if (sendEnabled) chatAccentGradient()
                    else Brush.linearGradient(listOf(Color(0xFF2A2732), Color(0xFF2A2732))),
                )
                .clickable(enabled = sendEnabled, onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "Отправить",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    return when {
        minutes < 1 -> "сейчас"
        minutes < 60 -> "$minutes мин"
        minutes < 24 * 60 -> "${minutes / 60} ч"
        else -> {
            val days = minutes / (24 * 60)
            if (days == 1L) "вчера" else "$days д"
        }
    }
}

fun formatMessageTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = timestamp
    return String.format(
        "%02d:%02d",
        cal.get(java.util.Calendar.HOUR_OF_DAY),
        cal.get(java.util.Calendar.MINUTE),
    )
}

fun isSameDay(a: Long, b: Long): Boolean {
    if (a <= 0 || b <= 0) return false
    val ca = java.util.Calendar.getInstance().apply { timeInMillis = a }
    val cb = java.util.Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(java.util.Calendar.YEAR) == cb.get(java.util.Calendar.YEAR) &&
        ca.get(java.util.Calendar.DAY_OF_YEAR) == cb.get(java.util.Calendar.DAY_OF_YEAR)
}

fun dateDividerLabel(timestamp: Long): String {
    val today = java.util.Calendar.getInstance()
    val msg = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    return when {
        today.get(java.util.Calendar.YEAR) == msg.get(java.util.Calendar.YEAR) &&
            today.get(java.util.Calendar.DAY_OF_YEAR) == msg.get(java.util.Calendar.DAY_OF_YEAR) -> "Сегодня"
        today.get(java.util.Calendar.DAY_OF_YEAR) - msg.get(java.util.Calendar.DAY_OF_YEAR) == 1 -> "Вчера"
        else -> java.text.SimpleDateFormat("d MMMM", java.util.Locale("ru")).format(java.util.Date(timestamp))
    }
}

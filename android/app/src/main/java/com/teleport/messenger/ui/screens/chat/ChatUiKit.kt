package com.teleport.messenger.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr
import com.teleport.messenger.ui.theme.TeleportAppTheme

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
                IconButton(onClick = onMenu) {
                    Icon(Icons.Default.MoreHoriz, null, tint = colors.accentBlue, modifier = Modifier.size(28.dp))
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
                leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.textMuted) },
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
        Triple(Icons.Default.Search, AppStringKey.SEARCH, onSearch),
        Triple(Icons.Default.Archive, AppStringKey.ARCHIVE, onArchive),
        Triple(Icons.Default.DoneAll, AppStringKey.READ_ALL, onReadAll),
        Triple(Icons.Default.CheckCircle, AppStringKey.SELECT_CHATS, onSelectChats),
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
    onBack: () -> Unit,
    onInfo: () -> Unit,
    onSearch: () -> Unit = {},
    onCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
) {
    val colors = TeleportAppTheme.colors
    Surface(color = colors.cardBg, shadowElevation = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Назад", tint = colors.accentBlue)
            }
            Text(
                title,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            IconButton(onClick = onCall) {
                Icon(Icons.Default.Call, "Звонок", tint = colors.accentBlue, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = onVideoCall) {
                Icon(Icons.Default.Videocam, "Видео", tint = colors.accentBlue, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, "Поиск", tint = colors.accentBlue, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = onInfo) {
                Surface(shape = CircleShape, color = colors.infoButtonBg) {
                    Icon(
                        Icons.Default.Info,
                        "Инфо",
                        modifier = Modifier.padding(8.dp).size(20.dp),
                        tint = colors.accentBlue,
                    )
                }
            }
        }
    }
}

@Composable
internal fun ChatDateDivider(label: String) {
    val colors = TeleportAppTheme.colors
    Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = colors.dateDividerBg, shape = RoundedCornerShape(12.dp)) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                color = colors.textMuted,
                fontSize = 12.sp,
            )
        }
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
    val colors = TeleportAppTheme.colors
    Surface(color = colors.cardBg, shadowElevation = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = onAttach,
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.accentBlue),
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(appStr(AppStringKey.MESSAGE_PLACEHOLDER), color = colors.textMuted) },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    Icon(Icons.Default.EmojiEmotions, null, tint = colors.textMuted, modifier = Modifier.size(22.dp))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = colors.inputBg,
                    unfocusedContainerColor = colors.inputBg,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                ),
            )
            if (text.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = onSend,
                    enabled = enabled,
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.accentBlue),
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                }
            }
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
    val hour = cal.get(java.util.Calendar.HOUR)
    val minute = cal.get(java.util.Calendar.MINUTE)
    val amPm = if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
    val h = if (hour == 0) 12 else hour
    return String.format("%d:%02d %s", h, minute, amPm)
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

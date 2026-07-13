package com.teleport.messenger.ui.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.ChatEntity
import com.teleport.messenger.data.entity.ChatType
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr
import kotlin.math.absoluteValue

object ChatListV6Palette {
    val Bg = Color(0xFF0A0A12)
    val NavBg = Color(0xFF0D0D16)
    val NavBorder = Color(0xFF1C1C2C)
    val NavActive = Color(0xFF5FA8FF)
    val NavInactive = Color(0xFF5C5A88)
    val FieldBg = Color(0x80161623)
    val FieldBorder = Color(0xFF2A2A45)
    val TextPrimary = Color(0xFFF0EFFF)
    val TextMuted = Color(0xFF8280B4)
    val Placeholder = Color(0xFF6B69A0)
    val Online = Color(0xFF2ED974)
    val ComposeStart = Color(0xFF3E8EFF)
    val ComposeEnd = Color(0xFF2255E0)
    val ChipActiveStart = Color(0xFF3E8EFF)
    val ChipActiveEnd = Color(0xFF2A5FE0)
    val BadgeStart = Color(0xFF5FA8FF)
    val BadgeEnd = Color(0xFF2E5FE0)
}

enum class ChatListFilter { All, Unread, Groups }

private val avatarGradients = listOf(
    listOf(Color(0xFF5FA8FF), Color(0xFF2E5FE0)),
    listOf(Color(0xFF4F97FF), Color(0xFF3350D0)),
    listOf(Color(0xFF6FB3FF), Color(0xFF3E6EE8)),
    listOf(Color(0xFF57A0FF), Color(0xFF2A55D6)),
    listOf(Color(0xFF7CBBFF), Color(0xFF4670E8)),
    listOf(Color(0xFF4A93FF), Color(0xFF2848C8)),
)

fun formatChatListTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val now = java.util.Calendar.getInstance()
    val msg = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val sameDay = now.get(java.util.Calendar.YEAR) == msg.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == msg.get(java.util.Calendar.DAY_OF_YEAR)
    if (sameDay) {
        return java.text.SimpleDateFormat("HH:mm", java.util.Locale("ru")).format(java.util.Date(timestamp))
    }
    val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
    if (yesterday.get(java.util.Calendar.YEAR) == msg.get(java.util.Calendar.YEAR) &&
        yesterday.get(java.util.Calendar.DAY_OF_YEAR) == msg.get(java.util.Calendar.DAY_OF_YEAR)
    ) {
        return "вчера"
    }
    val days = arrayOf("вс", "пн", "вт", "ср", "чт", "пт", "сб")
    val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
    if (timestamp >= weekAgo) {
        return days[msg.get(java.util.Calendar.DAY_OF_WEEK) - 1]
    }
    return java.text.SimpleDateFormat("dd.MM", java.util.Locale("ru")).format(java.util.Date(timestamp))
}

private fun chatInitials(title: String): String {
    val parts = title.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        parts.size == 1 -> parts[0].first().uppercase()
        else -> "?"
    }
}

@Composable
fun ChatListV6TopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCompose: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(ChatListV6Palette.FieldBg)
                .border(1.dp, ChatListV6Palette.FieldBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, null, tint = ChatListV6Palette.Placeholder, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    color = ChatListV6Palette.TextPrimary,
                ),
                singleLine = true,
                cursorBrush = SolidColor(ChatListV6Palette.NavActive),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text("Поиск", fontSize = 14.sp, color = ChatListV6Palette.Placeholder)
                    }
                    inner()
                },
            )
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(ChatListV6Palette.ComposeStart, ChatListV6Palette.ComposeEnd)))
                .clickable(onClick = onCompose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Edit, null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
fun ChatListV6Filters(
    selected: ChatListFilter,
    onSelect: (ChatListFilter) -> Unit,
) {
    val chips = listOf(
        ChatListFilter.All to "Все",
        ChatListFilter.Unread to "Непрочитанные",
        ChatListFilter.Groups to "Группы",
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 14.dp),
    ) {
        items(chips.size) { i ->
            val (filter, label) = chips[i]
            val active = filter == selected
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (active) {
                            Modifier.background(
                                Brush.linearGradient(
                                    listOf(ChatListV6Palette.ChipActiveStart, ChatListV6Palette.ChipActiveEnd),
                                ),
                            )
                        } else {
                            Modifier
                                .background(ChatListV6Palette.FieldBg)
                                .border(1.dp, ChatListV6Palette.FieldBorder, RoundedCornerShape(12.dp))
                        },
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color.White else ChatListV6Palette.TextMuted,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListV6Item(
    chat: ChatEntity,
    index: Int,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val time = remember(chat.lastMessageTime) { formatChatListTime(chat.lastMessageTime) }
    val gradient = avatarGradients[index % avatarGradients.size]
    val initials = remember(chat.title) { chatInitials(chat.title) }
    val showOnline = remember(chat.id, chat.type) {
        chat.type == ChatType.PRIVATE &&
            !chat.id.startsWith("welcome_") &&
            chat.id.hashCode().absoluteValue % 3 != 0
    }
    val timeColor = if (chat.unreadCount > 0) ChatListV6Palette.NavActive else ChatListV6Palette.NavInactive
    val clickModifier = if (onLongClick != null) {
        Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    } else {
        Modifier.clickable(onClick = onClick)
    }

    Row(
        modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            if (showOnline) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(13.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(ChatListV6Palette.Online)
                        .border(2.dp, ChatListV6Palette.Bg, RoundedCornerShape(5.dp)),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    chat.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatListV6Palette.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    time,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = timeColor,
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.lastMessagePreview.ifEmpty { appStr(AppStringKey.NO_MESSAGES) },
                    fontSize = 13.sp,
                    color = ChatListV6Palette.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (chat.unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(ChatListV6Palette.BadgeStart, ChatListV6Palette.BadgeEnd),
                                ),
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${chat.unreadCount}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

fun filterChatsV6(
    chats: List<ChatEntity>,
    query: String,
    filter: ChatListFilter,
): List<ChatEntity> {
    var list = chats.filter { it.type != ChatType.SAVED && !it.isArchived }
    when (filter) {
        ChatListFilter.Unread -> list = list.filter { it.unreadCount > 0 }
        ChatListFilter.Groups -> list = list.filter { it.type == ChatType.GROUP || it.type == ChatType.CHANNEL }
        ChatListFilter.All -> Unit
    }
    if (query.isNotBlank()) {
        list = list.filter { it.title.contains(query, ignoreCase = true) }
    }
    return list.sortedWith(
        compareByDescending<ChatEntity> { it.isPinned }.thenByDescending { it.lastMessageTime },
    )
}

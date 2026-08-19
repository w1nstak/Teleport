package com.teleport.messenger.ui.screens.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.ChatEntity
import com.teleport.messenger.data.entity.ChatType
import com.teleport.messenger.data.entity.UserEntity
import com.teleport.messenger.ui.components.AppFloatingBottomNav
import com.teleport.messenger.ui.components.MainTab
import com.teleport.messenger.ui.theme.ManropeFontFamily
import com.teleport.messenger.ui.theme.UnboundedFontFamily
import com.teleport.messenger.viewmodel.TeleportViewModel
import kotlin.coroutines.resume
import kotlin.math.absoluteValue
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

/** Colors from messenger_contacts_v2.html / messenger_contacts_empty.html */
private object ContactsPalette {
    val Bg = Color(0xFF0A0A12)
    val Card = Color(0xFF17151C)
    val Accent = Color(0xFF5B5BF0)
    val Accent2 = Color(0xFF8B5CF6)
    val Online = Color(0xFF4ADE80)
    val Text = Color(0xFFF4F2FA)
    val TextDim = Color(0xFF9B96A8)
    val Link = Color(0xFFB8B4FF)
    val Hairline = Color(0xFF2A2732)
    val SearchBg = Color(0x0DFFFFFF)
    val SearchBorder = Color(0x1AFFFFFF)
    val RowBg = Color(0x06FFFFFF)
    val RowBorder = Color(0x0DFFFFFF)
    val MsgBtnBg = Color(0x0DFFFFFF)
    val MsgBtnBorder = Color(0x1AFFFFFF)
    val AccentGradient = Brush.linearGradient(listOf(Accent, Accent2))
    val TitleGradient = Brush.linearGradient(listOf(Color.White, Color(0xFFC7C3FF)))

    val Avatars = listOf(
        Brush.linearGradient(listOf(Accent, Accent2)),
        Brush.linearGradient(listOf(Color(0xFFFF8A65), Color(0xFFD9542F))),
        Brush.linearGradient(listOf(Color(0xFF4ADE80), Color(0xFF0F8C56))),
        Brush.linearGradient(listOf(Color(0xFFA79BFA), Color(0xFF5B4FCF))),
        Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF1E6FBF))),
    )
}

@Composable
fun ContactsScreen(
    vm: TeleportViewModel,
    onChats: () -> Unit,
    onContacts: () -> Unit,
    onSettings: () -> Unit,
    onOpenChat: (String) -> Unit,
    onSearch: () -> Unit,
) {
    val chats by vm.chats.collectAsState()
    val me by vm.currentUser().collectAsState(initial = null)
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var peers by remember { mutableStateOf<Map<String, UserEntity>>(emptyMap()) }

    val contacts = remember(chats, query, peers) {
        chats
            .filter { it.type == ChatType.PRIVATE && !it.isArchived }
            .filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.lastMessagePreview.contains(query, ignoreCase = true) ||
                    peers[it.id]?.username?.contains(query, ignoreCase = true) == true ||
                    peers[it.id]?.displayName?.contains(query, ignoreCase = true) == true
            }
            .sortedBy { it.title.lowercase() }
    }

    LaunchedEffect(chats.filter { it.type == ChatType.PRIVATE && !it.isArchived }.map { it.id }.joinToString(), me?.id) {
        val myId = me?.id ?: return@LaunchedEffect
        val privateChats = chats.filter { it.type == ChatType.PRIVATE && !it.isArchived }
        val result = mutableMapOf<String, UserEntity>()
        for (chat in privateChats) {
            val user = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                vm.getContactForChat(chat.id, myId) { peer ->
                    if (cont.isActive) cont.resume(peer) {}
                }
            }
            if (user != null) result[chat.id] = user
        }
        peers = result
    }

    val favorites = remember(contacts) { contacts.filter { it.isPinned } }
    val onlineCount = remember(contacts, peers) {
        contacts.count { peers[it.id]?.isOnline == true }
    }
    val grouped = remember(contacts) {
        contacts.groupBy { contactLetter(it.title) }.toSortedMap()
    }

    fun inviteFriends() {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Присоединяйся ко мне в Teleport Messenger.")
        }
        context.startActivity(Intent.createChooser(share, "Пригласить"))
    }

    Scaffold(
        containerColor = ContactsPalette.Bg,
        bottomBar = {
            AppFloatingBottomNav(MainTab.Contacts, onChats, onContacts, onSettings)
        },
        floatingActionButton = {
            if (contacts.isNotEmpty()) {
                Box(
                    Modifier
                        .padding(end = 4.dp, bottom = 72.dp)
                        .size(52.dp)
                        .shadow(
                            14.dp,
                            RoundedCornerShape(18.dp),
                            ambientColor = ContactsPalette.Accent.copy(0.45f),
                            spotColor = ContactsPalette.Accent.copy(0.45f),
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(ContactsPalette.AccentGradient)
                        .clickable(onClick = onSearch),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PersonAdd,
                        contentDescription = "Добавить",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ContactsPalette.Bg),
        ) {
            ContactsHero(
                subtitle = contactsSubtitle(contacts.size, onlineCount),
                query = query,
                onQueryChange = { query = it },
                onAdd = onSearch,
            )

            if (contacts.isEmpty() && query.isBlank()) {
                ContactsEmptyState(onAdd = onSearch, onInvite = { inviteFriends() })
            } else if (contacts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Ничего не найдено",
                        fontFamily = ManropeFontFamily,
                        color = ContactsPalette.TextDim,
                        fontSize = 14.sp,
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 100.dp),
                ) {
                    if (favorites.isNotEmpty()) {
                        item(key = "fav-header") {
                            Text(
                                "Избранное",
                                fontFamily = UnboundedFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = ContactsPalette.TextDim,
                                letterSpacing = 0.3.sp,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                        }
                        item(key = "fav-row") {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(bottom = 22.dp)
                                    .drawBehind {
                                        drawLine(
                                            ContactsPalette.Hairline,
                                            Offset(0f, size.height - 0.5f),
                                            Offset(size.width, size.height - 0.5f),
                                            1.dp.toPx(),
                                        )
                                    }
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                favorites.forEachIndexed { index, chat ->
                                    val online = peers[chat.id]?.isOnline == true
                                    FavoriteContact(
                                        chat = chat,
                                        brush = ContactsPalette.Avatars[index % ContactsPalette.Avatars.size],
                                        online = online,
                                        highlighted = online,
                                        onClick = { onOpenChat(chat.id) },
                                    )
                                }
                            }
                        }
                    }

                    grouped.forEach { (letter, sectionChats) ->
                        item(key = "letter-$letter") {
                            ContactsSectionLabel(letter)
                        }
                        items(sectionChats, key = { it.id }) { chat ->
                            ContactRow(
                                chat = chat,
                                peer = peers[chat.id],
                                brush = ContactsPalette.Avatars[
                                    chat.id.hashCode().absoluteValue % ContactsPalette.Avatars.size
                                ],
                                online = peers[chat.id]?.isOnline == true,
                                onClick = { onOpenChat(chat.id) },
                                onMessage = { onOpenChat(chat.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactsHero(
    subtitle: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1520), Color(0xFF100E15), Color(0xFF0B0A0E)),
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x6B5B5BF0), Color.Transparent),
                        center = Offset(size.width * 0.15f, 0f),
                        radius = size.width * 0.7f,
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x528B5CF6), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.55f,
                    ),
                )
            }
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 20.dp),
    ) {
        ContactsSpark(Modifier.align(Alignment.TopEnd).offset(x = (-50).dp, y = 4.dp), 0.4f)
        ContactsSpark(Modifier.align(Alignment.TopEnd).offset(x = (-6).dp, y = 40.dp), 0.25f)

        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Контакты",
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 23.sp,
                        style = TextStyle(brush = ContactsPalette.TitleGradient),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        subtitle,
                        fontFamily = ManropeFontFamily,
                        fontSize = 12.sp,
                        color = ContactsPalette.TextDim,
                    )
                }
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(
                            10.dp,
                            RoundedCornerShape(14.dp),
                            ambientColor = ContactsPalette.Accent.copy(0.45f),
                            spotColor = ContactsPalette.Accent.copy(0.45f),
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(ContactsPalette.AccentGradient)
                        .clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(ContactsPalette.SearchBg)
                    .border(1.dp, ContactsPalette.SearchBorder, RoundedCornerShape(15.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Filled.Search,
                    null,
                    tint = ContactsPalette.TextDim,
                    modifier = Modifier.size(16.dp),
                )
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.5.sp,
                        color = ContactsPalette.Text,
                    ),
                    cursorBrush = SolidColor(ContactsPalette.Accent),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    "Поиск контактов",
                                    fontFamily = ManropeFontFamily,
                                    fontSize = 13.5.sp,
                                    color = ContactsPalette.TextDim,
                                )
                            }
                            inner()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ContactsEmptyState(onAdd: () -> Unit, onInvite: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp)
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(ContactsPalette.Accent.copy(0.25f), Color.Transparent),
                        ),
                        CircleShape,
                    ),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(ContactsPalette.Card)
                    .border(1.dp, ContactsPalette.Hairline, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PersonAdd,
                    null,
                    tint = ContactsPalette.Link,
                    modifier = Modifier.size(38.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Здесь пока пусто",
            fontFamily = UnboundedFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = ContactsPalette.Text,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Добавьте первый контакт, чтобы начать общение с друзьями и близкими",
            fontFamily = ManropeFontFamily,
            fontSize = 13.sp,
            color = ContactsPalette.TextDim,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.widthIn(max = 230.dp),
        )
        Spacer(Modifier.height(26.dp))

        Row(
            Modifier
                .shadow(
                    14.dp,
                    RoundedCornerShape(15.dp),
                    ambientColor = ContactsPalette.Accent.copy(0.4f),
                    spotColor = ContactsPalette.Accent.copy(0.4f),
                )
                .clip(RoundedCornerShape(15.dp))
                .background(ContactsPalette.AccentGradient)
                .clickable(onClick = onAdd)
                .padding(horizontal = 26.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(
                "Добавить контакт",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White,
            )
        }

        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Или ",
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                color = ContactsPalette.TextDim,
            )
            Text(
                "пригласите друзей",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = ContactsPalette.Link,
                modifier = Modifier.clickable(onClick = onInvite),
            )
            Text(
                " по ссылке",
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                color = ContactsPalette.TextDim,
            )
        }
    }
}

@Composable
private fun ContactsSectionLabel(letter: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 10.dp, start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(22.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp), ambientColor = ContactsPalette.Accent.copy(0.35f))
                .clip(RoundedCornerShape(8.dp))
                .background(ContactsPalette.AccentGradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                letter,
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color.White,
            )
        }
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(ContactsPalette.Hairline),
        )
    }
}

@Composable
private fun FavoriteContact(
    chat: ChatEntity,
    brush: Brush,
    online: Boolean,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val short = chat.title.trim().substringBefore(' ').ifBlank { chat.title }.take(8)
    Column(
        Modifier
            .width(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            if (highlighted) {
                Box(
                    Modifier
                        .size(64.dp)
                        .offset(y = 0.dp)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    ContactsPalette.Accent,
                                    ContactsPalette.Accent2,
                                    ContactsPalette.Accent,
                                ),
                            ),
                            RoundedCornerShape(20.dp),
                        ),
                )
                Box(
                    Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(ContactsPalette.Bg),
                )
            }
            Box(
                Modifier
                    .size(56.dp)
                    .shadow(8.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(brush),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    chat.title.firstOrNull()?.uppercase() ?: "?",
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color.White,
                )
            }
            if (online) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 3.dp, y = 3.dp)
                        .size(13.dp)
                        .border(2.5.dp, ContactsPalette.Bg, CircleShape)
                        .clip(CircleShape)
                        .background(ContactsPalette.Online),
                )
            }
        }
        Text(
            short,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = ContactsPalette.TextDim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ContactRow(
    chat: ChatEntity,
    peer: UserEntity? = null,
    brush: Brush,
    online: Boolean,
    onClick: () -> Unit,
    onMessage: () -> Unit,
) {
    val handle = remember(peer, chat) {
        peer?.username?.takeIf { it.isNotBlank() }?.let { u -> if (u.startsWith("@")) u else "@$u" }
            ?: run {
                val slug = chat.title.lowercase()
                    .replace(Regex("[^a-zа-я0-9]+"), ".")
                    .trim('.')
                    .take(12)
                    .ifBlank { "user" }
                "@$slug"
            }
    }
    val title = peer?.displayName?.takeIf { it.isNotBlank() } ?: chat.title
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ContactsPalette.RowBg)
            .border(1.dp, ContactsPalette.RowBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(44.dp)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .shadow(6.dp, RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(15.dp))
                    .background(brush),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    title.firstOrNull()?.uppercase() ?: "?",
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
            if (online) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(11.dp)
                        .border(2.5.dp, ContactsPalette.Bg, CircleShape)
                        .clip(CircleShape)
                        .background(ContactsPalette.Online),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = ContactsPalette.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                handle,
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                color = ContactsPalette.TextDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ContactsPalette.MsgBtnBg)
                .border(1.dp, ContactsPalette.MsgBtnBorder, RoundedCornerShape(12.dp))
                .clickable(onClick = onMessage),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Написать",
                tint = ContactsPalette.TextDim,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@Composable
private fun ContactsSpark(modifier: Modifier = Modifier, alpha: Float) {
    Canvas(modifier.size(12.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val s = size.minDimension / 24f
        val path = Path().apply {
            moveTo(cx, cy - 10f * s)
            cubicTo(cx, cy - 4f * s, cx + 4f * s, cy, cx + 10f * s, cy)
            cubicTo(cx + 4f * s, cy, cx, cy + 4f * s, cx, cy + 10f * s)
            cubicTo(cx, cy + 4f * s, cx - 4f * s, cy, cx - 10f * s, cy)
            cubicTo(cx - 4f * s, cy, cx, cy - 4f * s, cx, cy - 10f * s)
            close()
        }
        drawPath(path, Color.White.copy(alpha = alpha))
    }
}

private fun contactLetter(title: String): String {
    val c = title.trim().firstOrNull()?.uppercaseChar() ?: '#'
    return if (c.isLetter()) c.toString() else "#"
}

private fun contactsSubtitle(total: Int, online: Int): String {
    val word = when {
        total % 10 == 1 && total % 100 != 11 -> "контакт"
        total % 10 in 2..4 && total % 100 !in 12..14 -> "контакта"
        else -> "контактов"
    }
    return if (total == 0) "0 контактов"
    else "$total $word · $online в сети"
}

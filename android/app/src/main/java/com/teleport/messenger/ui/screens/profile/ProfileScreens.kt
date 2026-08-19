package com.teleport.messenger.ui.screens.profile

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.UserEntity
import com.teleport.messenger.ui.theme.ManropeFontFamily
import com.teleport.messenger.ui.theme.UnboundedFontFamily
import com.teleport.messenger.util.PrivacyHelper
import com.teleport.messenger.viewmodel.TeleportViewModel

private object ProfileMockPalette {
    val Bg = Color(0xFF0A0A12)
    val Card = Color(0xFF17151C)
    val Hairline = Color(0xFF2A2732)
    val Accent = Color(0xFF7C6CF5)
    val Accent2 = Color(0xFFFF8A65)
    val Online = Color(0xFF4ADE80)
    val Text = Color(0xFFF4F2FA)
    val TextDim = Color(0xFF9B96A8)
    val PurpleIc = Color(0xFFA79BFA)
    val CoralIc = Color(0xFFFFAB8D)
    val TealIc = Color(0xFF6EE7A8)
}

@Composable
fun ProfileScreen(
    vm: TeleportViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onUsername: () -> Unit,
    onAppearance: () -> Unit,
    onSecurity: () -> Unit = {},
    onSessions: () -> Unit = {},
    onAccounts: () -> Unit = {},
    onPremium: () -> Unit = {},
    onFavorites: () -> Unit = {},
    onStickers: () -> Unit = {},
    onBlocked: () -> Unit = {},
    onNotifications: () -> Unit = {},
) {
    val user by vm.currentUser().collectAsState(initial = null)
    val account by vm.activeAccount.collectAsState()
    val settings by vm.settings().collectAsState(initial = null)
    if (user == null) return

    val name = user?.displayName ?: "Пользователь"
    val username = user?.username
    val online = user?.isOnline == true && settings?.hideOnlineStatus != true
    val statusText = if (online) "в сети" else PrivacyHelper.onlineStatus(user!!, true, false, false)
    val bio = user?.bio?.takeIf { it.isNotBlank() } ?: "—"
    val music = user?.status?.takeIf { it.isNotBlank() }
    val phoneHidden = account?.phone.isNullOrBlank() ||
        account?.phone?.startsWith("web:") == true ||
        user?.anonymousMode == true
    val phoneValue = if (phoneHidden) "Скрыт" else (account?.phone ?: "Скрыт")
    var menuOpen by remember { mutableStateOf(false) }
    var showSongPicker by remember { mutableStateOf(false) }
    val profileProgress = remember(user?.isPremium, user?.avatarUri, username, bio) {
        var p = 0.25f
        if (!username.isNullOrBlank()) p += 0.2f
        if (bio != "—") p += 0.2f
        if (!user?.avatarUri.isNullOrBlank()) p += 0.2f
        if (user?.isPremium == true) p += 0.15f
        p.coerceIn(0.25f, 1f)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(ProfileMockPalette.Bg)
            .verticalScroll(rememberScrollState()),
    ) {
        ProfileHero(
            name = name.uppercase(),
            username = username,
            statusText = statusText,
            online = online,
            showBadge = user?.isPremium == true || username.equals("w1nst", ignoreCase = true),
            progress = profileProgress,
            onBack = onBack,
            onEdit = onEdit,
            onMore = { menuOpen = true },
            moreMenu = {
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("@username") }, onClick = { menuOpen = false; onUsername() })
                    DropdownMenuItem(text = { Text("Оформление") }, onClick = { menuOpen = false; onAppearance() })
                    DropdownMenuItem(text = { Text("Защита") }, onClick = { menuOpen = false; onSecurity() })
                    DropdownMenuItem(text = { Text("Устройства") }, onClick = { menuOpen = false; onSessions() })
                    DropdownMenuItem(text = { Text("Уведомления") }, onClick = { menuOpen = false; onNotifications() })
                    DropdownMenuItem(text = { Text("Premium") }, onClick = { menuOpen = false; onPremium() })
                    DropdownMenuItem(text = { Text("Избранное") }, onClick = { menuOpen = false; onFavorites() })
                    DropdownMenuItem(text = { Text("Стикеры") }, onClick = { menuOpen = false; onStickers() })
                    DropdownMenuItem(text = { Text("Заблокированные") }, onClick = { menuOpen = false; onBlocked() })
                    DropdownMenuItem(text = { Text("Аккаунты") }, onClick = { menuOpen = false; onAccounts() })
                    DropdownMenuItem(
                        text = { Text("Сменить песню") },
                        onClick = { menuOpen = false; showSongPicker = true },
                    )
                }
            },
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProfileInfoCard(
                icon = Icons.Outlined.MusicNote,
                iconBg = Color(0x297C6CF5),
                iconTint = ProfileMockPalette.PurpleIc,
                label = "Музыка профиля",
                value = music ?: "Не выбрана",
                trailing = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MusicWaveBars()
                        ProfileSongChangeButton(onClick = { showSongPicker = true })
                    }
                },
                onClick = { showSongPicker = true },
            )
            ProfileInfoCard(
                icon = Icons.Filled.Edit,
                iconBg = Color(0x29FF8A65),
                iconTint = ProfileMockPalette.CoralIc,
                label = "О себе",
                value = bio,
                onClick = onEdit,
            )
            ProfileInfoCard(
                icon = Icons.Outlined.Phone,
                iconBg = Color(0x244ADE80),
                iconTint = ProfileMockPalette.TealIc,
                label = "Номер",
                value = phoneValue,
            )
        }
    }

    if (showSongPicker && user != null) {
        ProfileSongPickerSheet(
            current = user!!.status,
            onDismiss = { showSongPicker = false },
            onSave = { song ->
                vm.updateProfile(user!!.copy(status = song.trim()))
                showSongPicker = false
            },
        )
    }
}

@Composable
private fun ProfileHero(
    name: String,
    username: String?,
    statusText: String,
    online: Boolean,
    showBadge: Boolean,
    progress: Float,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onMore: () -> Unit,
    moreMenu: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1622), Color(0xFF100E15), Color(0xFF0B0A0E)),
                ),
            ),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x8C7C6CF5), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(120f, 0f),
                        radius = 520f,
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x59FF8A65), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(900f, 120f),
                        radius = 420f,
                    ),
                ),
        )

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                    .background(
                        Brush.horizontalGradient(
                            listOf(ProfileMockPalette.Accent, ProfileMockPalette.Accent2),
                        ),
                    ),
            )
        }

        Row(
            Modifier
                .align(Alignment.TopStart)
                .padding(top = 34.dp, start = 16.dp),
        ) {
            GlassIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = ProfileMockPalette.Text, modifier = Modifier.size(17.dp))
            }
        }

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 34.dp, end = 16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassIconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, "Редактировать", tint = ProfileMockPalette.Text, modifier = Modifier.size(17.dp))
                }
                GlassIconButton(onClick = onMore) {
                    Icon(Icons.Filled.MoreHoriz, "Ещё", tint = ProfileMockPalette.Text, modifier = Modifier.size(19.dp))
                }
            }
            moreMenu()
        }

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileMockPalette.Text,
                    letterSpacing = 0.2.sp,
                )
                if (showBadge) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ProfileMockPalette.Accent, ProfileMockPalette.Accent2),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(11.dp))
                    }
                }
            }
            Row(
                Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (online) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ProfileMockPalette.Online),
                    )
                }
                Text(statusText, fontSize = 13.sp, color = ProfileMockPalette.TextDim)
            }
            username?.let {
                Text(
                    "@$it",
                    fontSize = 14.sp,
                    color = ProfileMockPalette.TextDim,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun GlassIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.05f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ProfileInfoCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ProfileMockPalette.Card)
            .border(1.dp, ProfileMockPalette.Hairline, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = ProfileMockPalette.TextDim)
            Spacer(Modifier.height(3.dp))
            Text(
                value,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = ProfileMockPalette.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun ProfileSongChangeButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x247C6CF5))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Sync,
            contentDescription = "Сменить песню",
            tint = ProfileMockPalette.PurpleIc,
            modifier = Modifier.size(15.dp),
        )
    }
}

private val SuggestedProfileSongs = listOf(
    "без названия — 24635259",
    "The Weeknd — Blinding Lights",
    "Billie Eilish — lovely",
    "Imagine Dragons — Believer",
    "Måneskin — Beggin'",
    "Miyagi & Andy Panda — I Got Love",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSongPickerSheet(
    current: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableStateOf(current.ifBlank { "" }) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ProfileMockPalette.Card,
        contentColor = ProfileMockPalette.Text,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Песня профиля",
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = ProfileMockPalette.Text,
            )
            Text(
                "Будет видна на вашей странице",
                fontFamily = ManropeFontFamily,
                fontSize = 13.sp,
                color = ProfileMockPalette.TextDim,
            )

            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text("Исполнитель — трек", color = ProfileMockPalette.TextDim)
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ProfileMockPalette.Accent,
                    unfocusedBorderColor = ProfileMockPalette.Hairline,
                    focusedContainerColor = ProfileMockPalette.Bg,
                    unfocusedContainerColor = ProfileMockPalette.Bg,
                    focusedTextColor = ProfileMockPalette.Text,
                    unfocusedTextColor = ProfileMockPalette.Text,
                    cursorColor = ProfileMockPalette.Accent,
                ),
            )

            SuggestedProfileSongs.forEach { song ->
                val selected = draft.equals(song, ignoreCase = true)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) Color(0x247C6CF5) else Color.Transparent,
                        )
                        .border(
                            1.dp,
                            if (selected) ProfileMockPalette.Accent.copy(0.45f) else ProfileMockPalette.Hairline,
                            RoundedCornerShape(14.dp),
                        )
                        .clickable { draft = song }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.MusicNote,
                        null,
                        tint = ProfileMockPalette.PurpleIc,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        song,
                        modifier = Modifier.weight(1f),
                        fontFamily = ManropeFontFamily,
                        fontSize = 14.sp,
                        color = ProfileMockPalette.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            null,
                            tint = ProfileMockPalette.Accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    onClick = { onSave("") },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Убрать", color = ProfileMockPalette.TextDim, fontFamily = ManropeFontFamily)
                }
                Box(
                    Modifier
                        .weight(1.4f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(ProfileMockPalette.Accent, ProfileMockPalette.Accent2),
                            ),
                        )
                        .clickable { onSave(draft) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Сохранить",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicWaveBars() {
    val heights = listOf(6f, 14f, 9f, 16f, 7f)
    val transition = rememberInfiniteTransition(label = "musicWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wavePhase",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(18.dp),
    ) {
        heights.forEachIndexed { index, h ->
            val t = ((phase + index * 0.15f) % 1f)
            val scale = 0.45f + 0.55f * (if (t < 0.5f) t * 2f else (1f - t) * 2f)
            Box(
                Modifier
                    .width(3.dp)
                    .height((h * scale).coerceAtLeast(4f).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ProfileMockPalette.Accent),
            )
        }
    }
}

@Composable
fun ContactProfileScreen(
    vm: TeleportViewModel,
    chatId: String,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onCall: (String) -> Unit,
) {
    val chat by vm.chat(chatId).collectAsState(initial = null)
    val me by vm.currentUser().collectAsState(initial = null)
    var contact by remember { mutableStateOf<UserEntity?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(chatId, me?.id) {
        me?.id?.let { id -> vm.getContactForChat(chatId, id) { contact = it } }
    }

    val displayUser = contact ?: UserEntity(
        id = "unknown",
        accountId = "",
        displayName = chat?.title ?: "Контакт",
        username = null,
    )
    val online = displayUser.isOnline
    val statusText = if (online) "в сети" else PrivacyHelper.onlineStatus(displayUser, true, false, false)
    val bio = displayUser.bio.takeIf { it.isNotBlank() } ?: "—"
    val music = displayUser.status.takeIf { it.isNotBlank() } ?: "Нет трека"

    Column(
        Modifier
            .fillMaxSize()
            .background(ProfileMockPalette.Bg)
            .verticalScroll(rememberScrollState()),
    ) {
        ProfileHero(
            name = displayUser.displayName.uppercase(),
            username = displayUser.username,
            statusText = statusText,
            online = online,
            showBadge = displayUser.isPremium,
            progress = 0.55f,
            onBack = onBack,
            onEdit = onMessage,
            onMore = { menuOpen = true },
            moreMenu = {
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Написать") },
                        onClick = { menuOpen = false; onMessage() },
                    )
                    DropdownMenuItem(
                        text = { Text("Аудиозвонок") },
                        onClick = { menuOpen = false; onCall("voice") },
                    )
                    DropdownMenuItem(
                        text = { Text("Видеозвонок") },
                        onClick = { menuOpen = false; onCall("video") },
                    )
                    DropdownMenuItem(
                        text = { Text(if (chat?.isPinned == true) "Открепить" else "Закрепить") },
                        onClick = {
                            menuOpen = false
                            chat?.let { vm.pinChat(it.id, !it.isPinned) }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(if ((chat?.muteUntil ?: 0L) > 0L) "Включить звук" else "Без звука") },
                        onClick = {
                            menuOpen = false
                            chat?.let { vm.muteChat(it.id, it.muteUntil <= 0L) }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("В архив") },
                        onClick = {
                            menuOpen = false
                            chat?.let { vm.archiveChat(it.id, true); onBack() }
                        },
                    )
                }
            },
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProfileInfoCard(
                icon = Icons.Outlined.MusicNote,
                iconBg = Color(0x297C6CF5),
                iconTint = ProfileMockPalette.PurpleIc,
                label = "Музыка",
                value = music,
                trailing = if (displayUser.status.isNotBlank()) ({ MusicWaveBars() }) else null,
            )
            ProfileInfoCard(
                icon = Icons.Filled.Edit,
                iconBg = Color(0x29FF8A65),
                iconTint = ProfileMockPalette.CoralIc,
                label = "О себе",
                value = bio,
            )
            ProfileInfoCard(
                icon = Icons.Outlined.Chat,
                iconBg = Color(0x297C6CF5),
                iconTint = ProfileMockPalette.PurpleIc,
                label = "Действия",
                value = "Написать",
                onClick = onMessage,
            )
            ProfileInfoCard(
                icon = Icons.Outlined.Call,
                iconBg = Color(0x244ADE80),
                iconTint = ProfileMockPalette.TealIc,
                label = "Звонок",
                value = "Голосовой",
                onClick = { onCall("voice") },
            )
            if (displayUser.id != "unknown" && displayUser.id != me?.id) {
                ProfileInfoCard(
                    icon = Icons.Outlined.Block,
                    iconBg = Color(0x29FF6F6F),
                    iconTint = Color(0xFFFF8A8A),
                    label = "Блокировка",
                    value = "Заблокировать",
                    onClick = { vm.blockUser(displayUser.id); onBack() },
                )
            }
        }
    }
}

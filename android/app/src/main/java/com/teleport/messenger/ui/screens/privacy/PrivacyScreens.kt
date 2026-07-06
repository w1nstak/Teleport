package com.teleport.messenger.ui.screens.privacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.UserEntity
import com.teleport.messenger.ui.components.*
import com.teleport.messenger.ui.screens.settings.SettingsGroupCard
import com.teleport.messenger.ui.screens.settings.SettingsRow
import com.teleport.messenger.ui.theme.TeleportAppTheme
import com.teleport.messenger.util.PrivacyHelper
import com.teleport.messenger.util.PrivacyLevel
import com.teleport.messenger.viewmodel.TeleportViewModel

@Composable
fun PrivacyScreen(
    vm: TeleportViewModel,
    onBack: () -> Unit,
    onBlocked: () -> Unit,
) {
    val user by vm.currentUser().collectAsState(initial = null)
    val settings by vm.settings().collectAsState(initial = null)
    var lastSeen by remember(user) { mutableStateOf(user?.privacyLastSeen ?: PrivacyLevel.EVERYONE) }
    var phone by remember(user) { mutableStateOf(user?.privacyPhone ?: PrivacyLevel.CONTACTS) }
    var photo by remember(user) { mutableStateOf(user?.privacyPhoto ?: PrivacyLevel.EVERYONE) }
    var hideOnline by remember(settings) { mutableStateOf(settings?.hideOnlineStatus ?: false) }
    var hideRead by remember(settings) { mutableStateOf(settings?.hideReadReceipts ?: false) }
    val colors = TeleportAppTheme.colors

    fun saveUser() {
        user?.let {
            vm.updateProfile(it.copy(
                privacyLastSeen = lastSeen,
                privacyPhone = phone,
                privacyPhoto = photo,
            ))
        }
    }

    fun saveSettings() {
        settings?.let {
            vm.updateSettings(it.copy(hideOnlineStatus = hideOnline, hideReadReceipts = hideRead))
        }
    }

    Scaffold(containerColor = colors.screenBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TeleportTopBar("Видимость профиля", onBack)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        "Кто видит ваши данные в чатах. Защита от взлома — в разделе «Защита аккаунта».",
                        color = colors.textMuted,
                        fontSize = 14.sp,
                    )
                }
                item {
                    Text("Кто может видеть", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                }
                item {
                    SettingsGroupCard {
                        PrivacyLevelRow("Последний визит", lastSeen) { lastSeen = it; saveUser() }
                        PrivacyLevelRow("Номер телефона", phone) { phone = it; saveUser() }
                        PrivacyLevelRow("Фото профиля", photo, showDivider = false) { photo = it; saveUser() }
                    }
                }
                item {
                    SettingsGroupCard {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Скрыть «online»", color = colors.textPrimary)
                            Switch(checked = hideOnline, onCheckedChange = { hideOnline = it; saveSettings() })
                        }
                        HorizontalDivider(color = colors.divider)
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Скрыть прочтение", color = colors.textPrimary)
                            Switch(checked = hideRead, onCheckedChange = { hideRead = it; saveSettings() })
                        }
                    }
                }
                item {
                    SettingsGroupCard {
                        SettingsRow(
                            Icons.Default.Block,
                            Color(0xFFFF3B30),
                            "Заблокированные",
                            onBlocked,
                            showDivider = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyLevelRow(
    title: String,
    selected: String,
    showDivider: Boolean = true,
    onSelect: (String) -> Unit,
) {
    val colors = TeleportAppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = colors.textPrimary)
            Text(PrivacyHelper.levelLabel(selected), color = colors.accentBlue)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(PrivacyLevel.EVERYONE, PrivacyLevel.CONTACTS, PrivacyLevel.NOBODY).forEach { level ->
                DropdownMenuItem(
                    text = { Text(PrivacyHelper.levelLabel(level)) },
                    onClick = { onSelect(level); expanded = false },
                )
            }
        }
        if (showDivider) HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = colors.divider)
    }
}

@Composable
fun BlockedUsersScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val blocked by vm.blocked().collectAsState(initial = emptyList())
    var users by remember { mutableStateOf<Map<String, UserEntity>>(emptyMap()) }

    LaunchedEffect(blocked) {
        vm.loadUsers(blocked.map { it.blockedUserId }) { users = it.associateBy { u -> u.id } }
    }

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Заблокированные", onBack)
        if (blocked.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет заблокированных", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(blocked) { entry ->
                    val u = users[entry.blockedUserId]
                    ListItem(
                        headlineContent = { Text(u?.displayName ?: entry.blockedUserId) },
                        supportingContent = { u?.username?.let { Text("@$it") } },
                        leadingContent = { TeleportAvatar(u?.displayName ?: "?", u?.isPremium == true) },
                        trailingContent = {
                            TextButton(onClick = { vm.unblockUser(entry.blockedUserId) }) {
                                Text("Разблокировать")
                            }
                        },
                    )
                }
            }
        }
    }
}

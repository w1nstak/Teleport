package com.teleport.messenger.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.ChatType
import com.teleport.messenger.ui.components.AppFloatingBottomNav
import com.teleport.messenger.ui.components.MainTab
import com.teleport.messenger.ui.components.TeleportTopBar
import com.teleport.messenger.ui.screens.chat.ChatListV6Item
import com.teleport.messenger.ui.screens.chat.ChatListV6Palette
import com.teleport.messenger.ui.screens.settings.SettingsGroupCard
import com.teleport.messenger.ui.screens.settings.SettingsScreenScaffold
import com.teleport.messenger.ui.theme.TeleportAppTheme
import com.teleport.messenger.viewmodel.TeleportViewModel

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
    val contacts = remember(chats) {
        chats.filter { it.type == ChatType.PRIVATE && !it.isArchived }
    }
    val colors = TeleportAppTheme.colors

    SettingsScreenScaffold(
        bottomBar = {
            AppFloatingBottomNav(MainTab.Contacts, onChats, onContacts, onSettings)
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(ChatListV6Palette.Bg)) {
            Text(
                "Контакты",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                color = ChatListV6Palette.TextPrimary,
            )
            if (contacts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Нет контактов", color = colors.textMuted)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onSearch) { Text("Найти пользователя") }
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 10.dp)) {
                    items(contacts.size, key = { contacts[it].id }) { index ->
                        ChatListV6Item(contacts[index], index, { onOpenChat(contacts[index].id) })
                    }
                }
            }
        }
    }
}

@Composable
fun CallsListScreen(
    vm: TeleportViewModel,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
) {
    val calls by vm.recentCalls.collectAsState()
    val chats by vm.chats.collectAsState()
    val colors = TeleportAppTheme.colors

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Звонки", onBack)
        if (calls.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет звонков", color = colors.textMuted)
            }
        } else {
            LazyColumn {
                items(calls, key = { it.id }) { call ->
                    val title = chats.find { it.id == call.chatId }?.title ?: "Чат"
                    ListItem(
                        headlineContent = { Text(title) },
                        supportingContent = {
                            Text(if (call.type == "video") "Видеозвонок" else "Звонок")
                        },
                        modifier = Modifier.clickable { onOpenChat(call.chatId) },
                    )
                    HorizontalDivider(color = colors.divider)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val folders by vm.folders().collectAsState(initial = emptyList())
    var newName by remember { mutableStateOf("") }
    val colors = TeleportAppTheme.colors

    Scaffold(
        containerColor = colors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Папки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад", tint = colors.textPrimary)
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Новая папка") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (newName.isNotBlank()) {
                            vm.createFolder(newName.trim())
                            newName = ""
                        }
                    }) { Icon(Icons.Default.Add, null) }
                },
            )
            Spacer(Modifier.height(16.dp))
            SettingsGroupCard {
                folders.forEach { folder ->
                    Text(
                        folder.name,
                        modifier = Modifier.padding(16.dp),
                        color = colors.textPrimary,
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = colors.divider)
                }
            }
        }
    }
}

@Composable
fun StickersScreen(onBack: () -> Unit) {
    SimplePlaceholderScreen("Эмодзи и стикеры", onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplePlaceholderScreen(title: String, onBack: () -> Unit) {
    val colors = TeleportAppTheme.colors
    Scaffold(
        containerColor = colors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Назад", color = colors.accentBlue) }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Раздел в разработке", color = colors.textMuted)
        }
    }
}

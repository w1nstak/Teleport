package com.teleport.messenger.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.screens.settings.SettingsGroupCard
import com.teleport.messenger.ui.screens.settings.SettingsV6SubTopBar
import com.teleport.messenger.ui.theme.ManropeFontFamily
import com.teleport.messenger.viewmodel.TeleportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val V6Bg = Color(0xFF0A0A12)
private val V6Card = Color(0xFF13131F)
private val V6Border = Color(0xFF22223A)
private val V6Muted = Color(0xFF6B69A0)
private val V6Text = Color(0xFFF0EFFF)
private val V6Blue = Color(0xFF5FA8FF)

@Composable
fun CallsListScreen(
    vm: TeleportViewModel,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onCall: (chatId: String, type: String) -> Unit = { _, _ -> },
) {
    val calls by vm.recentCalls.collectAsState()
    val chats by vm.chats.collectAsState()
    val timeFmt = remember { SimpleDateFormat("d MMM · HH:mm", Locale("ru")) }

    Column(
        Modifier
            .fillMaxSize()
            .background(V6Bg),
    ) {
        SettingsV6SubTopBar("Звонки", onBack)
        if (calls.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Call, null, tint = V6Muted, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Нет звонков", color = V6Muted, fontFamily = ManropeFontFamily)
                    Text(
                        "Начните звонок из чата",
                        color = V6Muted.copy(0.7f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(calls, key = { it.id }) { call ->
                    val title = chats.find { it.id == call.chatId }?.title ?: "Чат"
                    val isVideo = call.type == "video"
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(V6Card)
                            .border(1.dp, V6Border, RoundedCornerShape(16.dp))
                            .clickable { onCall(call.chatId, call.type) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            if (isVideo) Color(0xFF7C6FFF) else V6Blue,
                                            if (isVideo) Color(0xFF4A3AD6) else Color(0xFF2E5FE0),
                                        ),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                title,
                                color = V6Text,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = ManropeFontFamily,
                                fontSize = 15.sp,
                            )
                            Text(
                                buildString {
                                    append(if (isVideo) "Видеозвонок" else "Аудиозвонок")
                                    if (call.status == "ended") append(" · завершён")
                                    append(" · ")
                                    append(timeFmt.format(Date(call.startedAt)))
                                },
                                color = V6Muted,
                                fontSize = 12.sp,
                            )
                        }
                        IconButton(onClick = { onOpenChat(call.chatId) }) {
                            Icon(Icons.Default.Chat, "Чат", tint = V6Blue)
                        }
                        IconButton(onClick = { onCall(call.chatId, call.type) }) {
                            Icon(
                                if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                "Повторить",
                                tint = V6Blue,
                            )
                        }
                    }
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
    var renaming by remember { mutableStateOf<com.teleport.messenger.data.entity.ChatFolderEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(V6Bg),
    ) {
        SettingsV6SubTopBar("Папки", onBack)
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Новая папка", color = V6Muted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = V6Blue,
                    unfocusedBorderColor = V6Border,
                    focusedContainerColor = V6Card,
                    unfocusedContainerColor = V6Card,
                    focusedTextColor = V6Text,
                    unfocusedTextColor = V6Text,
                    cursorColor = V6Blue,
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        if (newName.isNotBlank()) {
                            vm.createFolder(newName.trim())
                            newName = ""
                        }
                    }) { Icon(Icons.Default.Add, null, tint = V6Blue) }
                },
            )
            Spacer(Modifier.height(16.dp))
            if (folders.isEmpty()) {
                Text("Создайте папку для группировки чатов", color = V6Muted, fontSize = 14.sp)
            } else {
                SettingsGroupCard {
                    folders.forEach { folder ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                folder.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp)
                                    .clickable {
                                        renaming = folder
                                        renameText = folder.name
                                    },
                                color = V6Text,
                                fontFamily = ManropeFontFamily,
                            )
                            TextButton(onClick = {
                                renaming = folder
                                renameText = folder.name
                            }) { Text("Изм.", color = V6Blue) }
                            TextButton(onClick = { vm.deleteFolder(folder.id) }) {
                                Text("Удалить", color = Color(0xFFFF7A7A))
                            }
                        }
                        HorizontalDivider(Modifier.padding(start = 16.dp), color = V6Border)
                    }
                }
            }
        }
    }

    renaming?.let { folder ->
        AlertDialog(
            onDismissRequest = { renaming = null },
            containerColor = V6Card,
            title = { Text("Переименовать", color = V6Text, fontFamily = ManropeFontFamily) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = V6Blue,
                        unfocusedBorderColor = V6Border,
                        focusedTextColor = V6Text,
                        unfocusedTextColor = V6Text,
                        cursorColor = V6Blue,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        vm.renameFolder(folder.id, renameText.trim())
                        renaming = null
                    }
                }) { Text("Сохранить", color = V6Blue) }
            },
            dismissButton = {
                TextButton(onClick = { renaming = null }) { Text("Отмена", color = V6Muted) }
            },
        )
    }
}

@Composable
fun StickersScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val emojis = listOf(
        "😀", "😂", "🥰", "😎", "🤩", "😇", "🤗", "🫠",
        "👍", "👎", "👏", "🙏", "💪", "🤝", "✌️", "🫶",
        "❤️", "🔥", "✨", "💫", "🎉", "🎊", "💯", "⚡",
        "🐱", "🐶", "🦊", "🐼", "🦄", "🐸", "🐝", "🦋",
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(V6Bg),
    ) {
        SettingsV6SubTopBar("Эмодзи", onBack)
        Text(
            "Нажмите, чтобы скопировать — вставьте в чат",
            color = V6Muted,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(emojis.size) { i ->
                val emoji = emojis[i]
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(V6Card)
                        .border(1.dp, V6Border, RoundedCornerShape(12.dp))
                        .clickable {
                            val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("emoji", emoji))
                            android.widget.Toast
                                .makeText(context, "$emoji скопировано", android.widget.Toast.LENGTH_SHORT)
                                .show()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, fontSize = 26.sp)
                }
            }
        }
    }
}

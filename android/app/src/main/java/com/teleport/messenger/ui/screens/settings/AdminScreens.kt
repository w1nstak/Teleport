package com.teleport.messenger.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.viewmodel.TeleportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val user by vm.currentUser().collectAsState(initial = null)
    val stats by vm.adminStats.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(Unit) { vm.loadAdminStats() }

    val ownerLabel = remember(user?.username) {
        user?.username?.let { "@$it" } ?: "@w1nst"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(SettingsV6Palette.Bg),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0x0DFFFFFF))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color(0xFFEDEBFF), modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("Админ-панель", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SettingsV6Palette.TextPrimary)
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF2A1A32), Color(0xFF13131F))))
                        .border(1.dp, Color(0xFF3D2A45), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFFF6F9F), Color(0xFFD6316E)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Dashboard, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("$ownerLabel · владелец", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SettingsV6Palette.TextPrimary)
                        Text("Полный доступ к статистике сервера", fontSize = 12.sp, color = SettingsV6Palette.TextMuted)
                    }
                }
            }

            if (loading && stats == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SettingsV6Palette.ToggleOnStart, strokeWidth = 2.dp)
                    }
                }
            }

            error?.let { msg ->
                item {
                    Text(msg, color = Color(0xFFFF7A7A), fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }

            stats?.let { s ->
                item { SettingsV6GroupLabel("СЕРВЕР") }
                item {
                    SettingsV6StatCard(
                        listOf(
                            "Пользователей" to s.usersTotal.toString(),
                            "Аккаунтов" to s.accountsTotal.toString(),
                            "Чатов" to s.chatsTotal.toString(),
                            "Онлайн сейчас" to s.onlineNow.toString(),
                        ),
                    )
                }
                item { SettingsV6GroupLabel("СООБЩЕНИЯ") }
                item {
                    SettingsV6StatCard(
                        listOf(
                            "Всего" to s.messagesTotal.toString(),
                            "Сегодня" to s.messagesToday.toString(),
                            "WebSocket" to s.wsConnections.toString(),
                        ),
                    )
                }
                val extra = buildList {
                    s.lastMessageAt?.let { ts ->
                        val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
                        add("Последнее сообщение" to fmt.format(Date(ts)))
                    }
                    s.publicUrl?.let { add("Сервер" to it) }
                    s.ownerUsername?.let { add("Владелец" to "@$it") }
                }
                if (extra.isNotEmpty()) {
                    item { SettingsV6GroupLabel("ИНФО") }
                    item { SettingsV6StatCard(extra) }
                }
            }

            item {
                TextButton(onClick = { vm.loadAdminStats() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Обновить", color = SettingsV6Palette.ToggleOnStart, fontWeight = FontWeight.Bold)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

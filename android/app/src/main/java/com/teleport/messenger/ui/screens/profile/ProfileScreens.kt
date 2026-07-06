package com.teleport.messenger.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.UserEntity
import com.teleport.messenger.ui.components.AppFloatingBottomNav
import com.teleport.messenger.ui.components.MainTab
import com.teleport.messenger.ui.theme.TeleportAppTheme
import com.teleport.messenger.util.PrivacyHelper
import com.teleport.messenger.viewmodel.TeleportViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    vm: TeleportViewModel,
    onChats: () -> Unit,
    onContacts: () -> Unit,
    onSettings: () -> Unit,
    onCalls: () -> Unit,
    onEdit: () -> Unit,
    onUsername: () -> Unit,
    onAppearance: () -> Unit,
) {
    val user by vm.currentUser().collectAsState(initial = null)
    val account by vm.activeAccount.collectAsState()
    val settings by vm.settings().collectAsState(initial = null)
    val colors = TeleportAppTheme.colors
    val context = LocalContext.current
    val name = user?.displayName ?: "Пользователь"
    val username = user?.username
    val online = user?.isOnline == true && settings?.hideOnlineStatus != true
    if (user == null) return
    val statusText = if (online) "в сети" else PrivacyHelper.onlineStatus(user, true, false, false)

    Scaffold(
        containerColor = colors.screenBg,
        bottomBar = {
            AppFloatingBottomNav(
                selected = MainTab.Profile,
                onChats = onChats,
                onContacts = onContacts,
                onProfile = {},
                onSettings = onSettings,
                onCalls = onCalls,
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Box(Modifier.fillMaxWidth().height(ProfileHeroHeight)) {
                    ProfileHeroImage(name, user?.avatarUri, Modifier.matchParentSize())
                    ProfileShareChip(
                        onClick = {
                            val shareText = buildString {
                                append(name)
                                username?.let { append("\n@$it") }
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    },
                                    "Поделиться",
                                ),
                            )
                        },
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp, top = 48.dp),
                    )
                    Column(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name.uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            if (user?.isPremium == true) {
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.Verified, null, tint = colors.accentBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (online) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).background(colors.onlineGreen, androidx.compose.foundation.shape.CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text(statusText, color = colors.onlineGreen, fontSize = 14.sp)
                            }
                        } else {
                            Text(statusText, color = Color.White.copy(0.8f), fontSize = 14.sp)
                        }
                        username?.let {
                            Text("@$it", color = Color.White.copy(0.75f), fontSize = 15.sp)
                        }
                    }
                }
            }
            item {
                val cardShape = RoundedCornerShape(12.dp)
                Column(Modifier.padding(horizontal = 16.dp)) {
                    user?.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                        SlimSectionHeader("О себе")
                        Surface(shape = cardShape, color = colors.cardBg) {
                            Text(bio, modifier = Modifier.padding(16.dp), color = colors.textPrimary, lineHeight = 22.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    Surface(shape = cardShape, color = colors.cardBg) {
                        Column {
                            SlimProfileInfoRow(
                                Icons.Default.Phone,
                                Color(0xFF34C759),
                                "Номер",
                                if (account?.phone?.startsWith("web:") == true || user?.anonymousMode == true) "Скрыт" else account?.phone ?: "Скрыт",
                            )
                            HorizontalDivider(Modifier.padding(start = 60.dp), color = colors.divider)
                            SlimProfileInfoRow(
                                Icons.Default.AlternateEmail,
                                Color(0xFF007AFF),
                                "Имя пользователя",
                                username?.let { "@$it" } ?: "Не задан",
                                onClick = onUsername,
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    SlimSectionHeader("Оформление")
                    Surface(shape = cardShape, color = colors.cardBg) {
                        SlimProfileInfoRow(
                            Icons.Default.TextFields,
                            Color(0xFFFF3B30),
                            "Шрифт",
                            "Системный",
                            onClick = onAppearance,
                            trailing = {
                                Text("Показать", color = colors.accentBlue, fontSize = 15.sp)
                            },
                        )
                    }
                    Spacer(Modifier.height(88.dp))
                }
            }
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

    LaunchedEffect(chatId, me?.id) {
        me?.id?.let { id -> vm.getContactForChat(chatId, id) { contact = it } }
    }

    val displayUser = contact ?: UserEntity(
        id = "unknown",
        accountId = "",
        displayName = chat?.title ?: "Контакт",
        username = null,
    )
    val colors = TeleportAppTheme.colors

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillMaxWidth().height(ProfileHeroHeight)) {
                ProfileHeroImage(displayUser.displayName, displayUser.avatarUri, Modifier.matchParentSize())
                ProfileTopActions(onBack = onBack, onEdit = null, modifier = Modifier.align(Alignment.TopCenter))
                Column(
                    Modifier.align(Alignment.BottomCenter).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(displayUser.displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    displayUser.username?.let { Text("@$it", color = Color.White.copy(0.8f)) }
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.cardBg,
            ) {
                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = onMessage) { Text("Написать") }
                    TextButton(onClick = { onCall("voice") }) { Text("Звонок") }
                    TextButton(onClick = { onCall("video") }) { Text("Видео") }
                }
            }
        }
    }
}

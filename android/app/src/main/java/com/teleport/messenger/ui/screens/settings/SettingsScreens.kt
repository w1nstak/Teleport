package com.teleport.messenger.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.data.entity.AppSettingsEntity
import com.teleport.messenger.data.entity.UserEntity
import com.teleport.messenger.ui.components.*
import com.teleport.messenger.ui.theme.TeleportAppTheme
import com.teleport.messenger.ui.theme.ColorThemes
import com.teleport.messenger.util.hashPin
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr
import com.teleport.messenger.ui.theme.themeModeLabel
import com.teleport.messenger.viewmodel.TeleportViewModel

@Composable
fun SettingsScreen(
    vm: TeleportViewModel,
    onChats: () -> Unit,
    onContacts: () -> Unit,
    onProfile: () -> Unit,
    onCalls: () -> Unit,
    onFavorites: () -> Unit,
    onAppearance: () -> Unit,
    onStickers: () -> Unit,
    onLocalization: () -> Unit,
    onEditProfile: () -> Unit,
    onFolders: () -> Unit,
    onPrivacy: () -> Unit,
    onSessions: () -> Unit,
    onNotifications: () -> Unit,
    onBlocked: () -> Unit,
) {
    val settings by vm.settings().collectAsState(initial = null)
    val colors = TeleportAppTheme.colors
    var notificationsOn by remember(settings) { mutableStateOf(settings?.notificationsEnabled ?: true) }
    var powerSaving by remember(settings) { mutableStateOf(settings?.powerSavingEnabled ?: false) }

    fun save(block: (AppSettingsEntity) -> AppSettingsEntity) {
        settings?.let { vm.updateSettings(block(it)) }
    }

    SettingsScreenScaffold(
        bottomBar = {
            AppFloatingBottomNav(
                selected = MainTab.Settings,
                onChats = onChats,
                onContacts = onContacts,
                onProfile = onProfile,
                onSettings = {},
                onCalls = onCalls,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item {
                Text(
                    appStr(AppStringKey.SETTINGS),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                )
            }
            item {
                SettingsGroupCard {
                    SettingsRow(Icons.Default.Star, Color(0xFF007AFF), "Избранное", onFavorites, showDivider = false)
                }
            }
            item {
                SettingsGroupCard {
                    SettingsRow(Icons.Default.Palette, Color(0xFFAF52DE), "Темы", onAppearance)
                    SettingsRow(Icons.Default.EmojiEmotions, Color(0xFFFFCC00), "Эмодзи и стикеры", onStickers)
                    SettingsRow(Icons.Default.GridView, Color(0xFF5AC8FA), "Навигация", onLocalization)
                    SettingsRow(Icons.Default.Person, Color(0xFF007AFF), "Профиль", onEditProfile)
                    SettingsRow(Icons.Default.Folder, Color(0xFFFF9500), "Папки", onFolders)
                    SettingsRow(Icons.Default.Lock, Color(0xFF34C759), "Конфиденциальность", onPrivacy)
                    SettingsRow(Icons.Default.Language, Color(0xFF007AFF), "Язык", onLocalization, showDivider = false)
                }
            }
            item {
                SettingsGroupCard {
                    SettingsRow(Icons.Default.Devices, Color(0xFF5856D6), "Устройства", onSessions)
                    SettingsToggleRow(
                        Icons.Default.Notifications,
                        Color(0xFFFF3B30),
                        "Уведомления",
                        notificationsOn,
                        onCheckedChange = {
                            notificationsOn = it
                            save { s -> s.copy(notificationsEnabled = it) }
                        },
                    )
                    SettingsToggleRow(
                        Icons.Default.Bolt,
                        Color(0xFF34C759),
                        "Энергосбережение",
                        powerSaving,
                        onCheckedChange = {
                            powerSaving = it
                            save { s -> s.copy(powerSavingEnabled = it) }
                        },
                    )
                    SettingsRow(Icons.Default.Block, Color(0xFFFF9500), "Заблокированные", onBlocked, showDivider = false)
                }
            }
            item {
                TextButton(
                    onClick = onNotifications,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Подробные настройки уведомлений", color = colors.accentBlue)
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun AppearanceScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val settings by vm.settings().collectAsState(initial = null)
    var themeMode by remember(settings) { mutableStateOf(settings?.themeMode ?: "system") }
    var dynamic by remember(settings) { mutableStateOf(settings?.useDynamicColor ?: true) }
    var colorId by remember(settings) { mutableStateOf(settings?.colorThemeId ?: "slimchat") }
    val colors = TeleportAppTheme.colors

    fun applyTheme(mode: String? = null, dyn: Boolean? = null, color: String? = null) {
        settings?.let {
            vm.updateSettings(it.copy(
                themeMode = mode ?: themeMode,
                useDynamicColor = dyn ?: dynamic,
                colorThemeId = color ?: colorId,
            ))
        }
    }

    Scaffold(containerColor = TeleportAppTheme.colors.screenBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TeleportTopBar("Оформление", onBack)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text("Тема приложения", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThemeModeCard(
                            title = "Светлая",
                            icon = Icons.Default.LightMode,
                            previewBg = Color(0xFFF4F5FA),
                            previewCard = Color.White,
                            previewText = Color(0xFF1A1D26),
                            selected = themeMode == "light",
                            onClick = { themeMode = "light"; applyTheme(mode = "light") },
                            modifier = Modifier.weight(1f),
                        )
                        ThemeModeCard(
                            title = "Тёмная",
                            icon = Icons.Default.DarkMode,
                            previewBg = Color(0xFF0D0F14),
                            previewCard = Color(0xFF1A1D28),
                            previewText = Color(0xFFE8ECF4),
                            selected = themeMode == "dark",
                            onClick = { themeMode = "dark"; applyTheme(mode = "dark") },
                            modifier = Modifier.weight(1f),
                        )
                        ThemeModeCard(
                            title = "Системная",
                            icon = Icons.Default.SettingsBrightness,
                            previewBg = Color(0xFF6B7280),
                            previewCard = Color(0xFF9CA3AF),
                            previewText = Color.White,
                            selected = themeMode == "system",
                            onClick = { themeMode = "system"; applyTheme(mode = "system") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    SettingsGroupCard {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Material You", fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                Text("Цвета из обоев Android", fontSize = 13.sp, color = colors.textMuted)
                            }
                            Switch(checked = dynamic, onCheckedChange = { dynamic = it; applyTheme(dyn = it) })
                        }
                    }
                }
                item {
                    Text("Акцентный цвет", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                }
                items(ColorThemes) { theme ->
                    val selected = colorId == theme.id
                    SettingsGroupCard {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { colorId = theme.id; applyTheme(color = theme.id) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(theme.primary),
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(theme.name, modifier = Modifier.weight(1f), color = colors.textPrimary)
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, null, tint = colors.accentBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    previewBg: Color,
    previewCard: Color,
    previewText: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) TeleportAppTheme.colors.accentBlue else Color.Transparent
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .background(TeleportAppTheme.colors.cardBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(previewBg)
                .padding(8.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(previewCard)
                    .align(Alignment.TopCenter),
            )
            Box(
                Modifier
                    .width(40.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(previewText.copy(0.6f))
                    .align(Alignment.BottomStart),
            )
        }
        Spacer(Modifier.height(8.dp))
        Icon(icon, title, tint = if (selected) TeleportAppTheme.colors.accentBlue else TeleportAppTheme.colors.textMuted, modifier = Modifier.size(20.dp))
        Text(title, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = TeleportAppTheme.colors.textPrimary)
    }
}

@Composable
fun EditProfileScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val user by vm.currentUser().collectAsState(initial = null)
    var name by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var status by remember(user) { mutableStateOf(user?.status ?: "") }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TeleportTopBar("Редактировать", onBack)
        TeleportTextField(name, { name = it }, "Имя")
        Spacer(Modifier.height(12.dp))
        TeleportTextField(bio, { bio = it }, "Описание")
        Spacer(Modifier.height(12.dp))
        TeleportTextField(status, { status = it }, "Статус")
        Spacer(Modifier.height(24.dp))
        TeleportButton("Сохранить", {
            user?.let { vm.updateProfile(it.copy(displayName = name, bio = bio, status = status)) }
            onBack()
        })
    }
}

@Composable
fun UsernameScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val user by vm.currentUser().collectAsState(initial = null)
    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var available by remember { mutableStateOf<Boolean?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TeleportTopBar("@username", onBack)
        Text("Ваш профиль: teleport.app/@username", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        TeleportTextField(username, {
            username = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase()
            vm.checkUsername(username, user?.id ?: "") { available = it }
        }, "Username")
        available?.let {
            Text(if (it) "✓ Доступен" else "✗ Занят", color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        TeleportButton("Сохранить", {
            user?.let { vm.updateProfile(it.copy(username = username)) }
            onBack()
        }, enabled = available == true)
    }
}

@Composable
fun SecurityScreen(
    vm: TeleportViewModel,
    onBack: () -> Unit,
    onSessions: () -> Unit,
) {
    val settings by vm.settings().collectAsState(initial = null)
    var pin by remember { mutableStateOf("") }
    var biometric by remember(settings) { mutableStateOf(settings?.biometricEnabled ?: false) }
    var appLock by remember(settings) { mutableStateOf(settings?.appLockEnabled ?: false) }
    val colors = TeleportAppTheme.colors

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Защита аккаунта", onBack)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    "Защита от взлома: блокировка телефона, контроль сессий и надёжный пароль.",
                    color = colors.textMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            item {
                SettingsGroupCard {
                    Text(
                        "Блокировка приложения",
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
                    )
                    Text(
                        "Даже если телефон в руках у другого — без PIN не откроют чаты",
                        fontSize = 13.sp,
                        color = colors.textMuted,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    )
                    TeleportTextField(
                        pin,
                        { pin = it.filter { c -> c.isDigit() }.take(6) },
                        "PIN-код (4–6 цифр)",
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    SwitchRow("Биометрия (отпечаток / Face)", biometric) { biometric = it }
                    SwitchRow("Требовать PIN при входе", appLock) { appLock = it }
                    Spacer(Modifier.height(8.dp))
                }
            }
            item {
                SettingsGroupCard {
                    SettingsRow(
                        Icons.Default.Devices,
                        Color(0xFF34C759),
                        "Активные сессии",
                        onSessions,
                        showDivider = false,
                    )
                }
            }
            item {
                Text("Рекомендации", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            }
            item {
                SettingsGroupCard {
                    listOf(
                        "Пароль — не короче 8 символов, буквы и цифры",
                        "Не сообщайте пароль и @username никому",
                        "Периодически проверяйте активные сессии",
                        "Включите блокировку экрана на телефоне",
                    ).forEachIndexed { i, tip ->
                        Text(tip, Modifier.padding(16.dp), color = colors.textPrimary, fontSize = 14.sp)
                        if (i < 3) HorizontalDivider(color = colors.divider)
                    }
                }
            }
            item {
                TeleportButton("Сохранить", {
                    if (appLock && pin.isNotBlank() && pin.length < 4) return@TeleportButton
                    settings?.let {
                        vm.updateSettings(it.copy(
                            pinHash = when {
                                pin.isNotBlank() -> hashPin(pin)
                                !appLock -> null
                                else -> it.pinHash
                            },
                            biometricEnabled = biometric,
                            appLockEnabled = appLock,
                        ))
                    }
                    onBack()
                }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
fun SessionsScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val sessions by vm.sessions().collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Активные сессии", onBack)
        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет активных сессий", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(sessions) { session ->
                    ListItem(
                        headlineContent = { Text(session.deviceName) },
                        supportingContent = {
                            Text("${session.platform} • ${if (session.isCurrent) "Текущая" else "Активна"}")
                        },
                        leadingContent = { Icon(Icons.Default.PhoneAndroid, null) },
                        trailingContent = {
                            if (!session.isCurrent) {
                                TextButton(onClick = { vm.terminateSession(session.id) }) {
                                    Text("Завершить")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val settings by vm.settings().collectAsState(initial = null)
    var enabled by remember(settings) { mutableStateOf(settings?.notificationsEnabled ?: true) }
    var silent by remember(settings) { mutableStateOf(settings?.silentMode ?: false) }
    var vibration by remember(settings) { mutableStateOf(settings?.vibrationEnabled ?: true) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TeleportTopBar("Уведомления", onBack)
        SwitchRow("Push-уведомления", enabled) { enabled = it }
        SwitchRow("Беззвучный режим", silent) { silent = it }
        SwitchRow("Вибрация", vibration) { vibration = it }
        Spacer(Modifier.height(24.dp))
        TeleportButton("Сохранить", {
            settings?.let { vm.updateSettings(it.copy(notificationsEnabled = enabled, silentMode = silent, vibrationEnabled = vibration)) }
            onBack()
        })
    }
}

@Composable
fun SearchUsersScreen(vm: TeleportViewModel, onBack: () -> Unit, onOpenChat: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<UserEntity>()) }

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Поиск пользователей", onBack)
        TeleportTextField(query, { query = it; vm.searchUsers(query) { results = it } }, "Имя или @username", Modifier.padding(16.dp))
        if (results.isEmpty() && query.length >= 2) {
            Text(
                "Пользователь не найден. Друг должен зарегистрироваться в Teleport и задать @username.",
                Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
        LazyColumn {
            items(results) { user ->
                ListItem(
                    headlineContent = { Text(user.displayName) },
                    supportingContent = { user.username?.let { Text("@$it") } },
                    leadingContent = { TeleportAvatar(user.displayName, user.isPremium) },
                    modifier = Modifier.clickable {
                        vm.openPrivateChat(user.id) { chatId -> onOpenChat(chatId) }
                    },
                )
            }
        }
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val colors = TeleportAppTheme.colors
    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Справка", onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Teleport Messenger", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Защищённый мессенджер: PIN, сессии, звонки и чаты.",
                    color = colors.textMuted,
                )
            }
            item {
                SettingsGroupCard {
                    listOf(
                        "Настройки → Защита аккаунта → PIN и биометрия",
                        "Настройки → Активные сессии → завершить чужие входы",
                        "Long-press на чат → закрепить / архив / папка",
                        "Вложения → спойлер, фото, видео, голос",
                    ).forEachIndexed { i, tip ->
                        Text(tip, Modifier.padding(16.dp), color = colors.textPrimary)
                        if (i < 3) HorizontalDivider(color = colors.divider)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountsScreen(
    vm: TeleportViewModel,
    onBack: () -> Unit,
    onAddAccount: () -> Unit,
    onSwitched: () -> Unit,
) {
    val accounts by vm.allAccounts.collectAsState()
    val active by vm.activeAccount.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TeleportTopBar("Аккаунты", onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(accounts) { account ->
                Card(
                    onClick = {
                        if (account.id != active?.id) vm.switchAccount(account.id, onSwitched)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (account.isActive) TeleportAppTheme.colors.accentBlue.copy(0.12f)
                        else TeleportAppTheme.colors.cardBg,
                    ),
                ) {
                    ListItem(
                        headlineContent = { Text(account.phone) },
                        supportingContent = {
                            Text(if (account.isActive) "Текущий аккаунт" else "Нажмите для переключения")
                        },
                        leadingContent = { TeleportAvatar(account.phone.takeLast(2), size = 44.dp) },
                    )
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                TeleportButton("Добавить аккаунт", onAddAccount)
            }
        }
    }
}

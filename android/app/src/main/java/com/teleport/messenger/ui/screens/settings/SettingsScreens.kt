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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.Chat
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
    onSecurity: () -> Unit,
    onAccounts: () -> Unit,
    onPremium: () -> Unit,
    onStars: () -> Unit,
    onHelp: () -> Unit,
    onLogout: () -> Unit,
    onAdmin: () -> Unit,
) {
    val settings by vm.settings().collectAsState(initial = null)
    val user by vm.currentUser().collectAsState(initial = null)
    val isOwner by vm.isOwner.collectAsState()
    val showAdmin = isOwner || isOwnerUsername(user?.username)
    var notificationsOn by remember(settings) { mutableStateOf(settings?.notificationsEnabled ?: true) }

    fun save(block: (AppSettingsEntity) -> AppSettingsEntity) {
        settings?.let { vm.updateSettings(block(it)) }
    }

    val accountRows = buildList {
        add(
            SettingsV6Row(
                icon = Icons.Outlined.Person,
                gradStart = Color(0xFF5FA8FF),
                gradEnd = Color(0xFF2E5FE0),
                title = "Профиль",
                onClick = onEditProfile,
            ),
        )
        add(
            SettingsV6Row(
                icon = Icons.Outlined.Shield,
                gradStart = Color(0xFF7C6FFF),
                gradEnd = Color(0xFF4A3AD6),
                title = "Конфиденциальность",
                onClick = onPrivacy,
            ),
        )
        add(
            SettingsV6Row(
                icon = Icons.Outlined.Notifications,
                gradStart = Color(0xFFFF9F5F),
                gradEnd = Color(0xFFE0692E),
                title = "Уведомления",
                toggle = notificationsOn,
                onToggle = {
                    notificationsOn = it
                    save { s -> s.copy(notificationsEnabled = it) }
                },
            ),
        )
        add(
            SettingsV6Row(
                icon = Icons.AutoMirrored.Outlined.Chat,
                gradStart = Color(0xFF4FD9A8),
                gradEnd = Color(0xFF1FA878),
                title = "Чаты",
                onClick = onFolders,
            ),
        )
        add(
            SettingsV6Row(
                icon = Icons.Outlined.Storage,
                gradStart = Color(0xFF5FD1FF),
                gradEnd = Color(0xFF2E9EE0),
                title = "Хранилище",
                value = "2.4/5 ГБ",
                onClick = onFolders,
            ),
        )
    }

    val appearanceRows = listOf(
        SettingsV6Row(
            icon = Icons.Outlined.DarkMode,
            gradStart = Color(0xFFB18BFF),
            gradEnd = Color(0xFF7A52E0),
            title = "Тема",
            value = themeModeLabel(settings?.themeMode ?: "dark"),
            onClick = onAppearance,
        ),
        SettingsV6Row(
            icon = Icons.Outlined.Image,
            gradStart = Color(0xFFFF7CB8),
            gradEnd = Color(0xFFE03E85),
            title = "Обои чатов",
            onClick = onAppearance,
        ),
        SettingsV6Row(
            icon = Icons.Outlined.Tune,
            gradStart = Color(0xFF5FD1FF),
            gradEnd = Color(0xFF2E9EE0),
            title = "Размер шрифта",
            value = "Средний",
            onClick = onLocalization,
        ),
    )

    val supportRows = listOf(
        SettingsV6Row(
            icon = Icons.Outlined.HelpOutline,
            gradStart = Color(0xFF8280B4),
            gradEnd = Color(0xFF5C5A88),
            title = "Центр помощи",
            onClick = onHelp,
        ),
        SettingsV6Row(
            icon = Icons.Outlined.Info,
            gradStart = Color(0xFF8280B4),
            gradEnd = Color(0xFF5C5A88),
            title = "О приложении",
            onClick = onHelp,
        ),
        SettingsV6Row(
            icon = Icons.Outlined.PersonAdd,
            gradStart = Color(0xFF5FA8FF),
            gradEnd = Color(0xFF2E5FE0),
            title = "Пригласить друзей",
            onClick = onContacts,
        ),
    )

    Scaffold(
        containerColor = SettingsV6Palette.Bg,
        bottomBar = {
            AppFloatingBottomNav(
                selected = MainTab.Settings,
                onChats = onChats,
                onContacts = onContacts,
                onSettings = {},
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SettingsV6Palette.Bg),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item { SettingsV6Title(appStr(AppStringKey.SETTINGS)) }
            item {
                SettingsV6ProfileCard(
                    initials = profileInitials(user?.displayName ?: user?.username ?: "?"),
                    name = "Мой профиль",
                    username = user?.username,
                    onClick = onEditProfile,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            item {
                SettingsV6GroupLabel("АККАУНТ")
                SettingsV6GroupCard(accountRows)
                Spacer(Modifier.height(20.dp))
            }
            if (showAdmin) {
                item {
                    SettingsV6GroupLabel("АДМИНИСТРИРОВАНИЕ")
                    SettingsV6GroupCard(
                        listOf(
                            SettingsV6Row(
                                icon = Icons.Outlined.Apps,
                                gradStart = Color(0xFFFF6F9F),
                                gradEnd = Color(0xFFD6316E),
                                title = "Админ-панель",
                                onClick = onAdmin,
                            ),
                        ),
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
            item {
                SettingsV6BalanceCard(
                    balance = user?.starsBalance ?: 0L,
                    onTopUp = onStars,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
            item {
                SettingsV6GroupLabel("ОФОРМЛЕНИЕ")
                SettingsV6GroupCard(appearanceRows)
                Spacer(Modifier.height(20.dp))
            }
            item {
                SettingsV6GroupLabel("ПОДДЕРЖКА")
                SettingsV6GroupCard(supportRows)
                Spacer(Modifier.height(20.dp))
            }
            item { SettingsV6Logout(onLogout) }
            item { Spacer(Modifier.height(8.dp)) }
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

    Scaffold(containerColor = SettingsV6Palette.Bg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(SettingsV6Palette.Bg)) {
            SettingsV6SubTopBar("Оформление", onBack)
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text("Тема приложения", fontWeight = FontWeight.SemiBold, color = SettingsV6Palette.TextPrimary)
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
                    Text("Акцентный цвет", fontWeight = FontWeight.SemiBold, color = SettingsV6Palette.TextPrimary)
                }
                items(ColorThemes) { theme ->
                    val selected = colorId == theme.id
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SettingsV6Palette.CardBg)
                            .border(1.dp, SettingsV6Palette.CardBorder, RoundedCornerShape(16.dp))
                            .clickable { colorId = theme.id; applyTheme(color = theme.id) }
                            .padding(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(theme.primary),
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(theme.name, modifier = Modifier.weight(1f), color = SettingsV6Palette.TextPrimary)
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, null, tint = SettingsV6Palette.ToggleOnStart)
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
    val borderColor = if (selected) SettingsV6Palette.ToggleOnStart else Color.Transparent
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .background(SettingsV6Palette.CardBg)
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
        Icon(icon, title, tint = if (selected) SettingsV6Palette.ToggleOnStart else SettingsV6Palette.TextMuted, modifier = Modifier.size(20.dp))
        Text(title, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = SettingsV6Palette.TextPrimary)
    }
}

@Composable
fun EditProfileScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val user by vm.currentUser().collectAsState(initial = null)
    var name by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var status by remember(user) { mutableStateOf(user?.status ?: "") }

    SettingsV6Screen(title = "Редактировать", onBack = onBack) {
        Column(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                name, { name = it },
                label = { Text("Имя", color = SettingsV6Palette.TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = v6TextFieldColors(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                bio, { bio = it },
                label = { Text("Описание", color = SettingsV6Palette.TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = v6TextFieldColors(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                status, { status = it },
                label = { Text("Статус", color = SettingsV6Palette.TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = v6TextFieldColors(),
            )
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SettingsV6Palette.ToggleOnStart)
                    .clickable {
                        user?.let { vm.updateProfile(it.copy(displayName = name, bio = bio, status = status)) }
                        onBack()
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun v6TextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SettingsV6Palette.TextPrimary,
    unfocusedTextColor = SettingsV6Palette.TextPrimary,
    focusedBorderColor = SettingsV6Palette.ToggleOnStart,
    unfocusedBorderColor = SettingsV6Palette.CardBorder,
    cursorColor = SettingsV6Palette.ToggleOnStart,
    focusedContainerColor = SettingsV6Palette.CardBg,
    unfocusedContainerColor = SettingsV6Palette.CardBg,
)

@Composable
fun UsernameScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val user by vm.currentUser().collectAsState(initial = null)
    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var available by remember { mutableStateOf<Boolean?>(null) }

    SettingsV6Screen(title = "@username", onBack = onBack) {
        Column(Modifier.fillMaxWidth()) {
            Text("Ваш профиль: teleport.app/@username", color = SettingsV6Palette.TextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                username,
                {
                    username = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase()
                    vm.checkUsername(username, user?.id ?: "") { available = it }
                },
                label = { Text("Username", color = SettingsV6Palette.TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = v6TextFieldColors(),
            )
            available?.let {
                Text(
                    if (it) "✓ Доступен" else "✗ Занят",
                    color = if (it) Color(0xFF2ED974) else Color(0xFFFF7A7A),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (available == true) SettingsV6Palette.ToggleOnStart else SettingsV6Palette.CardBorder)
                    .clickable(enabled = available == true) {
                        user?.let { vm.updateProfile(it.copy(username = username)) }
                        onBack()
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
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
    var pinError by remember { mutableStateOf<String?>(null) }
    var biometric by remember(settings) { mutableStateOf(settings?.biometricEnabled ?: false) }
    var appLock by remember(settings) { mutableStateOf(settings?.appLockEnabled ?: false) }
    val colors = TeleportAppTheme.colors

    Column(Modifier.fillMaxSize().background(SettingsV6Palette.Bg)) {
        SettingsV6SubTopBar("Защита аккаунта", onBack)
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    "Защита от взлома: блокировка телефона, контроль сессий и надёжный пароль.",
                    color = SettingsV6Palette.TextMuted,
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
                    if (appLock && pin.isNotBlank() && pin.length < 4) {
                        pinError = "PIN должен содержать от 4 до 6 цифр"
                        return@TeleportButton
                    }
                    if (appLock && pin.isBlank() && settings?.pinHash.isNullOrBlank()) {
                        pinError = "Задайте PIN для блокировки приложения"
                        return@TeleportButton
                    }
                    pinError = null
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
            pinError?.let { msg ->
                item {
                    Text(msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 4.dp))
                }
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

    SettingsV6Screen(title = "Активные сессии", onBack = onBack) {
        if (sessions.isEmpty()) {
            Text("Нет активных сессий", color = SettingsV6Palette.TextMuted, modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sessions) { session ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SettingsV6Palette.CardBg)
                            .border(1.dp, SettingsV6Palette.CardBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                    ) {
                        Text(session.deviceName, fontWeight = FontWeight.SemiBold, color = SettingsV6Palette.TextPrimary)
                        Text(
                            "${session.platform} • ${if (session.isCurrent) "Текущая" else "Активна"}",
                            fontSize = 13.sp,
                            color = SettingsV6Palette.TextMuted,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (!session.isCurrent) {
                            Text(
                                "Завершить",
                                color = Color(0xFFFF7A7A),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable { vm.terminateSession(session.id) },
                            )
                        }
                    }
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

    SettingsV6Screen(title = "Уведомления", onBack = onBack) {
        SettingsV6GroupCard(
            listOf(
                SettingsV6Row(Icons.Outlined.Notifications, Color(0xFFFF9F5F), Color(0xFFE0692E), "Push-уведомления", toggle = enabled, onToggle = { enabled = it }),
                SettingsV6Row(Icons.Outlined.VolumeOff, Color(0xFF8280B4), Color(0xFF5C5A88), "Беззвучный режим", toggle = silent, onToggle = { silent = it }),
                SettingsV6Row(Icons.Outlined.Vibration, Color(0xFF5FD1FF), Color(0xFF2E9EE0), "Вибрация", toggle = vibration, onToggle = { vibration = it }),
            ),
        )
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SettingsV6Palette.ToggleOnStart)
                .clickable {
                    settings?.let { vm.updateSettings(it.copy(notificationsEnabled = enabled, silentMode = silent, vibrationEnabled = vibration)) }
                    onBack()
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Сохранить", fontWeight = FontWeight.Bold, color = Color.White)
        }
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
    SettingsV6Screen(title = "Справка", onBack = onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Teleport Messenger", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SettingsV6Palette.TextPrimary)
            Text(
                "Защищённый мессенджер: PIN, сессии, звонки и чаты.",
                color = SettingsV6Palette.TextMuted,
            )
            SettingsV6GroupCard(
                listOf(
                    SettingsV6Row(Icons.Outlined.Lock, Color(0xFF7C6FFF), Color(0xFF4A3AD6), "PIN и биометрия", value = "Профиль → Защита"),
                    SettingsV6Row(Icons.Outlined.Devices, Color(0xFF4FD9A8), Color(0xFF1FA878), "Активные сессии", value = "Профиль"),
                    SettingsV6Row(Icons.Outlined.PushPin, Color(0xFF5FA8FF), Color(0xFF2E5FE0), "Закрепить чат", value = "Long-press"),
                    SettingsV6Row(Icons.Outlined.AttachFile, Color(0xFFFF7CB8), Color(0xFFE03E85), "Вложения", value = "Фото, видео, голос"),
                ),
            )
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

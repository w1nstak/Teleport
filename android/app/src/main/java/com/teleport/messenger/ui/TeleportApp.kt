package com.teleport.messenger.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teleport.messenger.TeleportApplication
import com.teleport.messenger.ui.screens.auth.*
import com.teleport.messenger.ui.screens.chat.*
import com.teleport.messenger.ui.screens.premium.*
import com.teleport.messenger.ui.screens.privacy.*
import com.teleport.messenger.ui.screens.profile.*
import com.teleport.messenger.ui.screens.main.*
import com.teleport.messenger.ui.screens.settings.*
import com.teleport.messenger.ui.strings.mergeAppStrings
import com.teleport.messenger.ui.theme.TeleportTheme
import com.teleport.messenger.util.AppLockGate
import com.teleport.messenger.viewmodel.TeleportViewModel

object Routes {
    const val AUTH = "auth"
    const val AUTH_USERNAME = "auth_username"
    const val AUTH_REGISTER = "auth_register"
    const val AUTH_PHONE = "auth_phone"
    const val CHATS = "chats"
    const val CONTACTS = "contacts"
    const val CALLS = "calls"
    const val ARCHIVE = "archive"
    const val CHAT = "chat/{chatId}"
    const val CHAT_SEARCH = "chat/{chatId}/search"
    const val CHAT_GALLERY = "chat/{chatId}/gallery"
    const val CALL = "call/{chatId}/{type}"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val USERNAME = "username"
    const val APPEARANCE = "appearance"
    const val LOCALIZATION = "localization"
    const val SECURITY = "security"
    const val SESSIONS = "sessions"
    const val NOTIFICATIONS = "notifications"
    const val SEARCH_USERS = "search_users"
    const val ACCOUNTS = "accounts"
    const val PREMIUM = "premium"
    const val STARS = "stars"
    const val GIFTS = "gifts"
    const val GIFT_COLLECTION = "gift_collection"
    const val CONTACT_PROFILE = "contact/{chatId}"
    const val PRIVACY = "privacy"
    const val BLOCKED = "blocked"
    const val MARKETPLACE = "marketplace"
    const val FOLDERS = "folders"
    const val STICKERS = "stickers"

    fun chat(id: String) = "chat/$id"
    fun chatSearch(id: String) = "chat/$id/search"
    fun chatGallery(id: String) = "chat/$id/gallery"
    fun call(id: String, type: String) = "call/$id/$type"
    fun contactProfile(chatId: String) = "contact/$chatId"
}

@Composable
fun TeleportApp() {
    val app = LocalContext.current.applicationContext as TeleportApplication
    val vm: TeleportViewModel = viewModel(factory = TeleportViewModel.Factory(app.repository, null))
    val nav = rememberNavController()
    val account by vm.activeAccount.collectAsState()
    val settings by vm.settings().collectAsState(initial = null)

    TeleportTheme(
        themeMode = settings?.themeMode ?: "system",
        colorThemeId = settings?.colorThemeId ?: "slimchat",
        useDynamicColor = settings?.useDynamicColor ?: true,
    ) {
        val appStrings = remember(settings?.localeOverridesJson) {
            mergeAppStrings(settings?.localeOverridesJson)
        }
        CompositionLocalProvider(LocalAppStrings provides appStrings) {
        AppLockGate(
            enabled = settings?.appLockEnabled == true,
            biometricEnabled = settings?.biometricEnabled == true,
            pinHash = settings?.pinHash,
        ) {
            NavHost(
                navController = nav,
                startDestination = if (account != null) Routes.CHATS else Routes.AUTH,
            ) {
                composable(Routes.AUTH) {
                    WelcomeAuthScreen(
                        onPhone = { nav.navigate(Routes.AUTH_PHONE) },
                        onUsername = { nav.navigate(Routes.AUTH_USERNAME) },
                        onRegister = { nav.navigate(Routes.AUTH_REGISTER) },
                    )
                }
                composable(Routes.AUTH_USERNAME) {
                    UsernameLoginScreen(
                        vm,
                        onBack = { nav.popBackStack() },
                        onSuccess = { nav.navigate(Routes.CHATS) { popUpTo(Routes.AUTH) { inclusive = true } } },
                    )
                }
                composable(Routes.AUTH_REGISTER) {
                    RegisterScreen(
                        vm,
                        onBack = { nav.popBackStack() },
                        onSuccess = { nav.navigate(Routes.CHATS) { popUpTo(Routes.AUTH) { inclusive = true } } },
                    )
                }
                composable(Routes.AUTH_PHONE) {
                    PhoneAuthScreen(vm, onBack = { nav.popBackStack() }, onCodeSent = { nav.navigate(Routes.AUTH_USERNAME) })
                }
                composable(Routes.CHATS) {
                    ChatListScreen(
                        vm,
                        onChatClick = { nav.navigate(Routes.chat(it)) },
                        onChats = {},
                        onContacts = { nav.navigate(Routes.CONTACTS) { launchSingleTop = true } },
                        onProfile = { nav.navigate(Routes.PROFILE) { launchSingleTop = true } },
                        onSettings = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        onCalls = { nav.navigate(Routes.CALLS) { launchSingleTop = true } },
                        onArchive = { nav.navigate(Routes.ARCHIVE) },
                        onSearch = { nav.navigate(Routes.SEARCH_USERS) },
                    )
                }
                composable(Routes.CONTACTS) {
                    ContactsScreen(
                        vm,
                        onChats = { nav.navigate(Routes.CHATS) { launchSingleTop = true } },
                        onContacts = {},
                        onProfile = { nav.navigate(Routes.PROFILE) { launchSingleTop = true } },
                        onSettings = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        onCalls = { nav.navigate(Routes.CALLS) { launchSingleTop = true } },
                        onOpenChat = { nav.navigate(Routes.chat(it)) },
                        onSearch = { nav.navigate(Routes.SEARCH_USERS) },
                    )
                }
                composable(Routes.CALLS) {
                    CallsListScreen(
                        vm,
                        onChats = { nav.navigate(Routes.CHATS) { launchSingleTop = true } },
                        onContacts = { nav.navigate(Routes.CONTACTS) { launchSingleTop = true } },
                        onProfile = { nav.navigate(Routes.PROFILE) { launchSingleTop = true } },
                        onSettings = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        onCalls = {},
                        onOpenChat = { nav.navigate(Routes.chat(it)) },
                    )
                }
                composable(Routes.ARCHIVE) {
                    ArchiveScreen(vm, onBack = { nav.popBackStack() }, onChatClick = { nav.navigate(Routes.chat(it)) })
                }
                composable(Routes.CHAT, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { entry ->
                    val chatId = entry.arguments?.getString("chatId") ?: return@composable
                    ChatScreen(vm, chatId,
                        onBack = { nav.popBackStack() },
                        onInfo = { nav.navigate(Routes.contactProfile(chatId)) },
                        onSearch = { nav.navigate(Routes.chatSearch(chatId)) },
                        onGallery = { nav.navigate(Routes.chatGallery(chatId)) },
                        onCall = { type -> nav.navigate(Routes.call(chatId, type)) },
                    )
                }
                composable(Routes.CHAT_SEARCH, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { entry ->
                    ChatSearchScreen(vm, entry.arguments?.getString("chatId") ?: "", onBack = { nav.popBackStack() })
                }
                composable(Routes.CHAT_GALLERY, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { entry ->
                    ChatGalleryScreen(vm, entry.arguments?.getString("chatId") ?: "", onBack = { nav.popBackStack() })
                }
                composable(Routes.CALL, arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType },
                )) { entry ->
                    val chatId = entry.arguments?.getString("chatId") ?: return@composable
                    val type = entry.arguments?.getString("type") ?: "voice"
                    LaunchedEffect(chatId) { vm.startCall(chatId, type) }
                    CallScreen(vm, chatId, type, onEnd = { nav.popBackStack() })
                }
                composable(Routes.SETTINGS) {
                    val chats by vm.chats.collectAsState()
                    SettingsScreen(vm,
                        onChats = { nav.navigate(Routes.CHATS) { launchSingleTop = true } },
                        onContacts = { nav.navigate(Routes.CONTACTS) { launchSingleTop = true } },
                        onProfile = { nav.navigate(Routes.PROFILE) { launchSingleTop = true } },
                        onCalls = { nav.navigate(Routes.CALLS) { launchSingleTop = true } },
                        onFavorites = {
                            chats.find { it.type == com.teleport.messenger.data.entity.ChatType.SAVED }
                                ?.let { nav.navigate(Routes.chat(it.id)) }
                        },
                        onAppearance = { nav.navigate(Routes.APPEARANCE) },
                        onStickers = { nav.navigate(Routes.STICKERS) },
                        onLocalization = { nav.navigate(Routes.LOCALIZATION) },
                        onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                        onFolders = { nav.navigate(Routes.FOLDERS) },
                        onPrivacy = { nav.navigate(Routes.PRIVACY) },
                        onSessions = { nav.navigate(Routes.SESSIONS) },
                        onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
                        onBlocked = { nav.navigate(Routes.BLOCKED) },
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        vm,
                        onChats = { nav.navigate(Routes.CHATS) { launchSingleTop = true } },
                        onContacts = { nav.navigate(Routes.CONTACTS) { launchSingleTop = true } },
                        onSettings = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        onCalls = { nav.navigate(Routes.CALLS) { launchSingleTop = true } },
                        onEdit = { nav.navigate(Routes.EDIT_PROFILE) },
                        onUsername = { nav.navigate(Routes.USERNAME) },
                        onAppearance = { nav.navigate(Routes.APPEARANCE) },
                    )
                }
                composable(Routes.CONTACT_PROFILE, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { entry ->
                    val chatId = entry.arguments?.getString("chatId") ?: return@composable
                    ContactProfileScreen(
                        vm,
                        chatId,
                        onBack = { nav.popBackStack() },
                        onMessage = { nav.popBackStack() },
                        onCall = { type -> nav.navigate(Routes.call(chatId, type)) },
                    )
                }
                composable(Routes.EDIT_PROFILE) { EditProfileScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.USERNAME) { UsernameScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.APPEARANCE) { AppearanceScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.LOCALIZATION) { LocalizationScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.PRIVACY) {
                    PrivacyScreen(vm,
                        onBack = { nav.popBackStack() },
                        onBlocked = { nav.navigate(Routes.BLOCKED) },
                    )
                }
                composable(Routes.BLOCKED) {
                    BlockedUsersScreen(vm, onBack = { nav.popBackStack() })
                }
                composable(Routes.SECURITY) {
                    SecurityScreen(
                        vm,
                        onBack = { nav.popBackStack() },
                        onSessions = { nav.navigate(Routes.SESSIONS) },
                    )
                }
                composable(Routes.SESSIONS) { SessionsScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.NOTIFICATIONS) { NotificationsScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.FOLDERS) { FoldersScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.STICKERS) { StickersScreen(onBack = { nav.popBackStack() }) }
                composable(Routes.HELP) { HelpScreen(onBack = { nav.popBackStack() }) }
                composable(Routes.SEARCH_USERS) {
                    SearchUsersScreen(
                        vm,
                        onBack = { nav.popBackStack() },
                        onOpenChat = { nav.navigate(Routes.chat(it)) },
                    )
                }
                composable(Routes.ACCOUNTS) {
                    AccountsScreen(
                        vm,
                        onBack = { nav.popBackStack() },
                        onAddAccount = { nav.navigate(Routes.AUTH) },
                        onSwitched = {
                            nav.navigate(Routes.CHATS) {
                                popUpTo(Routes.CHATS) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(Routes.PREMIUM) { PremiumScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.STARS) {
                    StarsScreen(vm,
                        onBack = { nav.popBackStack() },
                        onGift = { nav.navigate(Routes.GIFTS) },
                    )
                }
                composable(Routes.GIFTS) {
                    GiftsScreen(vm, onBack = { nav.popBackStack() },
                        onCollection = { nav.navigate(Routes.GIFT_COLLECTION) },
                        onMarketplace = { nav.navigate(Routes.MARKETPLACE) })
                }
                composable(Routes.GIFT_COLLECTION) { GiftCollectionScreen(vm, onBack = { nav.popBackStack() }) }
                composable(Routes.MARKETPLACE) { MarketplaceScreen(vm, onBack = { nav.popBackStack() }) }
            }
        }
        }
    }
}

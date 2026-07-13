package com.teleport.messenger.ui

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.teleport.messenger.ui.strings.LocalAppStrings
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
    const val ISKRY = "iskry"
    const val GIFTS = "gifts"
    const val GIFT_COLLECTION = "gift_collection"
    const val CONTACT_PROFILE = "contact/{chatId}"
    const val PRIVACY = "privacy"
    const val BLOCKED = "blocked"
    const val MARKETPLACE = "marketplace"
    const val FOLDERS = "folders"
    const val STICKERS = "stickers"
    const val HELP = "help"
    const val ADMIN = "admin"

    fun chat(id: String) = "chat/${Uri.encode(id)}"
    fun chatSearch(id: String) = "chat/${Uri.encode(id)}/search"
    fun chatGallery(id: String) = "chat/${Uri.encode(id)}/gallery"
    fun call(id: String, type: String) = "call/${Uri.encode(id)}/$type"
    fun contactProfile(chatId: String) = "contact/${Uri.encode(chatId)}"

    fun decodeRouteArg(value: String?): String? = value?.let { Uri.decode(it) }
}

@Composable
fun TeleportApp() {
    val app = LocalContext.current.applicationContext as TeleportApplication
    val vm: TeleportViewModel = viewModel(factory = TeleportViewModel.Factory(app.repository, null))
    val nav = rememberNavController()
    val account by vm.activeAccount.collectAsState()
    val settings by vm.settings().collectAsState(initial = null)
    val error by vm.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    LaunchedEffect(account) {
        if (account == null && nav.currentDestination?.route != Routes.AUTH) {
            nav.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

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
            Box(Modifier.fillMaxSize()) {
            NavHost(
                navController = nav,
                startDestination = if (account != null) Routes.CHATS else Routes.AUTH,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000)),
                enterTransition = { fadeIn(tween(280)) },
                exitTransition = { fadeOut(tween(220)) },
                popEnterTransition = { fadeIn(tween(280)) },
                popExitTransition = { fadeOut(tween(220)) },
            ) {
                composable(Routes.AUTH) {
                    TeleportAuthScreen(
                        vm,
                        onSuccess = {
                            nav.navigate(Routes.CHATS) { popUpTo(Routes.AUTH) { inclusive = true } }
                        },
                    )
                }
                composable(Routes.AUTH_USERNAME) {
                    TeleportAuthScreen(
                        vm,
                        onSuccess = {
                            nav.navigate(Routes.CHATS) { popUpTo(Routes.AUTH) { inclusive = true } }
                        },
                    )
                }
                composable(Routes.AUTH_REGISTER) {
                    TeleportAuthScreen(
                        vm,
                        onSuccess = {
                            nav.navigate(Routes.CHATS) { popUpTo(Routes.AUTH) { inclusive = true } }
                        },
                    )
                }
                composable(Routes.AUTH_PHONE) {
                    TeleportAuthScreen(
                        vm,
                        onSuccess = {
                            nav.navigate(Routes.CHATS) { popUpTo(Routes.AUTH) { inclusive = true } }
                        },
                    )
                }
                composable(Routes.CHATS) {
                    ChatListScreen(
                        vm,
                        onChatClick = { chatId ->
                            nav.navigate(Routes.chat(chatId)) { launchSingleTop = true }
                        },
                        onChats = {},
                        onContacts = { nav.navigate(Routes.CONTACTS) { launchSingleTop = true } },
                        onSettings = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        onArchive = { nav.navigate(Routes.ARCHIVE) },
                        onSearch = { nav.navigate(Routes.SEARCH_USERS) },
                    )
                }
                composable(Routes.CONTACTS) {
                    ContactsScreen(
                        vm,
                        onChats = { nav.navigate(Routes.CHATS) { launchSingleTop = true } },
                        onContacts = {},
                        onSettings = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        onOpenChat = { nav.navigate(Routes.chat(it)) },
                        onSearch = { nav.navigate(Routes.SEARCH_USERS) },
                    )
                }
                composable(Routes.CALLS) {
                    CallsListScreen(
                        vm,
                        onBack = { nav.popBackStack() },
                        onOpenChat = { nav.navigate(Routes.chat(it)) },
                    )
                }
                composable(Routes.ARCHIVE) {
                    ArchiveScreen(vm, onBack = { nav.popBackStack() }, onChatClick = { nav.navigate(Routes.chat(it)) })
                }
                composable(Routes.CHAT, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { entry ->
                    val chatId = Routes.decodeRouteArg(entry.arguments?.getString("chatId")) ?: return@composable
                    ChatScreen(vm, chatId,
                        onBack = { nav.popBackStack() },
                        onInfo = { nav.navigate(Routes.contactProfile(chatId)) },
                        onSearch = { nav.navigate(Routes.chatSearch(chatId)) },
                        onGallery = { nav.navigate(Routes.chatGallery(chatId)) },
                        onCall = { type -> nav.navigate(Routes.call(chatId, type)) },
                    )
                }
                composable(Routes.CHAT_SEARCH, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { entry ->
                    ChatSearchScreen(vm, Routes.decodeRouteArg(entry.arguments?.getString("chatId")) ?: "", onBack = { nav.popBackStack() })
                }
                composable(Routes.CHAT_GALLERY, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { entry ->
                    ChatGalleryScreen(vm, Routes.decodeRouteArg(entry.arguments?.getString("chatId")) ?: "", onBack = { nav.popBackStack() })
                }
                composable(Routes.CALL, arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType },
                )) { entry ->
                    val chatId = Routes.decodeRouteArg(entry.arguments?.getString("chatId")) ?: return@composable
                    val type = entry.arguments?.getString("type") ?: "voice"
                    LaunchedEffect(chatId) { vm.startCall(chatId, type) }
                    CallScreen(vm, chatId, type, onEnd = { nav.popBackStack() })
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(vm,
                        onChats = { nav.navigate(Routes.CHATS) { launchSingleTop = true } },
                        onContacts = { nav.navigate(Routes.CONTACTS) { launchSingleTop = true } },
                        onFavorites = {
                            vm.openSavedChat { chatId ->
                                nav.navigate(Routes.chat(chatId)) { launchSingleTop = true }
                            }
                        },
                        onAppearance = { nav.navigate(Routes.APPEARANCE) },
                        onStickers = { nav.navigate(Routes.STICKERS) },
                        onLocalization = { nav.navigate(Routes.LOCALIZATION) },
                        onEditProfile = { nav.navigate(Routes.PROFILE) },
                        onFolders = { nav.navigate(Routes.FOLDERS) },
                        onPrivacy = { nav.navigate(Routes.PRIVACY) },
                        onSecurity = { nav.navigate(Routes.SECURITY) },
                        onAccounts = { nav.navigate(Routes.ACCOUNTS) },
                        onPremium = { nav.navigate(Routes.PREMIUM) },
                        onStars = { nav.navigate(Routes.ISKRY) },
                        onHelp = { nav.navigate(Routes.HELP) },
                        onLogout = {
                            vm.logout {
                                nav.navigate(Routes.AUTH) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onSessions = { nav.navigate(Routes.SESSIONS) },
                        onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
                        onBlocked = { nav.navigate(Routes.BLOCKED) },
                        onAdmin = { nav.navigate(Routes.ADMIN) },
                    )
                }
                composable(Routes.ADMIN) {
                    AdminPanelScreen(vm, onBack = { nav.popBackStack() })
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        vm,
                        onBack = { nav.popBackStack() },
                        onEdit = { nav.navigate(Routes.EDIT_PROFILE) },
                        onUsername = { nav.navigate(Routes.USERNAME) },
                        onAppearance = { nav.navigate(Routes.APPEARANCE) },
                        onSecurity = { nav.navigate(Routes.SECURITY) },
                        onSessions = { nav.navigate(Routes.SESSIONS) },
                        onAccounts = { nav.navigate(Routes.ACCOUNTS) },
                        onPremium = { nav.navigate(Routes.PREMIUM) },
                        onFavorites = {
                            vm.openSavedChat { chatId ->
                                nav.navigate(Routes.chat(chatId)) { launchSingleTop = true }
                            }
                        },
                        onStickers = { nav.navigate(Routes.STICKERS) },
                        onBlocked = { nav.navigate(Routes.BLOCKED) },
                        onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
                    )
                }
                composable(Routes.CONTACT_PROFILE, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { entry ->
                    val chatId = Routes.decodeRouteArg(entry.arguments?.getString("chatId")) ?: return@composable
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
                composable(Routes.ISKRY) {
                    IskryScreen(vm,
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
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
            }
        }
        }
    }
}

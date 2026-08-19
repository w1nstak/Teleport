package com.teleport.messenger.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppThemeColors(
    val screenBg: Color,
    val cardBg: Color,
    val elevatedSurface: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val divider: Color,
    val inputBg: Color,
    val bubbleIncoming: Color,
    val bubbleOutgoing: Color,
    val accentBlue: Color,
    val accentRed: Color,
    val accentPink: Color,
    val onlineGreen: Color,
    val chevron: Color,
    val dateDividerBg: Color,
    val infoButtonBg: Color,
    val searchBorder: Color,
    val creditGreen: Color,
    val debitOrange: Color,
    val authGradientTop: Color,
    val authGradientMid: Color,
    val authGradientBottom: Color,
    val authInputBg: Color,
    val authTextPrimary: Color,
    val authTextSecondary: Color,
)

val LightAppColors = AppThemeColors(
    screenBg = Color(0xFFF4F5FA),
    cardBg = Color.White,
    elevatedSurface = Color.White,
    textPrimary = Color(0xFF1A1D26),
    textMuted = Color(0xFF9AA0B4),
    divider = Color(0xFFF0F2F7),
    inputBg = Color(0xFFF3F4F8),
    bubbleIncoming = Color.White,
    bubbleOutgoing = Color(0xFF4A69FF),
    accentBlue = Color(0xFF4A69FF),
    accentRed = Color(0xFFFF3B30),
    accentPink = Color(0xFFFF5C9D),
    onlineGreen = Color(0xFF34C759),
    chevron = Color(0xFFC5CAD6),
    dateDividerBg = Color(0xFFEEF0F6),
    infoButtonBg = Color(0xFFF0F2F8),
    searchBorder = Color(0xFFE8EBF2),
    creditGreen = Color(0xFF34C759),
    debitOrange = Color(0xFFFF9500),
    authGradientTop = Color(0xFFDCE8FF),
    authGradientMid = Color(0xFFEDE4FF),
    authGradientBottom = Color(0xFFFAFAFA),
    authInputBg = Color(0xFFF2F2F7),
    authTextPrimary = Color(0xFF000000),
    authTextSecondary = Color(0xFF8E8E93),
)

val DarkAppColors = AppThemeColors(
    screenBg = Color(0xFF0A0A12),
    cardBg = Color(0xFF13131F),
    elevatedSurface = Color(0xFF1A1A28),
    textPrimary = Color(0xFFF0EFFF),
    textMuted = Color(0xFF6B69A0),
    divider = Color(0xFF1C1C2C),
    inputBg = Color(0xFF161623),
    bubbleIncoming = Color(0xFF161623),
    bubbleOutgoing = Color(0xFF3E8EFF),
    accentBlue = Color(0xFF5FA8FF),
    accentRed = Color(0xFFFF7A7A),
    accentPink = Color(0xFFFF7CB8),
    onlineGreen = Color(0xFF4FD9A8),
    chevron = Color(0xFF4A4870),
    dateDividerBg = Color(0xFF161623),
    infoButtonBg = Color(0xFF1A1A28),
    searchBorder = Color(0xFF2A2A45),
    creditGreen = Color(0xFFC6FF3D),
    debitOrange = Color(0xFFFF9F5F),
    authGradientTop = Color(0xFF14141C),
    authGradientMid = Color(0xFF0E0E14),
    authGradientBottom = Color(0xFF0A0A12),
    authInputBg = Color(0xFF161623),
    authTextPrimary = Color(0xFFF0EFFF),
    authTextSecondary = Color(0xFF8280B4),
)

/** SlimChat — тёмный iOS-стиль как в @slim_chat */
val SlimChatAppColors = AppThemeColors(
    screenBg = Color(0xFF000000),
    cardBg = Color(0xFF1C1C1E),
    elevatedSurface = Color(0xFF2C2C2E),
    textPrimary = Color(0xFFFFFFFF),
    textMuted = Color(0xFF8E8E93),
    divider = Color(0xFF38383A),
    inputBg = Color(0xFF2C2C2E),
    bubbleIncoming = Color(0xFF2C2C2E),
    bubbleOutgoing = Color(0xFF0A84FF),
    accentBlue = Color(0xFF0A84FF),
    accentRed = Color(0xFFFF3B30),
    accentPink = Color(0xFFFF375F),
    onlineGreen = Color(0xFF32D74B),
    chevron = Color(0xFF636366),
    dateDividerBg = Color(0xFF2C2C2E),
    infoButtonBg = Color(0xFF3A3A3C),
    searchBorder = Color(0xFF38383A),
    creditGreen = Color(0xFF32D74B),
    debitOrange = Color(0xFFFF9F0A),
    authGradientTop = Color(0xFF000000),
    authGradientMid = Color(0xFF1C1C1E),
    authGradientBottom = Color(0xFF000000),
    authInputBg = Color(0xFF2C2C2E),
    authTextPrimary = Color(0xFFFFFFFF),
    authTextSecondary = Color(0xFF8E8E93),
)

val LocalAppThemeColors = staticCompositionLocalOf { LightAppColors }

data class ChatAccent(
    val primary: Color,
    val secondary: Color,
)

val LocalChatAccent = staticCompositionLocalOf {
    ChatAccent(Color(0xFF5B5BF0), Color(0xFF8B5CF6))
}

object TeleportAppTheme {
    val colors: AppThemeColors
        @Composable get() = LocalAppThemeColors.current
}

fun themeModeLabel(mode: String): String = when (mode) {
    "light" -> "Светлая"
    "dark" -> "Тёмная"
    else -> "Системная"
}

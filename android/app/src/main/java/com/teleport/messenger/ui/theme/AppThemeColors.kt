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
    screenBg = Color(0xFF0D0F14),
    cardBg = Color(0xFF1A1D28),
    elevatedSurface = Color(0xFF222633),
    textPrimary = Color(0xFFE8ECF4),
    textMuted = Color(0xFF7A8194),
    divider = Color(0xFF2A2F3D),
    inputBg = Color(0xFF252A36),
    bubbleIncoming = Color(0xFF252A36),
    bubbleOutgoing = Color(0xFF4A69FF),
    accentBlue = Color(0xFF6B84FF),
    accentRed = Color(0xFFFF453A),
    accentPink = Color(0xFFFF6DAD),
    onlineGreen = Color(0xFF32D74B),
    chevron = Color(0xFF5C6370),
    dateDividerBg = Color(0xFF252A36),
    infoButtonBg = Color(0xFF2A3040),
    searchBorder = Color(0xFF353B4A),
    creditGreen = Color(0xFF32D74B),
    debitOrange = Color(0xFFFF9F0A),
    authGradientTop = Color(0xFF141824),
    authGradientMid = Color(0xFF181C2A),
    authGradientBottom = Color(0xFF0D0F14),
    authInputBg = Color(0xFF252A36),
    authTextPrimary = Color(0xFFE8ECF4),
    authTextSecondary = Color(0xFF8E95A8),
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

object TeleportAppTheme {
    val colors: AppThemeColors
        @Composable get() = LocalAppThemeColors.current
}

fun themeModeLabel(mode: String): String = when (mode) {
    "light" -> "Светлая"
    "dark" -> "Тёмная"
    else -> "Системная"
}

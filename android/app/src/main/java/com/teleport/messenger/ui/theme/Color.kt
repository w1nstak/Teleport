package com.teleport.messenger.ui.theme

import androidx.compose.ui.graphics.Color

val TeleportBlue = Color(0xFF1565FF)
val TeleportBlueDark = Color(0xFF0A3D99)
val TeleportCyan = Color(0xFF4FC3F7)
val TeleportSurfaceDark = Color(0xFF0D1117)
val TeleportSurfaceLight = Color(0xFFF8FAFF)
val PremiumGold = Color(0xFFFFB800)
val StarYellow = Color(0xFFFFD54F)

data class ColorTheme(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
)

val ColorThemes = listOf(
    ColorTheme("slimchat", "SlimChat", Color(0xFF0A84FF), Color(0xFF5E5CE6), Color(0xFFFF375F)),
    ColorTheme("teleport_blue", "Teleport Blue", TeleportBlue, TeleportCyan, Color(0xFF7C4DFF)),
    ColorTheme("ocean", "Ocean", Color(0xFF0077B6), Color(0xFF00B4D8), Color(0xFF90E0EF)),
    ColorTheme("forest", "Forest", Color(0xFF2D6A4F), Color(0xFF40916C), Color(0xFF95D5B2)),
    ColorTheme("sunset", "Sunset", Color(0xFFE85D04), Color(0xFFF48C06), Color(0xFFFFBA08)),
    ColorTheme("violet", "Violet", Color(0xFF7B2CBF), Color(0xFF9D4EDD), Color(0xFFC77DFF)),
    ColorTheme("rose", "Rose", Color(0xFFE63946), Color(0xFFF4845F), Color(0xFFF1A7A7)),
)

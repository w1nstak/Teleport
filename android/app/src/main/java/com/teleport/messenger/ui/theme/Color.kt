package com.teleport.messenger.ui.theme

import androidx.compose.ui.graphics.Brush
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
    val tertiary: Color = secondary,
    val badge: String? = null,
)

/** Themes from messenger_chat_appearance_v4.html */
val ColorThemes = listOf(
    ColorTheme("violet", "Фиолет", Color(0xFF7C6CF5), Color(0xFFFF8A65)),
    ColorTheme("classic", "Классика", Color(0xFF5B5BF0), Color(0xFF8B5CF6)),
    ColorTheme("ocean", "Океан", Color(0xFF3B82F6), Color(0xFF22D3EE)),
    ColorTheme("mint", "Мята", Color(0xFF22C55E), Color(0xFFA3E635)),
    ColorTheme("sunset", "Закат", Color(0xFFF43F5E), Color(0xFFF59E0B), badge = "Новое"),
    ColorTheme("berry", "Ягода", Color(0xFFC026D3), Color(0xFF7C3AED)),
    ColorTheme("gold", "Золото", Color(0xFFF59E0B), Color(0xFFEF4444)),
    ColorTheme("graphite", "Графит", Color(0xFF8891A5), Color(0xFF3E4657)),
    ColorTheme("rose", "Роза", Color(0xFFEC4899), Color(0xFFF472B6)),
    ColorTheme("lime", "Лайм", Color(0xFF84CC16), Color(0xFF22C55E)),
    ColorTheme("indigo", "Индиго", Color(0xFF6366F1), Color(0xFF3B82F6)),
    ColorTheme("coral", "Коралл", Color(0xFFFB7185), Color(0xFFFDBA74)),
    ColorTheme("emerald", "Изумруд", Color(0xFF10B981), Color(0xFF06B6D4)),
    ColorTheme("lavender", "Лаванда", Color(0xFFA78BFA), Color(0xFFC4B5FD)),
    ColorTheme("fire", "Огонь", Color(0xFFEF4444), Color(0xFFF97316)),
    ColorTheme("steel", "Сталь", Color(0xFF64748B), Color(0xFF94A3B8)),
    ColorTheme("midnight", "Полночь", Color(0xFF1E293B), Color(0xFF475569)),
    ColorTheme("peach", "Персик", Color(0xFFFDBA74), Color(0xFFFCA5A5)),
    ColorTheme("cosmos", "Космос", Color(0xFF4C1D95), Color(0xFF7C3AED)),
    ColorTheme("neon", "Неон", Color(0xFF06B6D4), Color(0xFF7C3AED), badge = "Новое"),
    // Legacy ids (keep working for existing installs)
    ColorTheme("slimchat", "SlimChat", Color(0xFF0A84FF), Color(0xFF5E5CE6), Color(0xFFFF375F)),
    ColorTheme("teleport_blue", "Teleport Blue", TeleportBlue, TeleportCyan, Color(0xFF7C4DFF)),
    ColorTheme("forest", "Forest", Color(0xFF2D6A4F), Color(0xFF40916C), Color(0xFF95D5B2)),
    ColorTheme("violet_old", "Violet", Color(0xFF7B2CBF), Color(0xFF9D4EDD), Color(0xFFC77DFF)),
)

data class ChatWallpaper(
    val id: String,
    val name: String,
    val brush: Brush,
)

val ChatWallpapers = listOf(
    ChatWallpaper(
        "dark",
        "Тёмная",
        Brush.linearGradient(listOf(Color(0xFF1C1826), Color(0xFF0B0A0E))),
    ),
    ChatWallpaper(
        "aurora",
        "Аврора",
        Brush.radialGradient(listOf(Color(0xA67C6CF5), Color(0x59FF8A65), Color(0xFF100E15))),
    ),
    ChatWallpaper(
        "sunset_wp",
        "Закат",
        Brush.radialGradient(listOf(Color(0x99FF8A65), Color(0x66F43F5E), Color(0xFF14100F))),
    ),
    ChatWallpaper(
        "stardust",
        "Искристая пыль",
        Brush.linearGradient(listOf(Color(0xFF241F30), Color(0xFF100E15))),
    ),
    ChatWallpaper(
        "emerald_wp",
        "Изумруд",
        Brush.radialGradient(listOf(Color(0x804ADE80), Color(0x5922D3EE), Color(0xFF0D1310))),
    ),
    ChatWallpaper(
        "nebula",
        "Туманность",
        Brush.linearGradient(listOf(Color(0xFF2A2340), Color(0xFF100E15))),
    ),
    ChatWallpaper(
        "duoneon",
        "Дуо-неон",
        Brush.linearGradient(listOf(Color(0x80EC4899), Color(0x733B82F6), Color(0xFF100E15))),
    ),
    ChatWallpaper(
        "diagonal",
        "Диагональ",
        Brush.linearGradient(listOf(Color(0xFF221B2E), Color(0xFF0F0D13))),
    ),
    ChatWallpaper(
        "plasma",
        "Плазма",
        Brush.sweepGradient(listOf(Color(0xFF3B2A63), Color(0xFF100E15), Color(0xFF1C3A52), Color(0xFF100E15), Color(0xFF3B2A63))),
    ),
    ChatWallpaper(
        "horizon",
        "Горизонт",
        Brush.verticalGradient(listOf(Color(0x6BF59E0B), Color(0xFF14100E), Color(0x59EF4444))),
    ),
)

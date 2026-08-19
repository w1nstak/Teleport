package com.teleport.messenger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun TeleportTheme(
    themeMode: String = "system",
    colorThemeId: String = "violet",
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val palette = ColorThemes.find { it.id == colorThemeId } ?: ColorThemes.first()
    val isSlimChat = colorThemeId == "slimchat"
    val context = LocalContext.current

    val baseColors = when {
        isSlimChat -> SlimChatAppColors
        darkTheme -> DarkAppColors
        else -> LightAppColors
    }
    val appColors = baseColors.copy(
        accentBlue = palette.primary,
        bubbleOutgoing = palette.primary,
        accentPink = palette.secondary,
    )
    val effectiveDark = isSlimChat || darkTheme
    // Custom chat themes must win over Material You
    val allowDynamic = useDynamicColor && isSlimChat.not() &&
        colorThemeId in setOf("teleport_blue", "system")

    val colorScheme = when {
        allowDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        effectiveDark -> darkColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            tertiary = palette.tertiary,
            background = appColors.screenBg,
            surface = appColors.cardBg,
            surfaceVariant = appColors.inputBg,
            onPrimary = Color.White,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            onSurfaceVariant = appColors.textMuted,
            outline = appColors.divider,
        )
        else -> lightColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            tertiary = palette.tertiary,
            background = appColors.screenBg,
            surface = appColors.cardBg,
            surfaceVariant = appColors.inputBg,
            onPrimary = Color.White,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            onSurfaceVariant = appColors.textMuted,
            outline = appColors.divider,
        )
    }

    CompositionLocalProvider(
        LocalAppThemeColors provides appColors,
        LocalChatAccent provides ChatAccent(palette.primary, palette.secondary),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TeleportTypography,
            content = content,
        )
    }
}

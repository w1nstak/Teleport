package com.teleport.messenger.ui.theme



import android.os.Build

import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.runtime.CompositionLocalProvider

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext



@Composable

fun TeleportTheme(

    themeMode: String = "system",

    colorThemeId: String = "teleport_blue",

    useDynamicColor: Boolean = true,

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

    val appColors = when {
        isSlimChat -> SlimChatAppColors
        darkTheme -> DarkAppColors
        else -> LightAppColors
    }
    val effectiveDark = isSlimChat || darkTheme



    val colorScheme = when {

        useDynamicColor && !isSlimChat && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {

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



    CompositionLocalProvider(LocalAppThemeColors provides appColors) {

        MaterialTheme(

            colorScheme = colorScheme,

            typography = TeleportTypography,

            content = content,

        )

    }

}


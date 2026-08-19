package com.teleport.messenger.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.screens.settings.SettingsIcons
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr
import com.teleport.messenger.ui.theme.ManropeFontFamily
import com.teleport.messenger.ui.theme.TeleportMotion

enum class MainTab { Chats, Contacts, Settings }

/** From nav-preview.html / settings-preview.html — order: Contacts → Chats → Settings */
private object HomeNavPalette {
    val TextDim = Color(0xFF837E92)
    val TextActive = Color(0xFFFFFFFF)
    val PillBg = Color(0x0AFFFFFF)
    val PillBorder = Color(0x1AFFFFFF)
    val ItemActiveBg = Color(0x24FFFFFF)
    val ItemActiveBorder = Color(0x38FFFFFF)
}

private fun <T> navTween() = TeleportMotion.normal<T>()

@Composable
fun AppFloatingBottomNav(
    selected: MainTab,
    onChats: () -> Unit,
    onContacts: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xF20A0A12)),
                ),
            )
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(999.dp),
                    ambientColor = Color.Black.copy(0.45f),
                    spotColor = Color.Black.copy(0.45f),
                )
                .clip(RoundedCornerShape(999.dp))
                .background(HomeNavPalette.PillBg)
                .border(1.dp, HomeNavPalette.PillBorder, RoundedCornerShape(999.dp))
                .padding(7.dp)
                .animateContentSize(animationSpec = navTween()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Order as in design: Контакты → Чаты → Настройки
            PillNavItem(
                active = selected == MainTab.Contacts,
                icon = SettingsIcons.Contacts,
                label = "Контакты",
                onClick = onContacts,
            )
            PillNavItem(
                active = selected == MainTab.Chats,
                icon = SettingsIcons.Chats,
                label = appStr(AppStringKey.NAV_CHATS),
                onClick = onChats,
            )
            PillNavItem(
                active = selected == MainTab.Settings,
                icon = Icons.Outlined.Settings,
                label = appStr(AppStringKey.NAV_SETTINGS),
                onClick = onSettings,
            )
        }
    }
}

@Composable
private fun PillNavItem(
    active: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (active) 1.05f else 1f,
        animationSpec = navTween(),
        label = "navIconScale",
    )
    val padStart by animateDpAsState(
        targetValue = if (active) 13.dp else 15.dp,
        animationSpec = navTween(),
        label = "navPadStart",
    )
    val padEnd by animateDpAsState(
        targetValue = if (active) 18.dp else 15.dp,
        animationSpec = navTween(),
        label = "navPadEnd",
    )
    val iconColor by animateColorAsState(
        targetValue = if (active) HomeNavPalette.TextActive else HomeNavPalette.TextDim,
        animationSpec = navTween(),
        label = "navIconColor",
    )
    val bgColor by animateColorAsState(
        targetValue = if (active) HomeNavPalette.ItemActiveBg else Color.Transparent,
        animationSpec = navTween(),
        label = "navBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) HomeNavPalette.ItemActiveBorder else Color.Transparent,
        animationSpec = navTween(),
        label = "navBorder",
    )

    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(start = padStart, end = padEnd, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier
                .size(19.dp)
                .scale(scale),
        )
        AnimatedVisibility(
            visible = active,
            enter = fadeIn(navTween()) + expandHorizontally(
                animationSpec = navTween(),
                expandFrom = Alignment.Start,
                clip = true,
            ),
            exit = fadeOut(TeleportMotion.fast()) + shrinkHorizontally(
                animationSpec = navTween(),
                shrinkTowards = Alignment.Start,
                clip = true,
            ),
        ) {
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = ManropeFontFamily,
                color = HomeNavPalette.TextActive,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

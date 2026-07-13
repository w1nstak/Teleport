package com.teleport.messenger.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr

enum class MainTab { Chats, Contacts, Settings }

private object HomeNavPalette {
    val TextDim = Color(0xFF837E92)
    val TextActive = Color(0xFFFFFFFF)
    val PillBg = Color(0x0AFFFFFF)
    val PillBorder = Color(0x1AFFFFFF)
    val ItemActiveBg = Color(0x24FFFFFF)
    val ItemActiveBorder = Color(0x38FFFFFF)
}

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
                    listOf(Color.Transparent, Color(0xE60E0D12)),
                ),
            )
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(HomeNavPalette.PillBg)
                .border(1.dp, HomeNavPalette.PillBorder, RoundedCornerShape(999.dp))
                .padding(7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PillNavItem(
                active = selected == MainTab.Contacts,
                filled = Icons.Rounded.Groups,
                outline = Icons.Outlined.Groups,
                label = "Контакты",
                onClick = onContacts,
            )
            PillNavItem(
                active = selected == MainTab.Chats,
                filled = Icons.Rounded.ChatBubbleOutline,
                outline = Icons.Outlined.ChatBubbleOutline,
                label = appStr(AppStringKey.NAV_CHATS),
                onClick = onChats,
            )
            PillNavItem(
                active = selected == MainTab.Settings,
                filled = Icons.Rounded.Settings,
                outline = Icons.Outlined.Settings,
                label = appStr(AppStringKey.NAV_SETTINGS),
                onClick = onSettings,
            )
        }
    }
}

@Composable
private fun PillNavItem(
    active: Boolean,
    filled: ImageVector,
    outline: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (active) 1.05f else 1f,
        animationSpec = tween(300),
        label = "navIconScale",
    )
    val color = if (active) HomeNavPalette.TextActive else HomeNavPalette.TextDim

    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .then(
                if (active) {
                    Modifier
                        .background(HomeNavPalette.ItemActiveBg)
                        .border(1.dp, HomeNavPalette.ItemActiveBorder, RoundedCornerShape(999.dp))
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(
                start = if (active) 13.dp else 15.dp,
                end = if (active) 18.dp else 15.dp,
                top = 10.dp,
                bottom = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = if (active) filled else outline,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(19.dp).scale(scale),
        )
        AnimatedVisibility(
            visible = active,
            enter = fadeIn(tween(250)) + expandHorizontally(tween(300), expandFrom = Alignment.Start),
            exit = fadeOut(tween(200)) + shrinkHorizontally(tween(250), shrinkTowards = Alignment.Start),
        ) {
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = HomeNavPalette.TextActive,
                maxLines = 1,
            )
        }
    }
}

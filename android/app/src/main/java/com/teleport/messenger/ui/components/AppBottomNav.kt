package com.teleport.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.strings.AppStringKey
import com.teleport.messenger.ui.strings.appStr
import com.teleport.messenger.ui.theme.TeleportAppTheme

enum class MainTab { Chats, Contacts, Profile, Settings, Calls }

@Composable
fun AppFloatingBottomNav(
    selected: MainTab,
    onChats: () -> Unit,
    onContacts: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onCalls: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TeleportAppTheme.colors
    Column(modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.divider.copy(alpha = 0.5f), thickness = 0.5.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.screenBg)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SlimTab(MainTab.Chats, selected, Icons.Rounded.ChatBubbleOutline, Icons.Outlined.ChatBubbleOutline, appStr(AppStringKey.NAV_CHATS), onChats)
            SlimTab(MainTab.Contacts, selected, Icons.Rounded.People, Icons.Outlined.PeopleOutline, "Контакты", onContacts)
            SlimTab(MainTab.Profile, selected, Icons.Rounded.Person, Icons.Outlined.PersonOutline, "Вы", onProfile)
            SlimTab(MainTab.Settings, selected, Icons.Rounded.Settings, Icons.Outlined.Settings, appStr(AppStringKey.NAV_SETTINGS), onSettings)
            SlimTab(MainTab.Calls, selected, Icons.Rounded.Call, Icons.Outlined.Call, "Звонки", onCalls)
        }
    }
}

@Composable
private fun RowScope.SlimTab(
    tab: MainTab,
    selected: MainTab,
    filled: ImageVector,
    outline: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = TeleportAppTheme.colors
    val active = tab == selected
    val tint = if (active) colors.textPrimary else colors.textMuted
    Column(
        Modifier
            .weight(1f)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Icon(
                if (active) filled else outline,
                label,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = tint,
            maxLines = 1,
        )
    }
}

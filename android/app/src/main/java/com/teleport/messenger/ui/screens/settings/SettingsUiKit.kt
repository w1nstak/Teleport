package com.teleport.messenger.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.components.TeleportAvatar
import com.teleport.messenger.ui.theme.TeleportAppTheme

internal val SettingsCardShape = RoundedCornerShape(12.dp)

@Composable
internal fun SettingsProfileHeader(
    name: String,
    isPremium: Boolean,
    onEdit: () -> Unit,
    onQr: () -> Unit,
) {
    val colors = TeleportAppTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
    ) {
        IconButton(
            onClick = onQr,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp),
        ) {
            Surface(shape = CircleShape, color = colors.elevatedSurface) {
                Icon(
                    Icons.Default.QrCode2,
                    "QR",
                    modifier = Modifier.padding(10.dp).size(22.dp),
                    tint = colors.accentBlue,
                )
            }
        }
        TextButton(
            onClick = onEdit,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp),
        ) {
            Surface(shape = RoundedCornerShape(10.dp), color = colors.elevatedSurface) {
                Text(
                    "Изм.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Medium,
                    color = colors.accentBlue,
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TeleportAvatar(name, isPremium, modifier = Modifier.size(88.dp), size = 88.dp)
            Spacer(Modifier.height(12.dp))
            Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        }
    }
}

@Composable
internal fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = TeleportAppTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SettingsCardShape,
        color = colors.cardBg,
        shadowElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
internal fun SettingsToggleRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true,
) {
    val colors = TeleportAppTheme.colors
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsIconBox(icon, iconBg)
            Spacer(Modifier.width(14.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp, color = colors.textPrimary)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.accentBlue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.elevatedSurface,
                ),
            )
        }
        if (showDivider) {
            HorizontalDivider(Modifier.padding(start = 66.dp), color = colors.divider, thickness = 0.5.dp)
        }
    }
}

@Composable
private fun SettingsIconBox(icon: ImageVector, iconBg: Color) {
    Box(
        Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(iconBg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
internal fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    onClick: () -> Unit,
    value: String? = null,
    valueColor: Color? = null,
    showDivider: Boolean = true,
) {
    val colors = TeleportAppTheme.colors
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsIconBox(icon, iconBg)
            Spacer(Modifier.width(14.dp))
            Text(
                title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = colors.textPrimary,
            )
            if (value != null) {
                Text(value, color = valueColor ?: colors.textMuted, fontSize = 15.sp)
                Spacer(Modifier.width(6.dp))
            }
            Icon(Icons.Default.ChevronRight, null, tint = colors.chevron, modifier = Modifier.size(22.dp))
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                color = colors.divider,
                thickness = 0.5.dp,
            )
        }
    }
}

@Composable
internal fun SettingsSoftBackButton(onBack: () -> Unit) {
    val colors = TeleportAppTheme.colors
    IconButton(onClick = onBack) {
        Surface(shape = CircleShape, color = colors.elevatedSurface, shadowElevation = 6.dp) {
            Icon(
                Icons.Default.ArrowBack,
                "Назад",
                modifier = Modifier.padding(10.dp).size(22.dp),
                tint = colors.textPrimary,
            )
        }
    }
}

@Composable
internal fun SettingsScreenScaffold(
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = TeleportAppTheme.colors.screenBg,
        bottomBar = bottomBar,
        content = content,
    )
}

package com.teleport.messenger.ui.screens.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.theme.TeleportAppTheme
import kotlin.math.absoluteValue

internal val ProfileHeroHeight = 300.dp
internal val ProfileCardTopRadius = 28.dp
internal val ProfileActionRed = Color(0xFFFF3B30)

@Composable
internal fun profileAvatarGradient(name: String): Brush {
    val palette = listOf(0xFF3D5A80, 0xFF5C4D7D, 0xFF2F4858, 0xFF4A6670, 0xFF6B5B95, 0xFF355070)
    val base = Color(palette[name.hashCode().absoluteValue % palette.size].toInt())
    return Brush.verticalGradient(listOf(base, base.copy(alpha = 0.82f)))
}

@Composable
internal fun ProfileQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun ProfileInfoSection(title: String, content: @Composable () -> Unit) {
    val colors = TeleportAppTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Text(title, fontSize = 13.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
internal fun ProfileInfoValue(
    text: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = TeleportAppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            color = colors.accentBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
        trailing?.invoke()
    }
}

@Composable
internal fun ProfileTextAction(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        color = color,
        fontSize = 16.sp,
    )
}

@Composable
internal fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 20.dp),
        color = TeleportAppTheme.colors.divider,
        thickness = 0.5.dp,
    )
}

@Composable
internal fun ProfileHeroOverlay() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(ProfileHeroHeight)
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.45f to Color.Transparent,
                    0.75f to Color.Black.copy(0.35f),
                    1f to Color.Black.copy(0.72f),
                ),
            ),
    )
}

@Composable
internal fun ProfileHeroImage(
    name: String,
    avatarUri: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(ProfileHeroHeight)
            .background(profileAvatarGradient(name)),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUri.isNullOrBlank()) {
            coil.compose.AsyncImage(
                model = avatarUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                name.firstOrNull()?.uppercase() ?: "?",
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.35f),
            )
        }
        ProfileHeroOverlay()
    }
}

@Composable
internal fun SlimSectionHeader(title: String) {
    Text(
        title.uppercase(),
        fontSize = 13.sp,
        color = TeleportAppTheme.colors.textMuted,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

@Composable
internal fun SlimProfileInfoRow(
    icon: ImageVector,
    iconBg: Color,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = TeleportAppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(7.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, color = colors.textMuted)
            Text(value, fontSize = 16.sp, color = colors.textPrimary)
        }
        trailing?.invoke()
    }
}

@Composable
internal fun ProfileShareChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.45f),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Поделиться", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
internal fun ProfileTopActions(
    onBack: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    editLabel: String = "Изм.",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Surface(shape = CircleShape, color = Color.Black.copy(0.25f)) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "Назад",
                        modifier = Modifier.padding(8.dp).size(22.dp),
                        tint = Color.White,
                    )
                }
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
        if (onEdit != null) {
            TextButton(onClick = onEdit) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.Black.copy(0.25f)) {
                    Text(
                        editLabel,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}

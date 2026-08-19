package com.teleport.messenger.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.theme.JetBrainsMonoFontFamily
import com.teleport.messenger.ui.theme.ManropeFontFamily
import java.util.Locale
import kotlin.math.min

object SettingsV6Palette {
    val Bg = Color(0xFF0A0A12)
    val CardBg = Color(0xFF13131F)
    val CardBorder = Color(0xFF22223A)
    val Divider = Color(0xFF1C1C2C)
    val TextPrimary = Color(0xFFF0EFFF)
    val TextMuted = Color(0xFF6B69A0)
    val GroupLabel = Color(0xFF5C5A88)
    val Chevron = Color(0xFF4A4870)
    val SparkGreen = Color(0xFFC6FF3D)
    val SparkGlow = Color(0xFF8FCC1C)
    val BalanceCardStart = Color(0xFF221936)
    val BalanceCardEnd = Color(0xFF14141C)
    val BalanceBorder = Color(0xFF2C2A45)
    val LogoutBorder = Color(0xFF2A1F2A)
    val LogoutText = Color(0xFFFF7A7A)
    val Version = Color(0xFF3F3D5E)
    val ToggleOff = Color(0xFF232338)
    val ToggleOffBorder = Color(0xFF33324F)
    val ToggleOnStart = Color(0xFF5FA8FF)
    val ToggleOnEnd = Color(0xFF2E5FE0)
}

data class SettingsV6Row(
    val icon: ImageVector,
    val gradStart: Color,
    val gradEnd: Color,
    val title: String,
    val value: String? = null,
    val toggle: Boolean? = null,
    val onToggle: ((Boolean) -> Unit)? = null,
    val onClick: (() -> Unit)? = null,
)

@Composable
fun SparkIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFF0B0B10)) {
    Canvas(modifier) {
        val path = Path().apply {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val s = min(size.width, size.height) / 24f
            moveTo(cx, cy - 10f * s)
            cubicTo(cx, cy - 4f * s, cx + 4f * s, cy, cx + 10f * s, cy)
            cubicTo(cx + 4f * s, cy, cx, cy + 4f * s, cx, cy + 10f * s)
            cubicTo(cx, cy + 4f * s, cx - 4f * s, cy, cx - 10f * s, cy)
            cubicTo(cx - 4f * s, cy, cx, cy - 4f * s, cx, cy - 10f * s)
            close()
        }
        drawPath(path, tint)
    }
}

@Composable
fun SettingsV6Title(title: String) {
    Text(
        title,
        fontSize = 23.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = ManropeFontFamily,
        color = SettingsV6Palette.TextPrimary,
        letterSpacing = (-0.4).sp,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
fun SettingsV6ProfileCard(
    initials: String,
    name: String,
    username: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SettingsV6Palette.CardBg)
            .border(1.dp, SettingsV6Palette.CardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF5FA8FF), Color(0xFF2E5FE0)),
                        start = Offset(0f, 0f),
                        end = Offset(80f, 110f),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                initials,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = ManropeFontFamily,
                fontSize = 16.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = SettingsV6Palette.TextPrimary,
            )
            username?.let {
                Text(
                    "@$it",
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = SettingsV6Palette.TextMuted,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
        Icon(SettingsIcons.Chevron, null, tint = SettingsV6Palette.Chevron, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SettingsV6BalanceCard(balance: Long, onTopUp: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(SettingsV6Palette.BalanceCardStart, SettingsV6Palette.BalanceCardEnd)))
            .border(1.dp, SettingsV6Palette.BalanceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onTopUp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFE4FF9C), SettingsV6Palette.SparkGreen, SettingsV6Palette.SparkGlow),
                        center = Offset(12f, 10f),
                        radius = 28f,
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            SparkIcon(Modifier.size(16.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Баланс", fontSize = 12.sp, color = SettingsV6Palette.TextMuted)
            Text(
                "${formatIskry(balance)} искр",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = ManropeFontFamily,
                color = SettingsV6Palette.TextPrimary,
            )
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SettingsV6Palette.SparkGreen)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                "Пополнить",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = Color(0xFF0B0B10),
            )
        }
    }
}

@Composable
fun SettingsV6GroupLabel(label: String) {
    Text(
        label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = ManropeFontFamily,
        color = SettingsV6Palette.GroupLabel,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 10.dp, bottom = 6.dp),
    )
}

@Composable
fun SettingsV6GroupCard(rows: List<SettingsV6Row>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SettingsV6Palette.CardBg)
            .border(1.dp, SettingsV6Palette.CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp),
    ) {
        rows.forEachIndexed { index, row ->
            SettingsV6RowItem(row)
            if (index < rows.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 54.dp)
                        .height(1.dp)
                        .background(SettingsV6Palette.Divider),
                )
            }
        }
    }
}

@Composable
private fun SettingsV6RowItem(row: SettingsV6Row) {
    val rowClickable = row.onClick != null && row.toggle == null
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (rowClickable) Modifier.clickable(onClick = row.onClick!!) else Modifier)
            .padding(horizontal = 8.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(row.gradStart, row.gradEnd),
                        start = Offset(0f, 0f),
                        end = Offset(70f, 95f),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(row.icon, null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            row.title,
            Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = ManropeFontFamily,
            color = SettingsV6Palette.TextPrimary,
        )
        when {
            row.toggle != null -> {
                SettingsV6Toggle(
                    checked = row.toggle,
                    onCheckedChange = row.onToggle ?: {},
                )
            }
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.value?.let {
                        Text(it, fontSize = 13.sp, color = SettingsV6Palette.TextMuted)
                    }
                    if (row.onClick != null) {
                        Icon(SettingsIcons.Chevron, null, tint = SettingsV6Palette.Chevron, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsV6Toggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 21.dp else 2.dp,
        animationSpec = tween(220, easing = easing),
        label = "toggleThumb",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (checked) 0f else 1f,
        animationSpec = tween(200, easing = easing),
        label = "toggleBorder",
    )
    val onStart by animateColorAsState(
        targetValue = if (checked) SettingsV6Palette.ToggleOnStart else SettingsV6Palette.ToggleOff,
        animationSpec = tween(220, easing = easing),
        label = "toggleBgStart",
    )
    val onEnd by animateColorAsState(
        targetValue = if (checked) SettingsV6Palette.ToggleOnEnd else SettingsV6Palette.ToggleOff,
        animationSpec = tween(220, easing = easing),
        label = "toggleBgEnd",
    )
    Box(
        Modifier
            .width(42.dp)
            .height(25.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(listOf(onStart, onEnd)))
            .border(
                1.dp,
                SettingsV6Palette.ToggleOffBorder.copy(alpha = borderAlpha),
                RoundedCornerShape(13.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier
                .offset(x = thumbOffset)
                .align(Alignment.CenterStart)
                .size(19.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
fun SettingsV6Logout(onClick: () -> Unit) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SettingsV6Palette.CardBg)
                .border(1.dp, SettingsV6Palette.LogoutBorder, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF6F6F), Color(0xFFD93A3A)),
                            start = Offset(0f, 0f),
                            end = Offset(70f, 95f),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(SettingsIcons.Logout, null, tint = Color.White, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Выйти",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = ManropeFontFamily,
                color = SettingsV6Palette.LogoutText,
            )
        }
        Text(
            "Teleport v6.2.1",
            fontSize = 11.sp,
            fontFamily = JetBrainsMonoFontFamily,
            color = SettingsV6Palette.Version,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

fun formatIskry(amount: Long): String =
    "%,d".format(Locale("ru"), amount).replace(',', '\u00A0')

fun iskryLabel(amount: Long): String = "${formatIskry(amount)} искр"

@Composable
fun IskryPriceText(
    amount: Long,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = SettingsV6Palette.SparkGreen,
    iconSize: androidx.compose.ui.unit.Dp = 14.dp,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        SparkIcon(Modifier.size(iconSize), tint = color)
        Text(iskryLabel(amount), fontSize = fontSize, fontWeight = fontWeight, color = color)
    }
}

@Composable
fun SettingsV6StatCard(items: List<Pair<String, String>>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SettingsV6Palette.CardBg)
            .border(1.dp, SettingsV6Palette.CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp),
    ) {
        items.forEachIndexed { index, (title, value) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SettingsV6Palette.TextPrimary,
                )
                Text(
                    value,
                    fontSize = 13.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = SettingsV6Palette.TextMuted,
                )
            }
            if (index < items.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SettingsV6Palette.Divider),
                )
            }
        }
    }
}

fun isOwnerUsername(username: String?): Boolean =
    username?.trim()?.removePrefix("@")?.lowercase() == "w1nst"

fun profileInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        parts.isNotEmpty() -> parts[0].first().uppercase()
        else -> "?"
    }
}

@Composable
fun SettingsV6SubTopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0x0DFFFFFF))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color(0xFFEDEBFF), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SettingsV6Palette.TextPrimary)
    }
}

@Composable
fun SettingsV6Screen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(SettingsV6Palette.Bg),
    ) {
        SettingsV6SubTopBar(title, onBack)
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            content = content,
        )
    }
}

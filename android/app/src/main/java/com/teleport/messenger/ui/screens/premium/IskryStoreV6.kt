package com.teleport.messenger.ui.screens.premium

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.screens.settings.SparkIcon
import com.teleport.messenger.ui.screens.settings.formatIskry
import kotlin.math.min

object IskryV6Palette {
    val Bg = Color(0xFF0A0A0F)
    val TextPrimary = Color(0xFFFAFAFF)
    val TextSecondary = Color(0xFFF5F4FF)
    val TextMuted = Color(0xFF8E8CB8)
    val Label = Color(0xFF54527A)
    val SparkGreen = Color(0xFFC6FF3D)
    val SparkDark = Color(0xFF0B0B10)
    val HeroStart = Color(0xFF221936)
    val HeroMid = Color(0xFF14141C)
    val HeroEnd = Color(0xFF0E0E14)
    val HeroBorder = Color(0xFF262635)
    val RingOuter = Color(0xFF3A2E5C)
    val RingInner = Color(0xFF5A4696)
    val HitBadge = Color(0xFF7C5CFF)
    val FeaturedStart = Color(0xFF251A3E)
    val FeaturedEnd = Color(0xFF1A1230)
    val PackBg = Color(0xFF15151D)
    val PackBorder = Color(0xFF232330)
    val PriceMuted = Color(0xFF7A78A8)
    val Bonus = Color(0xFFB8ACF0)
    val BackBtn = Color(0x0DFFFFFF)
    val GiftBtnBg = Color(0x0FFFFFFF)
    val GiftBtnBorder = Color(0xFF2C2C3A)
    val GiftBtnText = Color(0xFFD6D4F5)
    val WeekBadgeBg = Color(0x1AC6FF3D)
}

data class IskryPack(
    val sparks: Int,
    val bonus: Int? = null,
    val priceRub: Int,
    val featured: Boolean = false,
    val badge: String? = null,
)

val defaultIskryPacks = listOf(
    IskryPack(sparks = 550, bonus = 50, priceRub = 449, featured = true, badge = "ХИТ"),
    IskryPack(sparks = 700, priceRub = 579),
    IskryPack(sparks = 1200, priceRub = 899),
    IskryPack(sparks = 1300, priceRub = 999),
)

@Composable
fun IskryStoreTopBar(onBack: () -> Unit) {
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
                .background(IskryV6Palette.BackBtn)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color(0xFFEDEBFF), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text("Искры", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = IskryV6Palette.TextSecondary)
    }
}

@Composable
fun IskryHeroCard(
    balance: Long,
    weekDelta: Long,
    onBuy: () -> Unit,
    onGift: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(IskryV6Palette.HeroStart, IskryV6Palette.HeroMid, IskryV6Palette.HeroEnd),
                    center = Offset(200f, 70f),
                    radius = 420f,
                ),
            )
            .border(1.dp, IskryV6Palette.HeroBorder, RoundedCornerShape(26.dp))
            .padding(horizontal = 22.dp, vertical = 34.dp),
    ) {
        IskryDecorRings(Modifier.align(Alignment.TopCenter).offset(y = (-95).dp))
        IskryFloatingSparks(Modifier.matchParentSize())
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "ВАШ БАЛАНС",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = IskryV6Palette.TextMuted,
                letterSpacing = 0.8.sp,
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFE4FF9C), IskryV6Palette.SparkGreen, Color(0xFF8FCC1C)),
                                center = Offset(17f, 14f),
                                radius = 40f,
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    SparkIcon(Modifier.size(24.dp), tint = IskryV6Palette.SparkDark)
                }
                Text(
                    formatIskry(balance),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = IskryV6Palette.TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
            }
            if (weekDelta >= 0) {
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(IskryV6Palette.WeekBadgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        "↑ +${formatIskry(weekDelta)} за неделю",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8FCC1C),
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(IskryV6Palette.SparkGreen)
                        .clickable(onClick = onBuy)
                        .padding(horizontal = 22.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Add, null, tint = IskryV6Palette.SparkDark, modifier = Modifier.size(16.dp))
                    Text("Купить", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IskryV6Palette.SparkDark)
                }
                Row(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(IskryV6Palette.GiftBtnBg)
                        .border(1.dp, IskryV6Palette.GiftBtnBorder, RoundedCornerShape(16.dp))
                        .clickable(onClick = onGift)
                        .padding(horizontal = 22.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.CardGiftcard, null, tint = IskryV6Palette.GiftBtnText, modifier = Modifier.size(16.dp))
                    Text("Подарить", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IskryV6Palette.GiftBtnText)
                }
            }
        }
    }
}

@Composable
private fun IskryFloatingSparks(modifier: Modifier = Modifier) {
    val sparks = listOf(
        Triple(0.83f, 0.07f, 15f) to IskryV6Palette.SparkGreen,
        Triple(0.92f, 0.25f, 9f) to IskryV6Palette.SparkGreen,
        Triple(0.08f, 0.65f, 13f) to Color(0xFFC4A9FF),
        Triple(0.16f, 0.77f, 8f) to IskryV6Palette.HitBadge,
        Triple(0.87f, 0.75f, 8f) to IskryV6Palette.SparkGreen,
        Triple(0.04f, 0.39f, 7f) to IskryV6Palette.HitBadge,
    )
    Canvas(modifier) {
        sparks.forEach { (pos, color) ->
            val (x, y, sparkSize) = pos
            val cx = size.width * x
            val cy = size.height * y
            val path = Path().apply {
                val s = sparkSize / 24f
                moveTo(cx, cy - 10f * s)
                cubicTo(cx, cy - 4f * s, cx + 4f * s, cy, cx + 10f * s, cy)
                cubicTo(cx + 4f * s, cy, cx, cy + 4f * s, cx, cy + 10f * s)
                cubicTo(cx, cy + 4f * s, cx - 4f * s, cy, cx - 10f * s, cy)
                cubicTo(cx - 4f * s, cy, cx, cy - 4f * s, cx, cy - 10f * s)
                close()
            }
            drawPath(path, color.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun IskryDecorRings(modifier: Modifier = Modifier) {
    Canvas(modifier.size(260.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        drawCircle(IskryV6Palette.RingOuter, 124f, Offset(cx, cy), style = Stroke(1f))
        drawCircle(IskryV6Palette.RingOuter, 100f, Offset(cx, cy), style = Stroke(1f))
        drawCircle(IskryV6Palette.RingInner, 76f, Offset(cx, cy), style = Stroke(1f))
        drawCircle(IskryV6Palette.RingInner, 52f, Offset(cx, cy), style = Stroke(1f))
    }
}

@Composable
fun IskryPackagesSection(packs: List<IskryPack>, onSelect: (IskryPack) -> Unit) {
    val featured = packs.firstOrNull { it.featured }
    val grid = packs.filter { !it.featured }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        Text(
            "ВЫБЕРИТЕ ПАКЕТ",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = IskryV6Palette.Label,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        featured?.let { pack ->
            IskryFeaturedPack(pack, onClick = { onSelect(pack) })
            Spacer(Modifier.height(10.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            grid.forEach { pack ->
                IskryGridPack(
                    pack,
                    onClick = { onSelect(pack) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun IskryFeaturedPack(pack: IskryPack, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(IskryV6Palette.FeaturedStart, IskryV6Palette.FeaturedEnd)))
            .border(1.dp, IskryV6Palette.HitBadge, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        pack.badge?.let { badge ->
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IskryV6Palette.HitBadge)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x1FC6FF3D)),
                contentAlignment = Alignment.Center,
            ) {
                SparkIcon(Modifier.size(24.dp), tint = IskryV6Palette.SparkGreen)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("${pack.sparks} искр", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IskryV6Palette.TextPrimary)
                pack.bonus?.let {
                    Text("+$it искр в подарок", fontSize = 12.sp, color = IskryV6Palette.Bonus, modifier = Modifier.padding(top = 1.dp))
                }
            }
            Text("${pack.priceRub} ₽", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IskryV6Palette.TextPrimary)
        }
    }
}

@Composable
private fun IskryGridPack(pack: IskryPack, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(IskryV6Palette.PackBg)
            .border(1.dp, IskryV6Palette.PackBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SparkIcon(Modifier.size(18.dp), tint = IskryV6Palette.SparkGreen)
        Text(
            "${pack.sparks}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = IskryV6Palette.TextSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            "${pack.priceRub} ₽",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = IskryV6Palette.PriceMuted,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

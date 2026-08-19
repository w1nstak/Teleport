package com.teleport.messenger.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.theme.ChatWallpaper
import com.teleport.messenger.ui.theme.ChatWallpapers
import com.teleport.messenger.ui.theme.ColorTheme
import com.teleport.messenger.ui.theme.ColorThemes
import com.teleport.messenger.ui.theme.ManropeFontFamily
import com.teleport.messenger.ui.theme.UnboundedFontFamily
import com.teleport.messenger.viewmodel.TeleportViewModel

private object AppearancePalette {
    val Bg = Color(0xFF0E0D12)
    val BgLight = Color(0xFFF5F4F8)
    val Card = Color(0xFF17151C)
    val CardLight = Color(0xFFFFFFFF)
    val Card2 = Color(0xFF1E1B24)
    val Card2Light = Color(0xFFEFEDF5)
    val Text = Color(0xFFF4F2FA)
    val TextLight = Color(0xFF1B1924)
    val TextDim = Color(0xFF9B96A8)
    val TextDimLight = Color(0xFF726C82)
    val Hairline = Color(0xFF2A2732)
    val HairlineLight = Color(0xFFE3E0EA)
}

private val AppearanceThemes = ColorThemes
    .filter { it.id !in setOf("slimchat", "teleport_blue", "forest", "violet_old") }
    .take(20)

@Composable
fun AppearanceScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val settings by vm.settings().collectAsState(initial = null)
    var themeMode by remember(settings) { mutableStateOf(settings?.themeMode ?: "dark") }
    var colorId by remember(settings) { mutableStateOf(settings?.colorThemeId ?: "violet") }
    var wallpaperId by remember(settings) { mutableStateOf(settings?.chatWallpaperId ?: "dark") }
    var largeFont by remember(settings) { mutableStateOf(settings?.largeChatFont ?: false) }
    var bubbleAnim by remember(settings) { mutableStateOf(settings?.bubbleAnimations ?: true) }

    val isDark = themeMode != "light"
    val theme = AppearanceThemes.find { it.id == colorId }
        ?: ColorThemes.find { it.id == colorId }
        ?: AppearanceThemes.first()
    val wallpaper = ChatWallpapers.find { it.id == wallpaperId } ?: ChatWallpapers.first()

    val bg = if (isDark) AppearancePalette.Bg else AppearancePalette.BgLight
    val text = if (isDark) AppearancePalette.Text else AppearancePalette.TextLight
    val textDim = if (isDark) AppearancePalette.TextDim else AppearancePalette.TextDimLight
    val hairline = if (isDark) AppearancePalette.Hairline else AppearancePalette.HairlineLight
    val card = if (isDark) AppearancePalette.Card else AppearancePalette.CardLight
    val card2 = if (isDark) AppearancePalette.Card2 else AppearancePalette.Card2Light
    val themeGrad = Brush.linearGradient(listOf(theme.primary, theme.secondary))

    fun save() {
        settings?.let {
            vm.updateSettings(
                it.copy(
                    themeMode = themeMode,
                    colorThemeId = colorId,
                    useDynamicColor = false,
                    chatWallpaperId = wallpaperId,
                    largeChatFont = largeFont,
                    bubbleAnimations = bubbleAnim,
                ),
            )
        }
        onBack()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        AppearanceHero(
            isDark = isDark,
            theme = theme,
            wallpaper = wallpaper,
            text = text,
            textDim = textDim,
            card2 = card2,
            onBack = onBack,
        )

        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            item {
                SectionHead(
                    title = "Цвет чата",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(themeGrad),
                            )
                            Text(
                                theme.name,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = text,
                            )
                        }
                    },
                    textDim = textDim,
                )
                Spacer(Modifier.height(10.dp))
                AppearanceThemeGrid(
                    themes = AppearanceThemes,
                    selectedId = colorId,
                    bg = bg,
                    text = text,
                    textDim = textDim,
                    onSelect = { colorId = it },
                )
            }

            item {
                SectionHead(
                    title = "Фон чата",
                    trailing = {
                        Text(
                            wallpaper.name,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = textDim,
                        )
                    },
                    textDim = textDim,
                )
                Spacer(Modifier.height(10.dp))
                // No upload / plus button — only presets from the mockup
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    ChatWallpapers.forEach { wp ->
                        WallpaperThumb(
                            wallpaper = wp,
                            selected = wallpaperId == wp.id,
                            frameBg = bg,
                            onClick = { wallpaperId = wp.id },
                        )
                    }
                }
            }

            item {
                SectionHead(title = "Дополнительно", textDim = textDim)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppearanceToggleCard(
                        icon = Icons.Outlined.DarkMode,
                        iconBg = Color(0x297C6CF5),
                        iconTint = Color(0xFFA79BFA),
                        label = "Тёмная тема",
                        sub = "Применяется во всём приложении",
                        checked = isDark,
                        themeGrad = themeGrad,
                        card = card,
                        hairline = hairline,
                        text = text,
                        textDim = textDim,
                        onToggle = { themeMode = if (it) "dark" else "light" },
                    )
                    AppearanceToggleCard(
                        icon = Icons.Outlined.TextFields,
                        iconBg = Color(0x29FF8A65),
                        iconTint = Color(0xFFFFAB8D),
                        label = "Крупный шрифт сообщений",
                        sub = "Увеличить размер текста в чатах",
                        checked = largeFont,
                        themeGrad = themeGrad,
                        card = card,
                        hairline = hairline,
                        text = text,
                        textDim = textDim,
                        onToggle = { largeFont = it },
                    )
                    AppearanceToggleCard(
                        icon = Icons.Outlined.AutoAwesome,
                        iconBg = Color(0x244ADE80),
                        iconTint = Color(0xFF6EE7A8),
                        label = "Анимация пузырей",
                        sub = "Плавное появление новых сообщений",
                        checked = bubbleAnim,
                        themeGrad = themeGrad,
                        card = card,
                        hairline = hairline,
                        text = text,
                        textDim = textDim,
                        onToggle = { bubbleAnim = it },
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(bg)
                .drawBehind {
                    drawLine(hairline, Offset(0f, 0.5f), Offset(size.width, 0.5f), 1.dp.toPx())
                }
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = theme.primary.copy(0.4f), spotColor = theme.primary.copy(0.4f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(themeGrad)
                    .clickable(onClick = ::save),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Сохранить оформление",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.5.sp,
                        color = Color.White,
                    )
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AppearanceHero(
    isDark: Boolean,
    theme: ColorTheme,
    wallpaper: ChatWallpaper,
    text: Color,
    textDim: Color,
    card2: Color,
    onBack: () -> Unit,
) {
    val themeGrad = Brush.linearGradient(listOf(theme.primary, theme.secondary))
    Column(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    if (isDark) {
                        Brush.verticalGradient(listOf(Color(0xFF181420), Color(0xFF100E15), Color(0xFF0B0A0E)))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFFEDEBF4), Color(0xFFF5F4F8)))
                    },
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(theme.primary.copy(if (isDark) 0.48f else 0.30f), Color.Transparent),
                        center = Offset(size.width * 0.12f, 0f),
                        radius = size.width * 0.7f,
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(theme.secondary.copy(if (isDark) 0.34f else 0.22f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.55f,
                    ),
                )
            }
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0x14FFFFFF) else Color(0x0D000000))
                    .border(1.dp, if (isDark) Color(0x24FFFFFF) else Color(0x14000000), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = text, modifier = Modifier.size(18.dp))
            }
            Text(
                "Оформление чата",
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = text,
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(wallpaper.brush)
                .background(if (isDark) Color(0x8C17151C) else Color(0xA6FFFFFF))
                .border(1.dp, if (isDark) Color(0x1AFFFFFF) else Color(0x0F000000), RoundedCornerShape(20.dp))
                .padding(14.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x66000000))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    wallpaper.name,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp,
                    color = Color.White.copy(0.92f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF4B4858), Color(0xFF2A2732)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("М", fontFamily = UnboundedFontFamily, fontSize = 8.5.sp, color = textDim)
                    }
                    Box(
                        Modifier
                            .widthIn(max = 220.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 5.dp))
                            .background(card2)
                            .border(1.dp, if (isDark) Color(0x0FFFFFFF) else Color(0x0D000000), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 5.dp))
                            .padding(horizontal = 11.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "Привет! Как настроение сегодня? 👋",
                            fontFamily = ManropeFontFamily,
                            fontSize = 12.sp,
                            color = text,
                            lineHeight = 16.sp,
                        )
                    }
                }
                PreviewOutBubble("Отлично! Только что сменил тему", themeGrad)
                PreviewOutBubble("Теперь чат выглядит вот так ✨", themeGrad)
            }
        }
    }
}

@Composable
private fun PreviewOutBubble(text: String, grad: Brush) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 48.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            Modifier
                .shadow(8.dp, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 5.dp))
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 5.dp))
                .background(grad)
                .padding(horizontal = 11.dp, vertical = 8.dp),
        ) {
            Text(text, fontFamily = ManropeFontFamily, fontSize = 12.sp, color = Color.White, lineHeight = 16.sp)
            Text(
                "14:12",
                fontFamily = ManropeFontFamily,
                fontSize = 9.sp,
                color = Color.White.copy(0.65f),
                modifier = Modifier.align(Alignment.End).padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun SectionHead(
    title: String,
    textDim: Color,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title.uppercase(),
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            color = textDim,
            letterSpacing = 0.6.sp,
        )
        trailing?.invoke()
    }
}

@Composable
private fun AppearanceThemeGrid(
    themes: List<ColorTheme>,
    selectedId: String,
    bg: Color,
    text: Color,
    textDim: Color,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        themes.chunked(5).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                row.forEach { theme ->
                    val selected = selectedId == theme.id
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSelect(theme.id) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box {
                            theme.badge?.let {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-6).dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFFFF8A65), Color(0xFFF43F5E))))
                                        .padding(horizontal = 6.dp, vertical = 2.5.dp),
                                ) {
                                    Text(
                                        it.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = ManropeFontFamily,
                                    )
                                }
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .then(
                                        if (selected) {
                                            Modifier.border(3.dp, bg, RoundedCornerShape(15.dp))
                                                .border(5.5.dp, theme.primary, RoundedCornerShape(15.dp))
                                        } else Modifier
                                    )
                                    .shadow(8.dp, RoundedCornerShape(15.dp))
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(Brush.linearGradient(listOf(theme.primary, theme.secondary))),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Box(
                                        Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x52000000)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(9.dp))
                                    }
                                }
                            }
                        }
                        Text(
                            theme.name,
                            fontFamily = ManropeFontFamily,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 9.5.sp,
                            color = if (selected) text else textDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                repeat(5 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WallpaperThumb(
    wallpaper: ChatWallpaper,
    selected: Boolean,
    frameBg: Color,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(52.dp)
            .then(
                if (selected) {
                    Modifier
                        .border(2.dp, frameBg, RoundedCornerShape(13.dp))
                        .border(4.dp, Color.White, RoundedCornerShape(13.dp))
                } else {
                    Modifier
                },
            )
            .shadow(6.dp, RoundedCornerShape(13.dp))
            .clip(RoundedCornerShape(13.dp))
            .background(wallpaper.brush)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                Icons.Filled.Check,
                null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun AppearanceToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    sub: String,
    checked: Boolean,
    themeGrad: Brush,
    card: Color,
    hairline: Color,
    text: Color,
    textDim: Color,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(card)
            .border(1.dp, hairline, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, color = text)
            Text(sub, fontFamily = ManropeFontFamily, fontSize = 11.5.sp, color = textDim)
        }
        Box(
            Modifier
                .width(44.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (checked) themeGrad else Brush.linearGradient(listOf(hairline, hairline))),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = if (checked) 21.dp else 3.dp)
                    .size(20.dp)
                    .shadow(2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
    }
}

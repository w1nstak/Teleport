package com.teleport.messenger.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.theme.ManropeFontFamily
import com.teleport.messenger.ui.theme.UnboundedFontFamily

/** Colors from messenger_login.html / messenger_registration_v3.html */
object AuthPalette {
    val Bg = Color(0xFF0A0A12)
    val Card = Color(0xFF17151C)
    val Accent = Color(0xFF5B5BF0)
    val Accent2 = Color(0xFF8B5CF6)
    val Text = Color(0xFFF4F2FA)
    val TextDim = Color(0xFF9B96A8)
    val Placeholder = Color(0xFF5C5768)
    val Hairline = Color(0xFF2A2732)
    val Link = Color(0xFFB8B4FF)
    val FocusRing = Color(0x285B5BF0)
    val FocusBorder = Color(0x995B5BF0)

    val PrimaryGradient = Brush.linearGradient(listOf(Accent, Accent2))
    val HeroOverlay = Brush.verticalGradient(
        listOf(Color(0xFF1A1520), Color(0xFF100E15), Color(0xFF0B0A0E)),
    )
}

enum class AuthTab { Login, Register }

@Composable
fun AuthScreenShell(
    heroTitle: String,
    heroSubtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(AuthPalette.Bg),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = AuthPalette.Accent.copy(alpha = 0.32f),
                radius = 180.dp.toPx(),
                center = Offset(-40.dp.toPx(), -20.dp.toPx()),
            )
            drawCircle(
                color = AuthPalette.Accent2.copy(alpha = 0.24f),
                radius = 180.dp.toPx(),
                center = Offset(size.width + 40.dp.toPx(), size.height + 20.dp.toPx()),
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            AuthHero(title = heroTitle, subtitle = heroSubtitle)
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
                content = content,
            )
        }
    }
}

@Composable
fun AuthHero(title: String, subtitle: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(AuthPalette.HeroOverlay)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x805B5BF0), Color.Transparent),
                        center = Offset(size.width * 0.2f, 0f),
                        radius = size.width * 0.7f,
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x668B5CF6), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.15f),
                        radius = size.width * 0.55f,
                    ),
                )
            }
            .padding(top = 54.dp, bottom = 36.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        AuthSpark(Modifier.align(Alignment.TopStart).offset(x = 8.dp, y = (-34).dp), alpha = 0.5f)
        AuthSpark(Modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = (-16).dp), alpha = 0.35f)
        AuthSpark(Modifier.align(Alignment.TopStart).offset(x = 32.dp, y = 10.dp), alpha = 0.25f)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                letterSpacing = 0.2.sp,
                color = AuthPalette.Text,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(9.dp))
            Text(
                subtitle,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = AuthPalette.TextDim,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.widthIn(max = 250.dp),
            )
        }
    }
}

@Composable
private fun AuthSpark(modifier: Modifier = Modifier, alpha: Float) {
    Canvas(modifier.size(14.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val s = size.minDimension / 24f
        val path = Path().apply {
            moveTo(cx, cy - 10f * s)
            cubicTo(cx, cy - 4f * s, cx + 4f * s, cy, cx + 10f * s, cy)
            cubicTo(cx + 4f * s, cy, cx, cy + 4f * s, cx, cy + 10f * s)
            cubicTo(cx, cy + 4f * s, cx - 4f * s, cy, cx - 10f * s, cy)
            cubicTo(cx - 4f * s, cy, cx, cy - 4f * s, cx, cy - 10f * s)
            close()
        }
        drawPath(path, Color.White.copy(alpha = alpha))
    }
}

@Composable
fun AuthLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (focused) AuthPalette.FocusBorder else AuthPalette.Hairline,
        animationSpec = tween(200),
        label = "authFieldBorder",
    )
    val shape = RoundedCornerShape(16.dp)

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            label,
            fontFamily = ManropeFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AuthPalette.TextDim,
            modifier = Modifier.padding(start = 2.dp),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(shape)
                .background(AuthPalette.Card)
                .border(1.dp, borderColor, shape)
                .then(
                    if (focused) {
                        Modifier.drawBehind {
                            drawRoundRect(
                                color = AuthPalette.FocusRing,
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                size = Size(size.width, size.height),
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(17.dp), contentAlignment = Alignment.Center) {
                    leadingIcon()
                }
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focused = it.isFocused },
                    textStyle = TextStyle(
                        fontFamily = ManropeFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuthPalette.Text,
                    ),
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation,
                    cursorBrush = SolidColor(AuthPalette.Accent),
                    decorationBox = { inner ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    placeholder,
                                    fontFamily = ManropeFontFamily,
                                    fontSize = 14.sp,
                                    color = AuthPalette.Placeholder,
                                )
                            }
                            inner()
                        }
                    },
                )
                trailing?.invoke()
            }
        }
    }
}

@Composable
fun AuthPasswordStrength(password: String) {
    val level = when {
        password.length >= 12 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10 && password.any { it.isDigit() } -> 3
        password.length >= 8 -> 2
        password.length >= 4 -> 1
        else -> 0
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(4) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (i < level) AuthPalette.PrimaryGradient
                        else Brush.linearGradient(listOf(AuthPalette.Hairline, AuthPalette.Hairline)),
                    ),
            )
        }
    }
}

@Composable
fun AuthCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: @Composable RowScope.() -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        content = {
            Box(
                Modifier
                    .padding(top = 1.dp)
                    .size(17.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (checked) {
                            Modifier.background(AuthPalette.PrimaryGradient)
                        } else {
                            Modifier
                                .background(AuthPalette.Card)
                                .border(1.dp, AuthPalette.Hairline, RoundedCornerShape(6.dp))
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (checked) {
                    Canvas(Modifier.size(10.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.15f, size.height * 0.5f)
                            lineTo(size.width * 0.4f, size.height * 0.78f)
                            lineTo(size.width * 0.85f, size.height * 0.22f)
                        }
                        drawPath(
                            path,
                            Color.White,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }
                }
            }
            label()
        },
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = if (enabled) 14.dp else 0.dp,
                shape = shape,
                ambientColor = AuthPalette.Accent.copy(0.4f),
                spotColor = AuthPalette.Accent.copy(0.4f),
            )
            .clip(shape)
            .background(
                if (enabled) AuthPalette.PrimaryGradient
                else Brush.linearGradient(listOf(Color(0xFF2A2732), Color(0xFF2A2732))),
            )
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White,
                )
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
fun AuthFooterLink(
    prefix: String,
    action: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            prefix,
            fontFamily = ManropeFontFamily,
            fontSize = 12.5.sp,
            color = AuthPalette.TextDim,
        )
        Text(
            action,
            fontFamily = ManropeFontFamily,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = AuthPalette.Link,
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@Composable
fun AuthFieldIcon(icon: ImageVector) {
    Icon(icon, contentDescription = null, tint = AuthPalette.TextDim, modifier = Modifier.size(17.dp))
}

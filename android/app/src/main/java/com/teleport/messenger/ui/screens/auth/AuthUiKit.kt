package com.teleport.messenger.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AuthPalette {
    val Bg = Color(0xFF000000)
    val BgGlowBlue = Color(0xFF1D4ED8)
    val BgGlowViolet = Color(0xFF6D28D9)
    val AccentBlue = Color(0xFF3B82F6)
    val AccentViolet = Color(0xFF8B5CF6)
    val AccentCyan = Color(0xFF38BDF8)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF94A3B8)
    val InputBg = Color(0xFF0A0F1A)
    val InputBorder = Color(0xFF243044)
    val TabTrack = Color(0xFF0C1018)
    val TabBorder = Color(0xFF1E293B)
    val IconTint = Color(0xFFCBD5E1)
    val LinkViolet = Color(0xFFA78BFA)

    val PrimaryGradient = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF7C3AED)))
    val GlowGradient = Brush.horizontalGradient(listOf(Color(0xFF3B82F6).copy(0.45f), Color(0xFF7C3AED).copy(0.45f)))
}

enum class AuthTab { Login, Register }

@Composable
fun AuthGradientBackground(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        drawRect(AuthPalette.Bg)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(AuthPalette.BgGlowBlue.copy(0.22f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.08f),
                radius = size.width * 0.65f,
            ),
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(AuthPalette.BgGlowViolet.copy(0.14f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.22f),
                radius = size.width * 0.45f,
            ),
        )
    }
}

@Composable
fun AuthScreenShell(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(AuthPalette.Bg)) {
        AuthGradientBackground(Modifier.fillMaxSize())
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

@Composable
fun AuthWelcomeHeader() {
    Spacer(Modifier.height(20.dp))
    DolphinLeapIcon(Modifier.size(68.dp, 24.dp))
    Spacer(Modifier.height(6.dp))
    Text(
        "TELEPORT",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = AuthPalette.TextPrimary,
        letterSpacing = 4.sp,
    )
    Spacer(Modifier.height(22.dp))
    Text(
        "Добро пожаловать в Teleport",
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = AuthPalette.TextPrimary,
        textAlign = TextAlign.Center,
        lineHeight = 28.sp,
    )
    Spacer(Modifier.height(26.dp))
}

@Composable
fun AuthTabSwitcher(selected: AuthTab, onSelect: (AuthTab) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AuthPalette.TabTrack)
            .border(1.dp, AuthPalette.TabBorder, shape)
            .padding(4.dp),
    ) {
        AuthTab.entries.forEach { tab ->
            val active = tab == selected
            val label = if (tab == AuthTab.Login) "Вход" else "Регистрация"
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(tab) }
                    .then(
                        if (active) {
                            Modifier
                                .shadow(8.dp, RoundedCornerShape(11.dp), ambientColor = AuthPalette.AccentBlue.copy(0.35f))
                                .background(AuthPalette.PrimaryGradient)
                        } else {
                            Modifier
                        },
                    )
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (active) Color.White else AuthPalette.TextMuted,
                )
            }
        }
    }
    Spacer(Modifier.height(18.dp))
}

@Composable
fun AuthPrimaryButton(
    text: String,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(Modifier.fillMaxWidth()) {
        if (enabled) {
            Box(
                Modifier
                    .matchParentSize()
                    .padding(top = 6.dp)
                    .clip(shape)
                    .background(AuthPalette.GlowGradient),
            )
        }
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = enabled && !loading,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color(0xFF1E293B),
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (enabled) AuthPalette.PrimaryGradient
                        else Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B))),
                        shape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AuthSwitchModeLink(isLogin: Boolean, onClick: () -> Unit) {
    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            if (isLogin) "Нет аккаунта? " else "Уже есть аккаунт? ",
            color = AuthPalette.TextMuted,
            fontSize = 14.sp,
        )
        Text(
            if (isLogin) "Зарегистрироваться" else "Войти",
            color = AuthPalette.LinkViolet,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@Composable
fun AuthPrivacyFooter() {
    Spacer(Modifier.height(22.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.Shield,
            contentDescription = null,
            tint = AuthPalette.TextMuted,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp),
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                "Ваши данные защищены сквозным шифрованием.",
                fontSize = 11.sp,
                color = AuthPalette.TextMuted,
                lineHeight = 15.sp,
            )
            Text(
                "Мы не передаём информацию третьим лицам.",
                fontSize = 11.sp,
                color = AuthPalette.TextMuted,
                lineHeight = 15.sp,
            )
        }
    }
}

package com.teleport.messenger.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.teleport.messenger.ui.theme.TeleportBlue
import com.teleport.messenger.ui.theme.TeleportBlueDark
import com.teleport.messenger.ui.theme.TeleportCyan

@Composable
fun AnimatedChatBackground(modifier: Modifier = Modifier, animated: Boolean = true) {
    val infinite = rememberInfiniteTransition(label = "bg")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "phase",
    )

    Canvas(modifier.fillMaxSize()) {
        if (animated) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(TeleportBlueDark, TeleportBlue, TeleportCyan.copy(0.3f)),
                    start = Offset(size.width * phase, 0f),
                    end = Offset(size.width * (1 - phase), size.height),
                ),
            )
        } else {
            drawRect(Color(0xFF0D1117))
        }
    }
}

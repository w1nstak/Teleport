package com.teleport.messenger.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.teleport.messenger.R
import kotlin.math.roundToInt

/** Дельфин в шапке — из макета. */
@Composable
fun DolphinLeapIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_dolphin_header),
        contentDescription = null,
        modifier = modifier,
    )
}

/** Голова дельфина в поле username — из макета, следует за курсором. */
@Composable
fun DolphinFieldPeek(
    text: String,
    cursorOffset: Int,
    textLayoutResult: TextLayoutResult?,
    fieldWidthPx: Float,
    hideForPassword: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible || fieldWidthPx <= 0f) return

    val density = LocalDensity.current
    val headW = with(density) { 44.dp.toPx() }
    val padStart = with(density) { 48.dp.toPx() }
    val padEnd = with(density) { 10.dp.toPx() }
    val approxCharW = with(density) { 8.5.dp.toPx() }

    val anchorRight = (fieldWidthPx - headW - padEnd).coerceAtLeast(padStart)
    val fallbackX = if (text.isEmpty()) anchorRight else padStart + text.length * approxCharW
    val maxX = anchorRight

    val cursorX = remember(text, cursorOffset, textLayoutResult, padStart, maxX, approxCharW) {
        val layoutText = textLayoutResult?.layoutInput?.text?.text
        if (textLayoutResult != null && layoutText == text && text.isNotEmpty()) {
            val safe = cursorOffset.coerceIn(0, text.length)
            runCatching {
                textLayoutResult.getCursorRect(safe).left + padStart
            }.getOrDefault(fallbackX)
        } else {
            fallbackX
        }
    }

    val targetX = cursorX.coerceIn(padStart, maxX)

    val animatedX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = 340f),
        label = "peekX",
    )
    val shy by animateFloatAsState(
        targetValue = if (hideForPassword) 1f else 0f,
        animationSpec = tween(240),
        label = "shy",
    )

    Box(modifier.height(54.dp)) {
        Image(
            painter = painterResource(R.drawable.ic_dolphin_peek),
            contentDescription = null,
            modifier = Modifier
                .offset {
                    IntOffset(
                        animatedX.roundToInt(),
                        with(density) { 10.dp.toPx() }.roundToInt(),
                    )
                }
                .size(44.dp, 16.dp)
                .graphicsLayer {
                    alpha = 1f - shy * 0.92f
                    translationX = shy * 40f
                },
        )
    }
}

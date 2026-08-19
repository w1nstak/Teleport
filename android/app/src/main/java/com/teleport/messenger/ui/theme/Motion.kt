package com.teleport.messenger.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Motion tokens.
 * Stack push/pop matches Telegram: full-width enter, ~30% parallax on the underlying screen.
 */
object TeleportMotion {
    /** Telegram-like ease-out curve */
    val Ease = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
    val EaseInOut = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    const val Fast = 180
    const val Normal = 280
    /** Duration close to Telegram iOS push (~340ms) */
    const val Telegram = 340

    fun <T> fast() = tween<T>(Fast, easing = EaseInOut)
    fun <T> normal() = tween<T>(Normal, easing = EaseInOut)
    fun <T> telegram() = tween<T>(Telegram, easing = Ease)

    fun <T> snappy() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** New screen slides in from the right (full width). */
    fun pushEnter() = slideInHorizontally(
        animationSpec = telegram(),
        initialOffsetX = { fullWidth -> fullWidth },
    )

    /** Previous screen peeks left ~30% (Telegram parallax). */
    fun pushExit() = slideOutHorizontally(
        animationSpec = telegram(),
        targetOffsetX = { fullWidth -> -(fullWidth * 0.3f).toInt() },
    )

    /** Revealing previous under a pop. */
    fun popEnter() = slideInHorizontally(
        animationSpec = telegram(),
        initialOffsetX = { fullWidth -> -(fullWidth * 0.3f).toInt() },
    )

    /** Current screen slides out to the right. */
    fun popExit() = slideOutHorizontally(
        animationSpec = telegram(),
        targetOffsetX = { fullWidth -> fullWidth },
    )

    /** Main tabs: Telegram-style quick crossfade, no heavy slide. */
    fun tabEnter() = fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing))
    fun tabExit() = fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing))
}

@Composable
fun Modifier.pressScaleWith(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.982f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = TeleportMotion.fast(),
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

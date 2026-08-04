package com.musicplayer.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Three bouncing bars that mark the row currently playing.
 * Freezes mid-height when paused so the row still reads as "current".
 */
@Composable
fun NowPlayingBars(
    color: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    val transition = rememberInfiniteTransition(label = "now-playing-bars")

    val heights = listOf(
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
            label = "bar1"
        ),
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(tween(380), RepeatMode.Reverse),
            label = "bar2"
        ),
        transition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween(640), RepeatMode.Reverse),
            label = "bar3"
        )
    )

    Canvas(modifier = modifier.size(size)) {
        val barWidth = this.size.width / 5f
        val gap = barWidth / 2f
        heights.forEachIndexed { index, animated ->
            val fraction = if (isPlaying) animated.value else 0.45f
            val barHeight = this.size.height * fraction
            val left = index * (barWidth + gap)
            drawLine(
                color = color,
                start = Offset(left + barWidth / 2f, this.size.height),
                end = Offset(left + barWidth / 2f, this.size.height - barHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

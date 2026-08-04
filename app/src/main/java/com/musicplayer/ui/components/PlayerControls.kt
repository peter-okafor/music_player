package com.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.RepeatMode
import com.musicplayer.ui.theme.TextMuted
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.util.TimeFormatter

/**
 * Seek bar plus transport row for the full-screen player.
 *
 * While the user drags, the displayed position follows the thumb rather than
 * the player clock, so the label never fights the gesture.
 */
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    currentTimeMs: Long,
    durationMs: Long,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableFloatStateOf(0f) }

    val playedFraction = if (durationMs > 0) {
        (currentTimeMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    val sliderValue = if (isScrubbing) scrubPosition else playedFraction
    val displayedMs = if (isScrubbing) (scrubPosition * durationMs).toLong() else currentTimeMs

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            value = sliderValue,
            onValueChange = {
                isScrubbing = true
                scrubPosition = it
            },
            onValueChangeFinished = {
                onSeek((scrubPosition * durationMs).toLong())
                isScrubbing = false
            },
            enabled = durationMs > 0,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = TimeFormatter.formatDuration(displayedMs),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = TimeFormatter.formatDuration(durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleEnabled) accentColor else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(40.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(72.dp)) {
                    Icon(
                        imageVector = if (isPlaying) {
                            Icons.Rounded.Pause
                        } else {
                            Icons.Rounded.PlayArrow
                        },
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(40.dp)
                )
            }

            IconButton(onClick = onCycleRepeat, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (repeatMode == RepeatMode.ONE) {
                        Icons.Rounded.RepeatOne
                    } else {
                        Icons.Rounded.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.OFF) accentColor else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

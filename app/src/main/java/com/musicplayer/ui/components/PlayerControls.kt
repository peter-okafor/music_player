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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.RepeatMode
import com.musicplayer.ui.theme.Primary
import com.musicplayer.ui.theme.SurfaceLight
import com.musicplayer.ui.theme.TextMuted
import com.musicplayer.ui.theme.TextPrimary
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.util.TimeFormatter

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
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember(currentTimeMs) {
        mutableFloatStateOf(if (durationMs > 0) currentTimeMs.toFloat() / durationMs else 0f)
    }
    var isDragging by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress slider
        Slider(
            value = if (isDragging > 0f) isDragging else sliderPosition,
            onValueChange = { isDragging = it },
            onValueChangeFinished = {
                onSeek((isDragging * durationMs).toLong())
                sliderPosition = isDragging
                isDragging = 0f
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = TextPrimary,
                activeTrackColor = TextPrimary,
                inactiveTrackColor = SurfaceLight
            )
        )

        // Time display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = TimeFormatter.formatDuration(currentTimeMs),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = TimeFormatter.formatDuration(durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(
                onClick = onToggleShuffle,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleEnabled) Primary else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Previous
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play/Pause
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(TextPrimary),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Next
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat
            IconButton(
                onClick = onCycleRepeat,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.OFF) Primary else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

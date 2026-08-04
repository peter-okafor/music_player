package com.musicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicplayer.player.SleepTimerState
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.util.TimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    state: SleepTimerState,
    defaultMinutes: Int,
    onStart: (minutes: Int, finishTrack: Boolean) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var minutes by remember { mutableFloatStateOf(defaultMinutes.toFloat()) }
    var finishTrack by remember { mutableStateOf(state.finishTrack) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Sleep timer",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (state.isActive) {
                Text(
                    text = "Pausing in ${TimeFormatter.formatCountdown(state.remainingMs)}" +
                        if (state.finishTrack) " (after this track)" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                Text(
                    text = "Playback pauses automatically when the timer ends.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PRESET_MINUTES) { preset ->
                    FilterChip(
                        selected = minutes.roundToInt() == preset,
                        onClick = { minutes = preset.toFloat() },
                        label = { Text(if (preset >= 60) "${preset / 60} hr" else "$preset min") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${minutes.roundToInt()} minutes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = minutes,
                onValueChange = { minutes = it },
                valueRange = 5f..180f,
                steps = 34,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Finish current track",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Wait for the song to end before pausing",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(checked = finishTrack, onCheckedChange = { finishTrack = it })
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isActive) {
                    OutlinedButton(
                        onClick = {
                            onCancel()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel timer")
                    }
                }
                Button(
                    onClick = {
                        onStart(minutes.roundToInt(), finishTrack)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isActive) "Restart" else "Start")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedSheet(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var speed by remember { mutableFloatStateOf(currentSpeed) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Playback speed",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "%.2fx".format(speed),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Slider(
                value = speed,
                onValueChange = { speed = it },
                onValueChangeFinished = { onSpeedChange(speed) },
                valueRange = 0.5f..2f,
                steps = 29,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PRESET_SPEEDS) { preset ->
                    FilterChip(
                        selected = kotlin.math.abs(speed - preset) < 0.01f,
                        onClick = {
                            speed = preset
                            onSpeedChange(preset)
                        },
                        label = { Text("%.2fx".format(preset)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private val PRESET_MINUTES = listOf(10, 15, 30, 45, 60, 90, 120)
private val PRESET_SPEEDS = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

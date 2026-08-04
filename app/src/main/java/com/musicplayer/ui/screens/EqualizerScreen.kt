package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.prefs.UserPreferences
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.ui.viewmodel.EqualizerViewModel

@Composable
fun EqualizerScreen(
    onNavigateBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(
            title = "Equalizer",
            subtitle = if (state.available) "Applied to all playback" else null,
            onNavigateBack = onNavigateBack,
            actions = {
                if (state.available) {
                    TextButton(onClick = viewModel::reset) { Text("Reset") }
                }
            }
        )

        if (!state.available) {
            EmptyState(
                title = "Equalizer unavailable",
                subtitle = "This device doesn't expose audio effects to apps, " +
                    "or playback hasn't started yet. Play a song and come back.",
                icon = Icons.Rounded.Equalizer
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp
            )
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable equalizer",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Turn off to bypass all effects",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = viewModel::setEnabled
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (state.presets.isNotEmpty()) {
                item {
                    Text(
                        text = "Presets",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.presets.withIndex().toList()) { (index, name) ->
                            FilterChip(
                                selected = state.currentPreset == index,
                                onClick = { viewModel.applyPreset(index) },
                                enabled = state.enabled,
                                label = { Text(name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Text(
                    text = "Bands",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(state.bands, key = { it.index }) { band ->
                BandSlider(
                    label = formatFrequency(band.centerFrequencyHz),
                    value = band.levelMillibel.toFloat(),
                    valueLabel = formatDecibel(band.levelMillibel.toInt()),
                    range = state.minLevel.toFloat()..state.maxLevel.toFloat(),
                    enabled = state.enabled,
                    onValueChange = { viewModel.setBandLevel(band.index, it.toInt().toShort()) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Effects",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))

                BandSlider(
                    label = "Bass boost",
                    value = state.bassBoost.toFloat(),
                    valueLabel = "${state.bassBoost / 10}%",
                    range = 0f..1000f,
                    enabled = state.enabled,
                    onValueChange = { viewModel.setBassBoost(it.toInt()) }
                )
                BandSlider(
                    label = "3D effect",
                    value = state.virtualizer.toFloat(),
                    valueLabel = "${state.virtualizer / 10}%",
                    range = 0f..1000f,
                    enabled = state.enabled,
                    onValueChange = { viewModel.setVirtualizer(it.toInt()) }
                )
                BandSlider(
                    label = "Loudness",
                    value = state.loudnessGain.toFloat(),
                    valueLabel = "+${state.loudnessGain / 100} dB",
                    range = 0f..2000f,
                    enabled = state.enabled,
                    onValueChange = { viewModel.setLoudness(it.toInt()) }
                )

                if (state.currentPreset == UserPreferences.PRESET_CUSTOM) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Custom preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BandSlider(
    label: String,
    value: Float,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(64.dp)
            )
            Slider(
                value = value.coerceIn(range.start, range.endInclusive),
                onValueChange = onValueChange,
                valueRange = range,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(56.dp)
            )
        }
    }
}

private fun formatFrequency(hz: Int): String =
    if (hz >= 1000) "${hz / 1000} kHz" else "$hz Hz"

private fun formatDecibel(millibel: Int): String {
    val db = millibel / 100
    return if (db > 0) "+$db dB" else "$db dB"
}

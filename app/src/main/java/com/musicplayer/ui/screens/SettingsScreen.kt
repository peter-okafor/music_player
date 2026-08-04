package com.musicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.prefs.ThemeMode
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    onMessage: (String) -> Unit = {}
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val isRescanning by viewModel.isRescanning.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            onMessage(it)
            viewModel.consumeMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(title = "Settings", onNavigateBack = onNavigateBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp
            )
        ) {
            item {
                SettingsSection(title = "Appearance")

                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> "System"
                                        ThemeMode.DARK -> "Dark"
                                        ThemeMode.LIGHT -> "Light"
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsToggle(
                    title = "Colour from album art",
                    subtitle = "Tint the player with the artwork's dominant colour",
                    checked = dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                SettingsSection(title = "Audio")

                SettingsAction(
                    title = "Equalizer",
                    subtitle = "Bands, presets, bass boost and loudness",
                    icon = Icons.Rounded.Equalizer,
                    onClick = onOpenEqualizer
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                SettingsSection(title = "Library")

                SettingsAction(
                    title = "Rescan library",
                    subtitle = "Pick up files added since the app was opened",
                    icon = Icons.Rounded.Refresh,
                    onClick = viewModel::rescanLibrary,
                    trailing = {
                        if (isRescanning) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsAction(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        trailing?.invoke()
    }
}

package com.musicplayer.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.ui.components.Artwork
import com.musicplayer.ui.components.PlaybackSpeedSheet
import com.musicplayer.ui.components.PlayerControls
import com.musicplayer.ui.components.SleepTimerSheet
import com.musicplayer.ui.theme.LocalAccentColor
import com.musicplayer.ui.theme.Radius
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.ui.viewmodel.PlayerViewModel
import com.musicplayer.util.TimeFormatter

/**
 * Full-screen now playing view.
 *
 * The background picks up the artwork's accent colour and the whole sheet can
 * be swiped down to dismiss, matching the gesture people expect from a
 * bottom-sheet style player.
 */
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val queueState by viewModel.queueState.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val sleepTimer by viewModel.sleepTimerState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()

    var showSleepSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    val dragAccumulator = remember { mutableFloatStateOf(0f) }

    val accent = LocalAccentColor.current
    val animatedAccent by animateColorAsState(targetValue = accent, label = "player-accent")
    val isFavorite = currentTrack?.id?.let { favorites.contains(it) } == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        animatedAccent.copy(alpha = 0.42f),
                        animatedAccent.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragAccumulator.floatValue > DISMISS_THRESHOLD_PX) onNavigateBack()
                        dragAccumulator.floatValue = 0f
                    },
                    onDragCancel = { dragAccumulator.floatValue = 0f }
                ) { _, dragAmount ->
                    dragAccumulator.floatValue += dragAmount
                }
            }
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Close player",
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PLAYING FROM",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = currentTrack?.album ?: "Unknown album",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onOpenQueue,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Rounded.QueueMusic,
                    contentDescription = "Queue"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Artwork(
            uri = currentTrack?.artworkUri,
            shape = Radius.artworkLarge,
            placeholderIconSize = 96.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Title + favourite
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTrack?.title ?: "Nothing playing",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentTrack?.artist ?: "—",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = viewModel::toggleFavorite,
                enabled = currentTrack != null
            ) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Rounded.Favorite
                    } else {
                        Icons.Rounded.FavoriteBorder
                    },
                    contentDescription = "Favourite",
                    tint = if (isFavorite) animatedAccent else TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PlayerControls(
            isPlaying = playbackState.isPlaying,
            currentTimeMs = playbackState.currentTimeMs,
            durationMs = playbackState.durationMs,
            shuffleEnabled = queueState.shuffleEnabled,
            repeatMode = queueState.repeatMode,
            onTogglePlayPause = viewModel::togglePlayPause,
            onNext = viewModel::next,
            onPrevious = viewModel::previous,
            onSeek = viewModel::seekTo,
            onToggleShuffle = viewModel::toggleShuffle,
            onCycleRepeat = viewModel::cycleRepeatMode,
            accentColor = animatedAccent
        )

        Spacer(modifier = Modifier.weight(1f))

        // Secondary actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            AssistChip(
                onClick = { showSleepSheet = true },
                label = {
                    Text(
                        if (sleepTimer.isActive) {
                            TimeFormatter.formatCountdown(sleepTimer.remainingMs)
                        } else {
                            "Sleep"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = if (sleepTimer.isActive) {
                        animatedAccent
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                    leadingIconContentColor = if (sleepTimer.isActive) {
                        animatedAccent
                    } else {
                        TextSecondary
                    }
                )
            )

            AssistChip(
                onClick = { showSpeedSheet = true },
                label = { Text("%.2fx".format(speed)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            AssistChip(
                onClick = onOpenEqualizer,
                label = { Text("Equalizer") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Equalizer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }

    if (showSleepSheet) {
        SleepTimerSheet(
            state = sleepTimer,
            defaultMinutes = viewModel.defaultSleepMinutes,
            onStart = viewModel::startSleepTimer,
            onCancel = viewModel::cancelSleepTimer,
            onDismiss = { showSleepSheet = false }
        )
    }

    if (showSpeedSheet) {
        PlaybackSpeedSheet(
            currentSpeed = speed,
            onSpeedChange = viewModel::setSpeed,
            onDismiss = { showSpeedSheet = false }
        )
    }
}

private const val DISMISS_THRESHOLD_PX = 220f

package com.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.model.Track
import com.musicplayer.ui.components.Artwork
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.components.NowPlayingBars
import com.musicplayer.ui.theme.Radius
import com.musicplayer.ui.theme.TextMuted
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.ui.viewmodel.QueueViewModel
import com.musicplayer.util.TimeFormatter
import kotlin.math.roundToInt

private val QUEUE_ROW_HEIGHT = 64.dp

/**
 * The up-next list.
 *
 * Rows can be long-pressed and dragged to reorder; the move is applied to the
 * real player queue as the item crosses each neighbour, so what you see is
 * always what will play.
 */
@Composable
fun QueueScreen(
    onNavigateBack: () -> Unit,
    viewModel: QueueViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues()
) {
    val queueState by viewModel.queueState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    val tracks = queueState.tracks
    val density = LocalDensity.current
    val rowHeightPx = with(density) { QUEUE_ROW_HEIGHT.toPx() }

    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(
            title = "Queue",
            subtitle = if (tracks.isEmpty()) {
                "Nothing queued"
            } else {
                "${tracks.size} songs · ${TimeFormatter.formatTotal(tracks.sumOf { it.duration })}"
            },
            onNavigateBack = onNavigateBack,
            actions = {
                if (tracks.isNotEmpty()) {
                    TextButton(onClick = viewModel::clear) { Text("Clear") }
                }
            }
        )

        if (tracks.isEmpty()) {
            EmptyState(
                title = "Queue is empty",
                subtitle = "Play something, or use \"Add to queue\" from any song menu.",
                icon = Icons.Rounded.QueueMusic
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding() + 12.dp
            )
        ) {
            // Deliberately keyed by position: a queue can legitimately hold the
            // same song twice, and positional identity is what keeps a drag
            // attached to the finger while the list reorders underneath it.
            itemsIndexed(tracks) { index, track ->
                val currentIndex by rememberUpdatedState(index)
                val lastIndex by rememberUpdatedState(tracks.lastIndex)
                val isDragging = draggingIndex == index

                QueueRow(
                    track = track,
                    isActive = track.id == currentTrack?.id,
                    isPlaying = playbackState.isPlaying,
                    onClick = { viewModel.playAt(index) },
                    onRemove = { viewModel.remove(index) },
                    dragHandleModifier = Modifier.pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingIndex = currentIndex
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                draggingIndex = -1
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggingIndex = -1
                                dragOffset = 0f
                            }
                        ) { change, amount ->
                            change.consume()
                            if (draggingIndex < 0) return@detectDragGesturesAfterLongPress

                            dragOffset += amount.y
                            val shift = (dragOffset / rowHeightPx).roundToInt()
                            if (shift != 0) {
                                val from = draggingIndex
                                val to = (from + shift).coerceIn(0, lastIndex)
                                if (to != from) {
                                    viewModel.move(from, to)
                                    draggingIndex = to
                                    dragOffset -= (to - from) * rowHeightPx
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            if (isDragging) {
                                translationY = dragOffset
                                scaleX = 1.02f
                                scaleY = 1.02f
                                shadowElevation = 12f
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun QueueRow(
    track: Track,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(QUEUE_ROW_HEIGHT)
            .padding(horizontal = 12.dp)
            .clip(Radius.card)
            .background(
                if (isActive) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = dragHandleModifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.DragHandle,
                contentDescription = "Reorder",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(contentAlignment = Alignment.Center) {
            Artwork(uri = track.artworkUri, size = 42.dp, placeholderIconSize = 18.dp)
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(Radius.artworkSmall)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    NowPlayingBars(
                        color = MaterialTheme.colorScheme.primary,
                        isPlaying = isPlaying,
                        size = 16.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove from queue",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

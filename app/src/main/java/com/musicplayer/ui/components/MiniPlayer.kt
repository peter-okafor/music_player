package com.musicplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Track
import com.musicplayer.ui.theme.Radius
import com.musicplayer.ui.theme.SurfaceElevated
import com.musicplayer.ui.theme.SurfaceLight
import com.musicplayer.ui.theme.TextSecondary

/**
 * Floating now-playing bar.
 *
 * Sits above the navigation bar as a rounded card rather than a full-width
 * strip, and supports horizontal swipes to skip — the interaction people
 * already know from every major player.
 */
@Composable
fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    progress: Float,
    isFavorite: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = track != null,
        enter = slideInVertically(initialOffsetY = { it * 2 }),
        exit = slideOutVertically(targetOffsetY = { it * 2 }),
        modifier = modifier
    ) {
        val current = track ?: return@AnimatedVisibility
        val accent = MaterialTheme.colorScheme.primary
        val dragTotal = remember { mutableFloatStateOf(0f) }
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            label = "mini-progress"
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(Radius.card)
                .background(
                    Brush.horizontalGradient(listOf(SurfaceElevated, SurfaceLight))
                )
                .border(1.dp, Color.White.copy(alpha = 0.06f), Radius.card)
                .clickable(onClick = onExpand)
                .pointerInput(current.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragTotal.floatValue <= -SWIPE_THRESHOLD_PX -> onNext()
                                dragTotal.floatValue >= SWIPE_THRESHOLD_PX -> onPrevious()
                            }
                            dragTotal.floatValue = 0f
                        },
                        onDragCancel = { dragTotal.floatValue = 0f }
                    ) { _, dragAmount ->
                        dragTotal.floatValue += dragAmount
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Artwork(
                    uri = current.artworkUri,
                    size = 44.dp,
                    shape = Radius.artworkSmall,
                    placeholderIconSize = 20.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = current.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (isFavorite) {
                            Icons.Rounded.Favorite
                        } else {
                            Icons.Rounded.FavoriteBorder
                        },
                        contentDescription = "Favourite",
                        tint = if (isFavorite) accent else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(onClick = onNext, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Hairline progress indicator along the bottom edge.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(2.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(2.dp)
                        .background(accent)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private const val SWIPE_THRESHOLD_PX = 120f

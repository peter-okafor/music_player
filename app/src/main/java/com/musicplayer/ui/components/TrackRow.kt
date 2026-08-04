package com.musicplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Track
import com.musicplayer.ui.theme.Radius
import com.musicplayer.ui.theme.TextMuted
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.util.TimeFormatter

/**
 * One song in a list.
 *
 * The active row gets a tinted container, a coloured title and animated bars
 * instead of a static icon, so it stays identifiable while scrolling.
 */
@Composable
fun TrackRow(
    track: Track,
    isActive: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayLater: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    trailingSlot: (@Composable () -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary

    val containerColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        } else {
            Color.Transparent
        },
        label = "track-row-container"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(Radius.card)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Artwork(uri = track.artworkUri, size = 50.dp, shape = Radius.artworkSmall)
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(Radius.artworkSmall)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    NowPlayingBars(color = accent, isPlaying = isPlaying)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isActive) accent else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = "${track.artist} · ${TimeFormatter.formatDuration(track.duration)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailingSlot != null) {
            trailingSlot()
        } else {
            if (isFavorite) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Favourite",
                    tint = accent,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(18.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isFavorite) "Remove from favourites" else "Add to favourites") },
                        onClick = {
                            showMenu = false
                            onToggleFavorite()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Rounded.Favorite
                                } else {
                                    Icons.Rounded.FavoriteBorder
                                },
                                contentDescription = null,
                                tint = if (isFavorite) accent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Play next") },
                        onClick = {
                            showMenu = false
                            onPlayNext()
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.PlaylistPlay, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to queue") },
                        onClick = {
                            showMenu = false
                            onPlayLater()
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.QueueMusic, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to playlist") },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.PlaylistAdd, contentDescription = null)
                        }
                    )
                    if (onRemoveFromPlaylist != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Remove from playlist") },
                            onClick = {
                                showMenu = false
                                onRemoveFromPlaylist()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.model.Track
import com.musicplayer.ui.components.Artwork
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.components.QuickActionCard
import com.musicplayer.ui.components.QuickActionRow
import com.musicplayer.ui.components.SectionHeader
import com.musicplayer.ui.theme.Radius
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenFavorites: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSongs: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues()
) {
    val summary by viewModel.summary.collectAsState()
    val recent by viewModel.recentTracks.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            LibraryHeader(
                title = "Your library",
                subtitle = "${summary.trackCount} songs · ${summary.albumCount} albums",
                actions = {
                    IconButton(onClick = onOpenEqualizer) {
                        Icon(Icons.Rounded.Equalizer, contentDescription = "Equalizer")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                }
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                QuickActionRow {
                    QuickActionCard(
                        title = "Shuffle all",
                        subtitle = "${summary.trackCount} songs",
                        icon = Icons.Rounded.Shuffle,
                        onClick = viewModel::shuffleAll,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Favourites",
                        subtitle = "${favorites.size} songs",
                        icon = Icons.Rounded.Favorite,
                        onClick = onOpenFavorites,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                QuickActionRow {
                    QuickActionCard(
                        title = "Folders",
                        subtitle = "Browse by location",
                        icon = Icons.Rounded.Folder,
                        onClick = onOpenFolders,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Queue",
                        subtitle = "Now playing",
                        icon = Icons.Rounded.QueueMusic,
                        onClick = onOpenQueue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (recent.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(28.dp)) }
            item { SectionHeader(title = "Jump back in", action = "All songs", onAction = onOpenSongs) }
            item {
                TrackCarousel(
                    tracks = recent,
                    onTrackClick = { track -> viewModel.playTrack(track, recent) }
                )
            }
        }

        if (mostPlayed.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(28.dp)) }
            item { SectionHeader(title = "Most played") }
            item {
                TrackCarousel(
                    tracks = mostPlayed,
                    onTrackClick = { track -> viewModel.playTrack(track, mostPlayed) }
                )
            }
        }

        if (recent.isEmpty() && mostPlayed.isEmpty()) {
            item { Spacer(modifier = Modifier.height(28.dp)) }
            item {
                Text(
                    text = "Play something and your recents will show up here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun TrackCarousel(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tracks, key = { it.id }) { track ->
            Column(
                modifier = Modifier
                    .width(132.dp)
                    .clip(Radius.card)
                    .clickable { onTrackClick(track) }
                    .padding(4.dp)
            ) {
                Artwork(
                    uri = track.artworkUri,
                    size = 124.dp,
                    shape = Radius.artworkMedium,
                    placeholderIconSize = 36.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
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
        }
    }
}

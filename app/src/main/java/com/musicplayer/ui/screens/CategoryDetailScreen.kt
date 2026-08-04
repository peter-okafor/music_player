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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.model.Track
import com.musicplayer.ui.components.Artwork
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.components.LoadingState
import com.musicplayer.ui.components.PlaylistSelectionDialog
import com.musicplayer.ui.components.TrackList
import com.musicplayer.ui.theme.Radius
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.ui.viewmodel.CategoryDetailViewModel
import com.musicplayer.ui.viewmodel.CategoryType
import com.musicplayer.util.TimeFormatter

@Composable
fun CategoryDetailScreen(
    categoryType: CategoryType,
    categoryId: String,
    categoryName: String,
    onNavigateBack: () -> Unit,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    onMessage: (String) -> Unit = {}
) {
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val artworkUri by viewModel.artworkUri.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val message by viewModel.message.collectAsState()

    var playlistTarget by remember { mutableStateOf<Track?>(null) }

    LaunchedEffect(categoryType, categoryId) { viewModel.load(categoryType, categoryId) }

    LaunchedEffect(message) {
        message?.let {
            onMessage(it)
            viewModel.consumeMessage()
        }
    }

    val typeLabel = when (categoryType) {
        CategoryType.ALBUM -> "Album"
        CategoryType.ARTIST -> "Artist"
        CategoryType.FOLDER -> "Folder"
        CategoryType.PLAYLIST -> "Playlist"
        CategoryType.FAVORITES -> "Favourites"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(
            title = categoryName,
            subtitle = typeLabel,
            onNavigateBack = onNavigateBack
        )

        when {
            isLoading && tracks.isEmpty() -> LoadingState()

            tracks.isEmpty() -> EmptyState(
                title = "Nothing here yet",
                subtitle = "This $typeLabel doesn't contain any playable songs."
            )

            else -> TrackList(
                tracks = tracks,
                activeTrackId = currentTrack?.id,
                isPlaying = playbackState.isPlaying,
                favoriteIds = favorites,
                onTrackClick = viewModel::playAt,
                onToggleFavorite = viewModel::toggleFavorite,
                onPlayNext = viewModel::playNext,
                onPlayLater = viewModel::playLater,
                onAddToPlaylist = { playlistTarget = it },
                onRemoveFromPlaylist = if (viewModel.isPlaylist) {
                    viewModel::removeFromPlaylist
                } else {
                    null
                },
                contentPadding = PaddingValues(
                    bottom = contentPadding.calculateBottomPadding() + 12.dp
                ),
                header = {
                    item {
                        DetailHeader(
                            title = categoryName,
                            subtitle = "${tracks.size} songs · " +
                                TimeFormatter.formatTotal(tracks.sumOf { it.duration }),
                            artworkUri = artworkUri,
                            onPlay = viewModel::playAll,
                            onShuffle = viewModel::shuffle
                        )
                    }
                }
            )
        }
    }

    playlistTarget?.let { track ->
        PlaylistSelectionDialog(
            track = track,
            playlists = playlists,
            onDismiss = { playlistTarget = null },
            onPlaylistSelected = { playlistId ->
                viewModel.addToPlaylist(playlistId, track)
                playlistTarget = null
            },
            onCreatePlaylist = { name ->
                viewModel.createPlaylistAndAdd(name, track)
                playlistTarget = null
            }
        )
    }
}

@Composable
private fun DetailHeader(
    title: String,
    subtitle: String,
    artworkUri: android.net.Uri?,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Artwork(
            uri = artworkUri,
            size = 180.dp,
            shape = Radius.artworkLarge,
            placeholderIconSize = 56.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPlay,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play")
            }
            OutlinedButton(
                onClick = onShuffle,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shuffle")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.model.Track
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.components.LoadingState
import com.musicplayer.ui.components.PlaylistSelectionDialog
import com.musicplayer.ui.components.TrackList
import com.musicplayer.ui.components.TrackSortMenu
import com.musicplayer.ui.viewmodel.SongsViewModel

/**
 * The main song list. Doubles as the Favourites screen — the only difference
 * is the filter applied in the view model, so both stay in sync.
 */
@Composable
fun SongsScreen(
    favoritesOnly: Boolean = false,
    title: String = if (favoritesOnly) "Favourites" else "Songs",
    onNavigateBack: (() -> Unit)? = null,
    viewModel: SongsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    onMessage: (String) -> Unit = {}
) {
    val tracks by viewModel.visibleTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.query.collectAsState()
    val searchActive by viewModel.searchActive.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val message by viewModel.message.collectAsState()

    var playlistTarget by remember { mutableStateOf<Track?>(null) }

    LaunchedEffect(favoritesOnly) { viewModel.load(favoritesOnly) }

    LaunchedEffect(message) {
        message?.let {
            onMessage(it)
            viewModel.consumeMessage()
        }
    }

    val layoutDirection = LocalLayoutDirection.current

    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(
            title = title,
            subtitle = "${tracks.size} songs",
            searchQuery = query,
            onSearchQueryChange = viewModel::setQuery,
            searchActive = searchActive,
            onSearchActiveChange = viewModel::setSearchActive,
            searchPlaceholder = "Search songs, artists, albums",
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(
                    onClick = viewModel::shuffleVisible,
                    enabled = tracks.isNotEmpty()
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle these songs")
                }
                TrackSortMenu(state = sort, onChange = viewModel::setSort)
            }
        )

        when {
            isLoading && tracks.isEmpty() -> LoadingState()

            tracks.isEmpty() && query.isNotBlank() -> EmptyState(
                title = "No matches",
                subtitle = "Nothing in your library matches \"$query\"."
            )

            tracks.isEmpty() && favoritesOnly -> EmptyState(
                title = "No favourites yet",
                subtitle = "Tap the heart on any song to keep it here."
            )

            tracks.isEmpty() -> EmptyState(
                title = "No music found",
                subtitle = "Add some audio files to your device and pull up Settings to rescan."
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
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding()
                )
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

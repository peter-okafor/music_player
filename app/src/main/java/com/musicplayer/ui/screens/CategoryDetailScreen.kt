package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.model.Track
import com.musicplayer.ui.components.PlaylistSelectionDialog
import com.musicplayer.ui.components.TrackList
import com.musicplayer.ui.theme.Primary
import com.musicplayer.ui.viewmodel.CategoryDetailViewModel
import com.musicplayer.ui.viewmodel.CategoryType

@Composable
fun CategoryDetailScreen(
    categoryType: CategoryType,
    categoryId: String,
    categoryName: String,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    bottomPadding: Float = 0f
) {
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }

    LaunchedEffect(categoryType, categoryId) {
        viewModel.loadTracks(categoryType, categoryId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
        }

        if (isLoading && tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            TrackList(
                tracks = tracks,
                activeTrackId = currentTrack?.id,
                isPlaying = playbackState.isPlaying,
                isLoading = isLoading,
                onTrackClick = { index -> viewModel.onTrackSelected(index) },
                onAddToQueue = { track -> viewModel.addToQueue(track) },
                onAddToPlaylist = { track ->
                    selectedTrackForPlaylist = track
                    showPlaylistDialog = true
                },
                onLoadMore = { },
                contentPadding = PaddingValues(bottom = bottomPadding.dp)
            )
        }
    }

    // Playlist selection dialog
    if (showPlaylistDialog && selectedTrackForPlaylist != null) {
        PlaylistSelectionDialog(
            track = selectedTrackForPlaylist!!,
            onDismiss = {
                showPlaylistDialog = false
                selectedTrackForPlaylist = null
            },
            onPlaylistSelected = { playlistId ->
                // TODO: Add track to playlist
                showPlaylistDialog = false
                selectedTrackForPlaylist = null
            }
        )
    }
}

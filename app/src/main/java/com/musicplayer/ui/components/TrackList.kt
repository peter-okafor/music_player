package com.musicplayer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Track
import com.musicplayer.ui.theme.Primary

@Composable
fun TrackList(
    tracks: List<Track>,
    activeTrackId: String?,
    isPlaying: Boolean,
    isLoading: Boolean,
    onTrackClick: (Int) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onLoadMore: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        itemsIndexed(
            items = tracks,
            key = { _, track -> track.id }
        ) { index, track ->
            TrackItem(
                track = track,
                isActive = track.id == activeTrackId,
                isPlaying = track.id == activeTrackId && isPlaying,
                onClick = { onTrackClick(index) },
                onAddToQueue = { onAddToQueue(track) },
                onAddToPlaylist = { onAddToPlaylist(track) }
            )

            // Load more when reaching near the end
            if (index >= tracks.size - 5) {
                onLoadMore()
            }
        }

        // Loading indicator at bottom
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

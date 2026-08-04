package com.musicplayer.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Track

/**
 * The shared song list.
 *
 * The whole library is held in memory by the repository, so this renders the
 * already-filtered/sorted list directly — no paging callbacks to mis-fire
 * during a search.
 */
@Composable
fun TrackList(
    tracks: List<Track>,
    activeTrackId: String?,
    isPlaying: Boolean,
    favoriteIds: Set<String>,
    onTrackClick: (Int) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onPlayLater: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 12.dp),
    onRemoveFromPlaylist: ((Track) -> Unit)? = null,
    header: (LazyListScope.() -> Unit)? = null
) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = contentPadding
    ) {
        header?.invoke(this)

        itemsIndexed(
            items = tracks,
            key = { index, track -> "${track.id}-$index" }
        ) { index, track ->
            TrackRow(
                track = track,
                isActive = track.id == activeTrackId,
                isPlaying = track.id == activeTrackId && isPlaying,
                isFavorite = favoriteIds.contains(track.id),
                onClick = { onTrackClick(index) },
                onToggleFavorite = { onToggleFavorite(track) },
                onPlayNext = { onPlayNext(track) },
                onPlayLater = { onPlayLater(track) },
                onAddToPlaylist = { onAddToPlaylist(track) },
                onRemoveFromPlaylist = onRemoveFromPlaylist?.let { remove -> { remove(track) } }
            )
        }
    }
}

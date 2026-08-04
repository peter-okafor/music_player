package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.ui.components.ArtistRow
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.components.LoadingState
import com.musicplayer.ui.viewmodel.ArtistsViewModel

@Composable
fun ArtistsScreen(
    onArtistClick: (id: String, name: String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ArtistsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues()
) {
    val artists by viewModel.visibleArtists.collectAsState()
    val artwork by viewModel.artwork.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.query.collectAsState()
    val searchActive by viewModel.searchActive.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(
            title = "Artists",
            subtitle = "${artists.size} artists",
            searchQuery = query,
            onSearchQueryChange = viewModel::setQuery,
            searchActive = searchActive,
            onSearchActiveChange = viewModel::setSearchActive,
            searchPlaceholder = "Search artists",
            onNavigateBack = onNavigateBack
        )

        when {
            isLoading && artists.isEmpty() -> LoadingState()

            artists.isEmpty() -> EmptyState(
                title = if (query.isBlank()) "No artists yet" else "No matching artists",
                subtitle = if (query.isBlank()) null else "Nothing matches \"$query\"."
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = contentPadding.calculateBottomPadding() + 12.dp
                )
            ) {
                items(artists, key = { it.id }) { artist ->
                    ArtistRow(
                        name = artist.name,
                        subtitle = "${artist.trackCount} songs · ${artist.albumCount} albums",
                        artworkUri = artwork[artist.name],
                        onClick = { onArtistClick(artist.name, artist.name) }
                    )
                }
            }
        }
    }
}

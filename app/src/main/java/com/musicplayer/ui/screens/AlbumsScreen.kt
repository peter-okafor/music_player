package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.ui.components.AlbumTile
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.components.LoadingState
import com.musicplayer.ui.viewmodel.AlbumsViewModel

@Composable
fun AlbumsScreen(
    onAlbumClick: (id: String, name: String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AlbumsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues()
) {
    val albums by viewModel.visibleAlbums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.query.collectAsState()
    val searchActive by viewModel.searchActive.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(
            title = "Albums",
            subtitle = "${albums.size} albums",
            searchQuery = query,
            onSearchQueryChange = viewModel::setQuery,
            searchActive = searchActive,
            onSearchActiveChange = viewModel::setSearchActive,
            searchPlaceholder = "Search albums",
            onNavigateBack = onNavigateBack
        )

        when {
            isLoading && albums.isEmpty() -> LoadingState()

            albums.isEmpty() -> EmptyState(
                title = if (query.isBlank()) "No albums yet" else "No matching albums",
                subtitle = if (query.isBlank()) {
                    "Albums appear once your device has tagged music."
                } else {
                    "Nothing matches \"$query\"."
                }
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(
                    bottom = contentPadding.calculateBottomPadding() + 12.dp
                )
            ) {
                items(albums, key = { it.id }) { album ->
                    AlbumTile(
                        name = album.name,
                        artist = album.artist,
                        artworkUri = album.artworkUri,
                        onClick = { onAlbumClick(album.id, album.name) }
                    )
                }
            }
        }
    }
}

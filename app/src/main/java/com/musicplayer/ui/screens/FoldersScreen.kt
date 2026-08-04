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
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.FolderRow
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.components.LoadingState
import com.musicplayer.ui.viewmodel.FoldersViewModel

@Composable
fun FoldersScreen(
    onFolderClick: (path: String, name: String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: FoldersViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues()
) {
    val folders by viewModel.visibleFolders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.query.collectAsState()
    val searchActive by viewModel.searchActive.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(
            title = "Folders",
            subtitle = "${folders.size} folders",
            searchQuery = query,
            onSearchQueryChange = viewModel::setQuery,
            searchActive = searchActive,
            onSearchActiveChange = viewModel::setSearchActive,
            searchPlaceholder = "Search folders",
            onNavigateBack = onNavigateBack
        )

        when {
            isLoading && folders.isEmpty() -> LoadingState()

            folders.isEmpty() -> EmptyState(
                title = if (query.isBlank()) "No folders found" else "No matching folders",
                subtitle = if (query.isBlank()) null else "Nothing matches \"$query\"."
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = contentPadding.calculateBottomPadding() + 12.dp
                )
            ) {
                items(folders, key = { it.path }) { folder ->
                    FolderRow(
                        name = folder.name,
                        subtitle = "${folder.trackCount} songs",
                        onClick = { onFolderClick(folder.path, folder.name) }
                    )
                }
            }
        }
    }
}

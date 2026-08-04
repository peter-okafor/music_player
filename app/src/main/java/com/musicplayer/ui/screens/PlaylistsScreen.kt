package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.model.Playlist
import com.musicplayer.ui.components.CollectionRow
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.LibraryHeader
import com.musicplayer.ui.components.LoadingState
import com.musicplayer.ui.viewmodel.PlaylistsScreenViewModel

@Composable
fun PlaylistsScreen(
    onPlaylistClick: (id: String, name: String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: PlaylistsScreenViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    onMessage: (String) -> Unit = {}
) {
    val playlists by viewModel.visiblePlaylists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.query.collectAsState()
    val searchActive by viewModel.searchActive.collectAsState()
    val message by viewModel.message.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Playlist?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(message) {
        message?.let {
            onMessage(it)
            viewModel.consumeMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LibraryHeader(
                title = "Playlists",
                subtitle = "${playlists.size} playlists",
                searchQuery = query,
                onSearchQueryChange = viewModel::setQuery,
                searchActive = searchActive,
                onSearchActiveChange = viewModel::setSearchActive,
                searchPlaceholder = "Search playlists",
                onNavigateBack = onNavigateBack
            )

            when {
                isLoading && playlists.isEmpty() -> LoadingState()

                playlists.isEmpty() -> EmptyState(
                    title = if (query.isBlank()) "No playlists yet" else "No matching playlists",
                    subtitle = if (query.isBlank()) {
                        "Create one and start collecting the songs you keep coming back to."
                    } else {
                        "Nothing matches \"$query\"."
                    },
                    icon = Icons.Rounded.QueueMusic,
                    actionLabel = if (query.isBlank()) "New playlist" else null,
                    onAction = if (query.isBlank()) {
                        { showCreateDialog = true }
                    } else {
                        null
                    }
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = contentPadding.calculateBottomPadding() + 88.dp
                    )
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CollectionRow(
                                title = playlist.name,
                                subtitle = "${playlist.trackCount} songs",
                                onClick = { onPlaylistClick(playlist.id, playlist.name) },
                                fallbackIcon = Icons.Rounded.QueueMusic,
                                modifier = Modifier.padding(end = 44.dp)
                            )
                            IconButton(
                                onClick = { pendingDelete = playlist },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete playlist"
                                )
                            }
                        }
                    }
                }
            }
        }

        if (playlists.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New playlist") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 20.dp,
                        bottom = contentPadding.calculateBottomPadding() + 20.dp
                    )
            )
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

    pendingDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete playlist?") },
            text = { Text("\"${playlist.name}\" will be removed. The songs stay on your device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlaylist(playlist.id)
                        pendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

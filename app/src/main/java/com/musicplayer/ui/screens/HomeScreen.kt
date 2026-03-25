package com.musicplayer.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.data.model.Track
import com.musicplayer.ui.components.CategoryButtonsGrid
import com.musicplayer.ui.components.PlaylistSelectionDialog
import com.musicplayer.ui.components.TrackList
import com.musicplayer.ui.theme.Primary
import com.musicplayer.ui.theme.TextSecondary
import com.musicplayer.ui.viewmodel.HomeViewModel
import com.musicplayer.ui.viewmodel.PermissionStatus

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToFolders: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToAlbums: () -> Unit = {},
    onNavigateToArtists: () -> Unit = {},
    bottomPadding: Float = 0f
) {
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val permissionStatus by viewModel.permissionStatus.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Filter tracks based on search query
    val filteredTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) {
            tracks
        } else {
            tracks.filter { track ->
                track.title.contains(searchQuery, ignoreCase = true) ||
                track.artist.contains(searchQuery, ignoreCase = true) ||
                track.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(permissionStatus) {
        if (permissionStatus == PermissionStatus.UNDETERMINED) {
            permissionLauncher.launch(permission)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Your Library",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search songs...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            cursorColor = Primary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    )
                }

                IconButton(
                    onClick = {
                        if (isSearchActive) {
                            searchQuery = ""
                            isSearchActive = false
                            focusManager.clearFocus()
                        } else {
                            isSearchActive = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearchActive) "Close search" else "Search",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            CategoryButtonsGrid(
                onFoldersClick = onNavigateToFolders,
                onPlaylistsClick = onNavigateToPlaylists,
                onAlbumsClick = onNavigateToAlbums,
                onArtistsClick = onNavigateToArtists
            )
        }

        when (permissionStatus) {
            PermissionStatus.UNDETERMINED -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            PermissionStatus.DENIED -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Permission Required",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please grant access to your music library to use this app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { permissionLauncher.launch(permission) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            PermissionStatus.GRANTED -> {
                if (tracks.isEmpty() && isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else if (tracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No music found on your device",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    TrackList(
                        tracks = filteredTracks,
                        activeTrackId = currentTrack?.id,
                        isPlaying = playbackState.isPlaying,
                        isLoading = isLoading && !isSearchActive,
                        onTrackClick = { index ->
                            // Find the actual index in the original tracks list
                            val track = filteredTracks[index]
                            val originalIndex = tracks.indexOf(track)
                            if (originalIndex >= 0) {
                                viewModel.onTrackSelected(originalIndex)
                            }
                        },
                        onPlayNext = { track -> viewModel.playNext(track) },
                        onPlayLater = { track -> viewModel.playLater(track) },
                        onAddToPlaylist = { track ->
                            selectedTrackForPlaylist = track
                            showPlaylistDialog = true
                        },
                        onLoadMore = { if (!isSearchActive) viewModel.loadMore() },
                        contentPadding = PaddingValues(bottom = bottomPadding.dp)
                    )
                }
            }
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

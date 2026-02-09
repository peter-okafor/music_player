package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.Playlist
import com.musicplayer.data.model.Track
import com.musicplayer.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPlaylists() {
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _playlists.value = playlistRepository.loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error loading playlists", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, track: Track) {
        viewModelScope.launch {
            try {
                playlistRepository.addTrackToPlaylist(playlistId, track)
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error adding track to playlist", e)
            }
        }
    }

    fun createPlaylistAndAddTrack(name: String, track: Track) {
        viewModelScope.launch {
            try {
                val playlistId = playlistRepository.createPlaylist(name)
                if (playlistId != null) {
                    playlistRepository.addTrackToPlaylist(playlistId, track)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error creating playlist", e)
            }
        }
    }
}

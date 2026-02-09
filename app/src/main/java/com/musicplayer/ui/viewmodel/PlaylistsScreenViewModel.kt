package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.Playlist
import com.musicplayer.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsScreenViewModel @Inject constructor(
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
                android.util.Log.e("PlaylistsScreenViewModel", "Error loading playlists", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                playlistRepository.createPlaylist(name)
                // Reload playlists after creating
                loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e("PlaylistsScreenViewModel", "Error creating playlist", e)
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(playlistId)
                // Reload playlists after deleting
                loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e("PlaylistsScreenViewModel", "Error deleting playlist", e)
            }
        }
    }
}

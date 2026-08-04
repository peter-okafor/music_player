package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.Playlist
import com.musicplayer.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsScreenViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val visiblePlaylists: StateFlow<List<Playlist>> =
        combine(_playlists, _query) { playlists, query ->
            if (query.isBlank()) {
                playlists
            } else {
                playlists.filter { it.name.contains(query, ignoreCase = true) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _playlists.value = playlistRepository.loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading playlists", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            runCatching {
                playlistRepository.createPlaylist(name)
                _playlists.value = playlistRepository.loadPlaylists()
            }.onSuccess { _message.value = "Created \"$name\"" }
                .onFailure { _message.value = "Could not create playlist" }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            runCatching {
                val deleted = playlistRepository.deletePlaylist(playlistId)
                _playlists.value = playlistRepository.loadPlaylists()
                _message.value = if (deleted) "Playlist deleted" else "Could not delete playlist"
            }.onFailure { _message.value = "Could not delete playlist" }
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setSearchActive(active: Boolean) {
        _searchActive.value = active
        if (!active) _query.value = ""
    }

    fun consumeMessage() {
        _message.value = null
    }

    companion object {
        private const val TAG = "PlaylistsScreenViewModel"
    }
}

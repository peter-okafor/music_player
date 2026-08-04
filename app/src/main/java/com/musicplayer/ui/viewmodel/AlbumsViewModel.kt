package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.Album
import com.musicplayer.data.repository.MediaRepository
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
class AlbumsViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _albums = MutableStateFlow<List<Album>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    val visibleAlbums: StateFlow<List<Album>> = combine(_albums, _query) { albums, query ->
        if (query.isBlank()) {
            albums
        } else {
            albums.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _albums.value = mediaRepository.loadAlbums()
            } catch (e: Exception) {
                android.util.Log.e("AlbumsViewModel", "Error loading albums", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setSearchActive(active: Boolean) {
        _searchActive.value = active
        if (!active) _query.value = ""
    }
}

package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.Folder
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
class FoldersViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    val visibleFolders: StateFlow<List<Folder>> = combine(_folders, _query) { folders, query ->
        if (query.isBlank()) {
            folders
        } else {
            folders.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.path.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _folders.value = mediaRepository.loadFolders()
            } catch (e: Exception) {
                android.util.Log.e("FoldersViewModel", "Error loading folders", e)
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

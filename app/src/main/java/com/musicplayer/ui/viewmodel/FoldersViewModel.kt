package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.Folder
import com.musicplayer.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadFolders() {
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _folders.value = mediaRepository.loadFolders()
            } catch (e: Exception) {
                android.util.Log.e("FoldersViewModel", "Error loading folders", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

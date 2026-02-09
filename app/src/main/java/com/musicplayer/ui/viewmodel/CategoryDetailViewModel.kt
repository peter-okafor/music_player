package com.musicplayer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.Track
import com.musicplayer.data.repository.MediaRepository
import com.musicplayer.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CategoryType {
    ALBUM,
    ARTIST,
    FOLDER
}

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val mediaRepository: MediaRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack

    fun loadTracks(categoryType: CategoryType, categoryId: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _tracks.value = when (categoryType) {
                    CategoryType.ALBUM -> mediaRepository.loadTracksByAlbum(categoryId)
                    CategoryType.ARTIST -> mediaRepository.loadTracksByArtist(categoryId)
                    CategoryType.FOLDER -> mediaRepository.loadTracksByFolder(categoryId)
                }
            } catch (e: Exception) {
                android.util.Log.e("CategoryDetailViewModel", "Error loading tracks", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTrackSelected(index: Int) {
        val allTracks = _tracks.value
        if (index in allTracks.indices) {
            playbackController.setQueue(allTracks, index)
        }
    }

    fun togglePlayPause() {
        playbackController.togglePlayPause()
    }

    fun next() {
        playbackController.next()
    }

    fun addToQueue(track: Track) {
        playbackController.addToQueue(listOf(track))
    }
}

package com.musicplayer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.Track
import com.musicplayer.data.repository.MediaRepository
import com.musicplayer.data.repository.PlaylistRepository
import com.musicplayer.data.repository.RemoveTrackResult
import com.musicplayer.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CategoryType {
    ALBUM,
    ARTIST,
    FOLDER,
    PLAYLIST
}

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val mediaRepository: MediaRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack

    private var currentCategoryType: CategoryType? = null
    private var currentCategoryId: String? = null

    fun loadTracks(categoryType: CategoryType, categoryId: String) {
        currentCategoryType = categoryType
        currentCategoryId = categoryId
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _tracks.value = when (categoryType) {
                    CategoryType.ALBUM -> mediaRepository.loadTracksByAlbum(categoryId)
                    CategoryType.ARTIST -> mediaRepository.loadTracksByArtist(categoryId)
                    CategoryType.FOLDER -> mediaRepository.loadTracksByFolder(categoryId)
                    CategoryType.PLAYLIST -> playlistRepository.loadPlaylistTracks(categoryId)
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

    fun playNext(track: Track) {
        playbackController.playNext(listOf(track))
    }

    fun playLater(track: Track) {
        playbackController.playLater(listOf(track))
    }

    fun removeTrackFromPlaylist(track: Track) {
        android.util.Log.d("CategoryDetailViewModel", "removeTrackFromPlaylist called for track: ${track.id}, title: ${track.title}")
        android.util.Log.d("CategoryDetailViewModel", "currentCategoryType: $currentCategoryType, currentCategoryId: $currentCategoryId")

        val playlistId = currentCategoryId
        if (playlistId == null) {
            android.util.Log.e("CategoryDetailViewModel", "playlistId is null, cannot remove track")
            return
        }
        if (currentCategoryType != CategoryType.PLAYLIST) {
            android.util.Log.e("CategoryDetailViewModel", "Not a playlist, cannot remove track")
            return
        }

        viewModelScope.launch {
            try {
                android.util.Log.d("CategoryDetailViewModel", "Calling repository to remove track ${track.id} from playlist $playlistId")
                when (val result = playlistRepository.removeTrackFromPlaylist(playlistId, track.id)) {
                    is RemoveTrackResult.Success -> {
                        android.util.Log.d("CategoryDetailViewModel", "Track removed successfully")
                        _tracks.value = _tracks.value.filter { it.id != track.id }
                    }
                    is RemoveTrackResult.PlaylistNotOwned -> {
                        android.util.Log.e("CategoryDetailViewModel", "No write access to playlist")
                        _errorMessage.emit("Cannot modify this playlist - no write permission")
                    }
                    is RemoveTrackResult.NotFound -> {
                        android.util.Log.e("CategoryDetailViewModel", "Track not found in playlist")
                        _errorMessage.emit("Track not found in playlist")
                    }
                    is RemoveTrackResult.Failed -> {
                        android.util.Log.e("CategoryDetailViewModel", "Failed to remove track")
                        _errorMessage.emit("Failed to remove track")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CategoryDetailViewModel", "Error removing track from playlist", e)
                _errorMessage.emit("Error removing track")
            }
        }
    }

    fun isPlaylist(): Boolean = currentCategoryType == CategoryType.PLAYLIST
}

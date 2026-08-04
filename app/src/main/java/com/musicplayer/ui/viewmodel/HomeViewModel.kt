package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.Track
import com.musicplayer.data.prefs.UserPreferences
import com.musicplayer.data.repository.MediaRepository
import com.musicplayer.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibrarySummary(
    val trackCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val favoriteCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val playbackController: PlaybackController,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _summary = MutableStateFlow(LibrarySummary())
    val summary: StateFlow<LibrarySummary> = _summary.asStateFlow()

    private val _recentTracks = MutableStateFlow<List<Track>>(emptyList())
    val recentTracks: StateFlow<List<Track>> = _recentTracks.asStateFlow()

    private val _mostPlayed = MutableStateFlow<List<Track>>(emptyList())
    val mostPlayed: StateFlow<List<Track>> = _mostPlayed.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack
    val favorites: StateFlow<Set<String>> = preferences.favorites

    private var allTracks: List<Track> = emptyList()

    fun refresh() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                allTracks = mediaRepository.getAllTracks()
                val albums = allTracks.map { it.albumId }.toSet().size
                val artists = allTracks.map { it.artist }.toSet().size

                _summary.value = LibrarySummary(
                    trackCount = allTracks.size,
                    albumCount = albums,
                    artistCount = artists,
                    favoriteCount = preferences.favorites.value.size
                )

                val byId = allTracks.associateBy { it.id }
                _recentTracks.value = preferences.recentlyPlayed.value
                    .mapNotNull { byId[it] }
                    .take(RECENT_LIMIT)

                val counts = preferences.playCounts()
                _mostPlayed.value = allTracks
                    .filter { (counts[it.id] ?: 0) > 0 }
                    .sortedByDescending { counts[it.id] ?: 0 }
                    .take(RECENT_LIMIT)
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading library summary", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun shuffleAll() {
        if (allTracks.isEmpty()) return
        playbackController.shufflePlay(allTracks)
    }

    fun playTrack(track: Track, from: List<Track>) {
        val index = from.indexOfFirst { it.id == track.id }
        if (index >= 0) playbackController.setQueue(from, index)
    }

    fun togglePlayPause() = playbackController.togglePlayPause()

    fun next() = playbackController.next()

    companion object {
        private const val RECENT_LIMIT = 12
    }
}

package com.musicplayer.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.Playlist
import com.musicplayer.data.model.Track
import com.musicplayer.data.prefs.UserPreferences
import com.musicplayer.data.repository.MediaRepository
import com.musicplayer.data.repository.PlaylistRepository
import com.musicplayer.data.repository.RemoveTrackResult
import com.musicplayer.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CategoryType { ALBUM, ARTIST, FOLDER, PLAYLIST, FAVORITES }

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackController: PlaybackController,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _artworkUri = MutableStateFlow<Uri?>(null)
    val artworkUri: StateFlow<Uri?> = _artworkUri.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack
    val favorites: StateFlow<Set<String>> = preferences.favorites

    private var categoryType: CategoryType? = null
    private var categoryId: String? = null

    fun load(type: CategoryType, id: String) {
        categoryType = type
        categoryId = id
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loaded = when (type) {
                    CategoryType.ALBUM -> mediaRepository.loadTracksByAlbum(id)
                    CategoryType.ARTIST -> mediaRepository.loadTracksByArtist(id)
                    CategoryType.FOLDER -> mediaRepository.loadTracksByFolder(id)
                    CategoryType.PLAYLIST -> playlistRepository.loadPlaylistTracks(id)
                    CategoryType.FAVORITES -> mediaRepository.loadTracksByIds(
                        preferences.favorites.value.toList()
                    )
                }
                _tracks.value = loaded
                _artworkUri.value = loaded.firstNotNullOfOrNull { it.artworkUri }
                _playlists.value = playlistRepository.loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading category tracks", e)
                _message.value = "Could not load tracks"
            } finally {
                _isLoading.value = false
            }
        }
    }

    val isPlaylist: Boolean get() = categoryType == CategoryType.PLAYLIST

    val totalDurationMs: Long get() = _tracks.value.sumOf { it.duration }

    // --------------------------------------------------------------- actions

    fun playAt(index: Int) {
        val list = _tracks.value
        if (index in list.indices) playbackController.setQueue(list, index)
    }

    fun playAll() {
        if (_tracks.value.isNotEmpty()) playbackController.setQueue(_tracks.value, 0)
    }

    fun shuffle() = playbackController.shufflePlay(_tracks.value)

    fun playNext(track: Track) = playbackController.playNext(listOf(track))

    fun playLater(track: Track) = playbackController.playLater(listOf(track))

    fun toggleFavorite(track: Track) {
        val added = preferences.toggleFavorite(track.id)
        _message.value = if (added) "Added to favourites" else "Removed from favourites"
        if (categoryType == CategoryType.FAVORITES && !added) {
            _tracks.value = _tracks.value.filter { it.id != track.id }
        }
    }

    fun addToPlaylist(playlistId: String, track: Track) {
        viewModelScope.launch {
            runCatching { playlistRepository.addTrackToPlaylist(playlistId, track) }
                .onSuccess { _message.value = "Added to playlist" }
                .onFailure { _message.value = "Could not add to playlist" }
        }
    }

    fun createPlaylistAndAdd(name: String, track: Track) {
        viewModelScope.launch {
            runCatching {
                val id = playlistRepository.createPlaylist(name)
                if (id != null) {
                    playlistRepository.addTrackToPlaylist(id, track)
                    _playlists.value = playlistRepository.loadPlaylists()
                    _message.value = "Created \"$name\""
                } else {
                    _message.value = "Could not create playlist"
                }
            }.onFailure { _message.value = "Could not create playlist" }
        }
    }

    fun removeFromPlaylist(track: Track) {
        val playlistId = categoryId
        if (playlistId == null || categoryType != CategoryType.PLAYLIST) return

        viewModelScope.launch {
            when (playlistRepository.removeTrackFromPlaylist(playlistId, track.id)) {
                RemoveTrackResult.Success -> {
                    _tracks.value = _tracks.value.filter { it.id != track.id }
                    _message.value = "Removed from playlist"
                }
                RemoveTrackResult.PlaylistNotOwned ->
                    _message.value = "This playlist was created by another app and can't be edited"
                RemoveTrackResult.NotFound -> _message.value = "Track not found in playlist"
                RemoveTrackResult.Failed -> _message.value = "Could not remove track"
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    companion object {
        private const val TAG = "CategoryDetailViewModel"
    }
}

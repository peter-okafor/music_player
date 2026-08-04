package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.Playlist
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.SortState
import com.musicplayer.data.model.Track
import com.musicplayer.data.model.applySort
import com.musicplayer.data.prefs.UserPreferences
import com.musicplayer.data.repository.MediaRepository
import com.musicplayer.data.repository.PlaylistRepository
import com.musicplayer.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Songs tab and the Favourites screen.
 *
 * Sorting and filtering happen over the repository's cached list, so typing
 * in the search field never touches MediaStore.
 */
@HiltViewModel
class SongsViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackController: PlaybackController,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    private val _sort = MutableStateFlow(
        SortState.parse(preferences.getSort(SORT_TAB, SortState().serialize()))
    )
    val sort: StateFlow<SortState> = _sort.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack
    val favorites: StateFlow<Set<String>> = preferences.favorites

    /** The list actually rendered: filtered, then sorted. */
    val visibleTracks: StateFlow<List<Track>> = combine(
        _allTracks,
        _query,
        _sort,
        _favoritesOnly,
        preferences.favorites
    ) { tracks, query, sort, favoritesOnly, favoriteIds ->
        tracks
            .filter { !favoritesOnly || favoriteIds.contains(it.id) }
            .filter { track ->
                query.isBlank() ||
                    track.title.contains(query, ignoreCase = true) ||
                    track.artist.contains(query, ignoreCase = true) ||
                    track.album.contains(query, ignoreCase = true)
            }
            .applySort(sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(favoritesOnly: Boolean = false) {
        _favoritesOnly.value = favoritesOnly
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allTracks.value = mediaRepository.getAllTracks()
                _playlists.value = playlistRepository.loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading tracks", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { _allTracks.value = mediaRepository.refresh() }
            _isLoading.value = false
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setSearchActive(active: Boolean) {
        _searchActive.value = active
        if (!active) _query.value = ""
    }

    fun setSort(state: SortState) {
        _sort.value = state
        preferences.setSort(SORT_TAB, state.serialize())
    }

    // --------------------------------------------------------------- actions

    fun playAt(index: Int) {
        val tracks = visibleTracks.value
        if (index in tracks.indices) playbackController.setQueue(tracks, index)
    }

    fun shuffleVisible() {
        playbackController.shufflePlay(visibleTracks.value)
    }

    fun playNext(track: Track) = playbackController.playNext(listOf(track))

    fun playLater(track: Track) = playbackController.playLater(listOf(track))

    fun toggleFavorite(track: Track) {
        val added = preferences.toggleFavorite(track.id)
        _message.value = if (added) "Added to favourites" else "Removed from favourites"
    }

    fun addToPlaylist(playlistId: String, track: Track) {
        viewModelScope.launch {
            runCatching { playlistRepository.addTrackToPlaylist(playlistId, track) }
                .onSuccess {
                    _message.value = "Added to playlist"
                    _playlists.value = playlistRepository.loadPlaylists()
                }
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

    fun consumeMessage() {
        _message.value = null
    }

    companion object {
        private const val TAG = "SongsViewModel"
        private const val SORT_TAB = "songs"
    }
}

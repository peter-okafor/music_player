package com.musicplayer.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.QueueState
import com.musicplayer.data.model.Track
import com.musicplayer.data.repository.MediaRepository
import com.musicplayer.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PermissionStatus {
    UNDETERMINED,
    GRANTED,
    DENIED
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaRepository: MediaRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _permissionStatus = MutableStateFlow(PermissionStatus.UNDETERMINED)
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val queueState: StateFlow<QueueState> = playbackController.queueState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack

    private var currentPage = 0
    private var hasMoreData = true
    private val pageSize = 50

    init {
        checkPermission()
    }

    fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val isGranted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        _permissionStatus.value = if (isGranted) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.UNDETERMINED
        }

        if (isGranted) {
            loadInitialTracks()
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _permissionStatus.value = if (isGranted) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }

        if (isGranted) {
            loadInitialTracks()
        }
    }

    private fun loadInitialTracks() {
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                currentPage = 0
                hasMoreData = true

                val loadedTracks = mediaRepository.loadTracks(0, pageSize)
                _tracks.value = loadedTracks
                hasMoreData = loadedTracks.size == pageSize
                currentPage = 1
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading tracks", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value || !hasMoreData) return

        viewModelScope.launch {
            _isLoading.value = true

            val offset = currentPage * pageSize
            val loadedTracks = mediaRepository.loadTracks(offset, pageSize)

            if (loadedTracks.isNotEmpty()) {
                _tracks.value = _tracks.value + loadedTracks
                hasMoreData = loadedTracks.size == pageSize
                currentPage++
            } else {
                hasMoreData = false
            }

            _isLoading.value = false
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

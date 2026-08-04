package com.musicplayer.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.Track
import com.musicplayer.data.prefs.ThemeMode
import com.musicplayer.data.prefs.UserPreferences
import com.musicplayer.data.repository.MediaRepository
import com.musicplayer.player.PlaybackController
import com.musicplayer.player.SleepTimerController
import com.musicplayer.player.SleepTimerState
import com.musicplayer.ui.util.ArtworkPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PermissionStatus { UNDETERMINED, GRANTED, DENIED }

/**
 * State that outlives any single screen: permission, theme, the currently
 * playing track and the sleep timer. The app scaffold and the mini player
 * both read from here so they never disagree.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaRepository: MediaRepository,
    private val playbackController: PlaybackController,
    private val sleepTimerController: SleepTimerController,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _permissionStatus = MutableStateFlow(PermissionStatus.UNDETERMINED)
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    private val _accentColor = MutableStateFlow<Int?>(null)
    val accentColor: StateFlow<Int?> = _accentColor.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack
    val sleepTimerState: StateFlow<SleepTimerState> = sleepTimerController.state
    val favorites: StateFlow<Set<String>> = preferences.favorites
    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
    val dynamicColor: StateFlow<Boolean> = preferences.dynamicColor

    val requiredPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    init {
        checkPermission()
        observeArtwork()
    }

    fun checkPermission() {
        val granted = ContextCompat.checkSelfPermission(context, requiredPermission) ==
            PackageManager.PERMISSION_GRANTED
        _permissionStatus.value = if (granted) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.UNDETERMINED
        }
        if (granted) warmLibraryCache()
    }

    fun onPermissionResult(granted: Boolean) {
        _permissionStatus.value = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        if (granted) warmLibraryCache()
    }

    private fun warmLibraryCache() {
        viewModelScope.launch {
            runCatching { mediaRepository.getAllTracks() }
        }
    }

    private fun observeArtwork() {
        viewModelScope.launch {
            currentTrack.collectLatest { track ->
                if (!preferences.dynamicColor.value) {
                    _accentColor.value = null
                    return@collectLatest
                }
                _accentColor.value = ArtworkPalette.accentFor(context, track?.artworkUri)
            }
        }
    }

    // ------------------------------------------------------------- transport

    fun togglePlayPause() = playbackController.togglePlayPause()

    fun next() = playbackController.next()

    fun previous() = playbackController.previous()

    fun toggleFavorite(trackId: String) {
        preferences.toggleFavorite(trackId)
    }

    // ---------------------------------------------------------- sleep timer

    val defaultSleepMinutes: Int get() = preferences.lastSleepMinutes

    fun startSleepTimer(minutes: Int, finishTrack: Boolean) =
        sleepTimerController.start(minutes, finishTrack)

    fun cancelSleepTimer() = sleepTimerController.cancel()
}

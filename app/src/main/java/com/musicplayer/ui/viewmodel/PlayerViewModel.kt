package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.QueueState
import com.musicplayer.data.model.Track
import com.musicplayer.data.prefs.UserPreferences
import com.musicplayer.player.PlaybackController
import com.musicplayer.player.SleepTimerController
import com.musicplayer.player.SleepTimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackController: PlaybackController,
    private val sleepTimerController: SleepTimerController,
    private val preferences: UserPreferences
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val queueState: StateFlow<QueueState> = playbackController.queueState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack
    val sleepTimerState: StateFlow<SleepTimerState> = sleepTimerController.state
    val favorites: StateFlow<Set<String>> = preferences.favorites
    val playbackSpeed: StateFlow<Float> = preferences.playbackSpeed

    val defaultSleepMinutes: Int get() = preferences.lastSleepMinutes

    fun togglePlayPause() = playbackController.togglePlayPause()

    fun next() = playbackController.next()

    fun previous() = playbackController.previous()

    fun seekTo(positionMs: Long) = playbackController.seekTo(positionMs)

    fun seekBy(deltaMs: Long) = playbackController.seekBy(deltaMs)

    fun toggleShuffle() = playbackController.toggleShuffle()

    fun cycleRepeatMode() = playbackController.cycleRepeatMode()

    fun setSpeed(speed: Float) = playbackController.setPlaybackSpeed(speed)

    fun toggleFavorite() {
        currentTrack.value?.let { preferences.toggleFavorite(it.id) }
    }

    fun startSleepTimer(minutes: Int, finishTrack: Boolean) =
        sleepTimerController.start(minutes, finishTrack)

    fun cancelSleepTimer() = sleepTimerController.cancel()
}

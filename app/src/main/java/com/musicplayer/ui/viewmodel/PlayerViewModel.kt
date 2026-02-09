package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.QueueState
import com.musicplayer.data.model.Track
import com.musicplayer.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackController: PlaybackController
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val queueState: StateFlow<QueueState> = playbackController.queueState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack

    fun togglePlayPause() {
        playbackController.togglePlayPause()
    }

    fun next() {
        playbackController.next()
    }

    fun previous() {
        playbackController.previous()
    }

    fun seekTo(positionMs: Long) {
        playbackController.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playbackController.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playbackController.cycleRepeatMode()
    }
}

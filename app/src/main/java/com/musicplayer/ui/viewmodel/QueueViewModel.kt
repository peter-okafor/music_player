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
class QueueViewModel @Inject constructor(
    private val playbackController: PlaybackController
) : ViewModel() {

    val queueState: StateFlow<QueueState> = playbackController.queueState
    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState
    val currentTrack: StateFlow<Track?> = playbackController.currentTrack

    fun playAt(index: Int) = playbackController.selectTrack(index)

    fun move(from: Int, to: Int) = playbackController.moveQueueItem(from, to)

    fun remove(index: Int) = playbackController.removeQueueItem(index)

    fun clear() = playbackController.clearQueue()

    fun togglePlayPause() = playbackController.togglePlayPause()
}

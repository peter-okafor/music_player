package com.musicplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicplayer.data.model.PlaybackState
import com.musicplayer.data.model.QueueState
import com.musicplayer.data.model.RepeatMode
import com.musicplayer.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _queueState = MutableStateFlow(QueueState())
    val queueState: StateFlow<QueueState> = _queueState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    // Store original track order for shuffle
    private var originalTracks: List<Track> = emptyList()
    private var currentTracks: List<Track> = emptyList()

    private var isConnecting = false

    init {
        // Delay connection to allow app to fully initialize
        scope.launch {
            // delay(500)
            connectToService()
        }
    }

    private fun connectToService() {
        if (isConnecting || mediaController != null) return
        isConnecting = true

        try {
            val sessionToken = SessionToken(
                context,
                ComponentName(context, MusicService::class.java)
            )

            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture?.addListener({
                try {
                    mediaController = controllerFuture?.get()
                    setupPlayerListener()
                    startProgressUpdates()
                    android.util.Log.d("PlaybackController", "Connected to MusicService")
                } catch (e: Exception) {
                    android.util.Log.e("PlaybackController", "Error getting media controller", e)
                } finally {
                    isConnecting = false
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            android.util.Log.e("PlaybackController", "Error connecting to service", e)
            isConnecting = false
        }
    }

    fun ensureConnected() {
        if (mediaController == null && !isConnecting) {
            connectToService()
        }
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrack()
                updateQueueState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateQueueState()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateQueueState()
            }
        })
    }

    private fun startProgressUpdates() {
        scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(250)
            }
        }
    }

    private fun updatePlaybackState() {
        val controller = mediaController ?: return
        _playbackState.value = PlaybackState(
            isPlaying = controller.isPlaying,
            isLoaded = controller.playbackState == Player.STATE_READY,
            isBuffering = controller.playbackState == Player.STATE_BUFFERING,
            currentTimeMs = controller.currentPosition,
            durationMs = controller.duration.coerceAtLeast(0)
        )
    }

    private fun updateCurrentTrack() {
        val controller = mediaController ?: return
        val index = controller.currentMediaItemIndex
        if (index >= 0 && index < currentTracks.size) {
            _currentTrack.value = currentTracks[index]
        }
    }

    private fun updateQueueState() {
        val controller = mediaController ?: return
        _queueState.value = QueueState(
            tracks = currentTracks,
            currentIndex = controller.currentMediaItemIndex,
            shuffleEnabled = controller.shuffleModeEnabled,
            repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            }
        )
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun next() {
        mediaController?.seekToNextMediaItem()
    }

    fun previous() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else {
            controller.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        ensureConnected()
        val controller = mediaController
        if (controller == null) {
            android.util.Log.w("PlaybackController", "MediaController not ready, cannot set queue")
            return
        }

        originalTracks = tracks
        currentTracks = tracks

        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(track.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.artworkUri)
                        .build()
                )
                .build()
        }

        controller.setMediaItems(mediaItems, startIndex, 0)
        controller.prepare()
        controller.play()

        updateQueueState()
        updateCurrentTrack()
    }

    fun addToQueue(tracks: List<Track>) {
        val controller = mediaController ?: return

        currentTracks = currentTracks + tracks
        originalTracks = originalTracks + tracks

        tracks.forEach { track ->
            val mediaItem = MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(track.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.artworkUri)
                        .build()
                )
                .build()
            controller.addMediaItem(mediaItem)
        }

        updateQueueState()
    }

    fun selectTrack(index: Int) {
        val controller = mediaController ?: return
        if (index >= 0 && index < controller.mediaItemCount) {
            controller.seekTo(index, 0)
            controller.play()
        }
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun cycleRepeatMode() {
        val controller = mediaController ?: return
        controller.repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun release() {
        scope.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}

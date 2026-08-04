package com.musicplayer.player

import android.content.ComponentName
import android.content.Context
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
import com.musicplayer.data.prefs.UserPreferences
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

/**
 * App-side facade over the Media3 session.
 *
 * Owns the mirror of the player queue (Media3 only stores MediaItems, we need
 * the richer [Track]) and exposes it as state the UI can observe.
 */
@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferences
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

    /** Mirrors the media items currently loaded into the player. */
    private var currentTracks: MutableList<Track> = mutableListOf()

    private var isConnecting = false
    private var lastRecordedTrackId: String? = null

    init {
        scope.launch { connectToService() }
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
                    applyStoredSpeed()
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error getting media controller", e)
                } finally {
                    isConnecting = false
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error connecting to service", e)
            isConnecting = false
        }
    }

    fun ensureConnected() {
        if (mediaController == null && !isConnecting) connectToService()
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = updatePlaybackState()

            override fun onPlaybackStateChanged(playbackState: Int) = updatePlaybackState()

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrack()
                updateQueueState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = updateQueueState()

            override fun onRepeatModeChanged(repeatMode: Int) = updateQueueState()

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) =
                updateQueueState()
        })
    }

    private fun startProgressUpdates() {
        scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(PROGRESS_INTERVAL_MS)
            }
        }
    }

    private fun applyStoredSpeed() {
        val speed = preferences.playbackSpeed.value
        if (speed != 1f) mediaController?.setPlaybackSpeed(speed)
    }

    private fun updatePlaybackState() {
        val controller = mediaController ?: return
        _playbackState.value = PlaybackState(
            isPlaying = controller.isPlaying,
            isLoaded = controller.playbackState == Player.STATE_READY,
            isBuffering = controller.playbackState == Player.STATE_BUFFERING,
            currentTimeMs = controller.currentPosition.coerceAtLeast(0),
            durationMs = controller.duration.coerceAtLeast(0),
            speed = controller.playbackParameters.speed
        )
    }

    private fun updateCurrentTrack() {
        val controller = mediaController ?: return
        val index = controller.currentMediaItemIndex
        val track = currentTracks.getOrNull(index)
        _currentTrack.value = track

        if (track != null && track.id != lastRecordedTrackId) {
            lastRecordedTrackId = track.id
            preferences.recordPlayed(track.id)
        }
    }

    private fun updateQueueState() {
        val controller = mediaController ?: return
        _queueState.value = QueueState(
            tracks = currentTracks.toList(),
            currentIndex = controller.currentMediaItemIndex,
            shuffleEnabled = controller.shuffleModeEnabled,
            repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            }
        )
    }

    // ------------------------------------------------------------- transport

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    fun next() {
        mediaController?.seekToNextMediaItem()
    }

    fun previous() {
        val controller = mediaController ?: return
        if (controller.currentPosition > RESTART_THRESHOLD_MS) {
            controller.seekTo(0)
        } else {
            controller.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun seekBy(deltaMs: Long) {
        val controller = mediaController ?: return
        val target = (controller.currentPosition + deltaMs)
            .coerceIn(0, controller.duration.coerceAtLeast(0))
        controller.seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3f)
        mediaController?.setPlaybackSpeed(clamped)
        preferences.setPlaybackSpeed(clamped)
        updatePlaybackState()
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

    // ----------------------------------------------------------------- queue

    /** Replaces the queue and starts playing at [startIndex]. */
    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        ensureConnected()
        val controller = mediaController ?: run {
            android.util.Log.w(TAG, "MediaController not ready, cannot set queue")
            return
        }
        if (tracks.isEmpty()) return

        currentTracks = tracks.toMutableList()
        controller.setMediaItems(
            tracks.map(::createMediaItem),
            startIndex.coerceIn(0, tracks.lastIndex),
            0
        )
        controller.prepare()
        controller.play()

        updateQueueState()
        updateCurrentTrack()
    }

    /** Plays a collection in shuffled order starting from a random track. */
    fun shufflePlay(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        val shuffled = tracks.shuffled()
        setQueue(shuffled, 0)
        mediaController?.shuffleModeEnabled = true
    }

    /** Inserts tracks directly after the current one. */
    fun playNext(tracks: List<Track>) {
        ensureConnected()
        val controller = mediaController ?: return
        if (tracks.isEmpty()) return

        if (currentTracks.isEmpty() || controller.mediaItemCount == 0) {
            setQueue(tracks, 0)
            return
        }

        val insertIndex = (controller.currentMediaItemIndex + 1)
            .coerceIn(0, currentTracks.size)
        currentTracks.addAll(insertIndex, tracks)
        tracks.forEachIndexed { offset, track ->
            controller.addMediaItem(insertIndex + offset, createMediaItem(track))
        }
        updateQueueState()
    }

    /** Appends tracks to the end of the queue. */
    fun playLater(tracks: List<Track>) {
        ensureConnected()
        val controller = mediaController ?: return
        if (tracks.isEmpty()) return

        if (currentTracks.isEmpty() || controller.mediaItemCount == 0) {
            setQueue(tracks, 0)
            return
        }

        currentTracks.addAll(tracks)
        tracks.forEach { controller.addMediaItem(createMediaItem(it)) }
        updateQueueState()
    }

    /** Moves a queue entry, keeping the mirror list in sync with the player. */
    fun moveQueueItem(from: Int, to: Int) {
        val controller = mediaController ?: return
        if (from == to) return
        if (from !in currentTracks.indices) return
        val target = to.coerceIn(0, currentTracks.lastIndex)

        val track = currentTracks.removeAt(from)
        currentTracks.add(target, track)
        controller.moveMediaItem(from, target)
        updateQueueState()
        updateCurrentTrack()
    }

    fun removeQueueItem(index: Int) {
        val controller = mediaController ?: return
        if (index !in currentTracks.indices) return

        currentTracks.removeAt(index)
        controller.removeMediaItem(index)

        if (currentTracks.isEmpty()) {
            controller.stop()
            _currentTrack.value = null
        }
        updateQueueState()
        updateCurrentTrack()
    }

    fun clearQueue() {
        val controller = mediaController ?: return
        currentTracks.clear()
        controller.clearMediaItems()
        controller.stop()
        _currentTrack.value = null
        updateQueueState()
    }

    /** Jumps to a queue position. */
    fun selectTrack(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.seekTo(index, 0)
            controller.play()
        }
    }

    private fun createMediaItem(track: Track): MediaItem = MediaItem.Builder()
        .setMediaId(track.id)
        .setUri(track.contentUri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(track.artworkUri)
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .build()
        )
        .build()

    fun release() {
        scope.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }

    companion object {
        private const val TAG = "PlaybackController"
        private const val PROGRESS_INTERVAL_MS = 250L
        private const val RESTART_THRESHOLD_MS = 3000L
    }
}

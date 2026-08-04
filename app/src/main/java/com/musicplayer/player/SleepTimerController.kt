package com.musicplayer.player

import com.musicplayer.data.prefs.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingMs: Long = 0L,
    /** When true the timer waits for the current track to end before pausing. */
    val finishTrack: Boolean = false
)

/**
 * Pauses playback after a countdown.
 *
 * If [finishTrack] is set the pause is deferred until the playing track ends,
 * which is the behaviour people expect when falling asleep mid-song.
 */
@Singleton
class SleepTimerController @Inject constructor(
    private val playbackController: PlaybackController,
    private val preferences: UserPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    fun start(minutes: Int, finishTrack: Boolean = false) {
        cancel()
        preferences.lastSleepMinutes = minutes

        val totalMs = minutes * 60_000L
        _state.value = SleepTimerState(isActive = true, remainingMs = totalMs, finishTrack = finishTrack)

        job = scope.launch {
            var remaining = totalMs
            while (isActive && remaining > 0) {
                delay(TICK_MS)
                remaining -= TICK_MS
                _state.value = _state.value.copy(remainingMs = remaining.coerceAtLeast(0))
            }
            if (!isActive) return@launch

            if (finishTrack) waitForTrackEnd()
            playbackController.pause()
            _state.value = SleepTimerState()
        }
    }

    /** Extends an already-running timer. */
    fun addMinutes(minutes: Int) {
        val current = _state.value
        if (!current.isActive) {
            start(minutes, false)
            return
        }
        val newTotal = (current.remainingMs + minutes * 60_000L) / 60_000L
        start(newTotal.toInt().coerceAtLeast(1), current.finishTrack)
    }

    fun cancel() {
        job?.cancel()
        job = null
        _state.value = SleepTimerState()
    }

    private suspend fun waitForTrackEnd() {
        // Poll until the track is within a second of its end, capped so a
        // paused or stalled player can't hold the timer open forever.
        var guard = MAX_TRACK_WAIT_MS
        while (guard > 0) {
            val snapshot = playbackController.playbackState.value
            val remaining = snapshot.durationMs - snapshot.currentTimeMs
            if (!snapshot.isPlaying || (snapshot.durationMs > 0 && remaining <= TICK_MS)) return
            delay(TICK_MS)
            guard -= TICK_MS
        }
    }

    companion object {
        private const val TICK_MS = 1_000L
        private const val MAX_TRACK_WAIT_MS = 15 * 60_000L
    }
}

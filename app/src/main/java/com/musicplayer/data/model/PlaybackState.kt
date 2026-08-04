package com.musicplayer.data.model

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isLoaded: Boolean = false,
    val isBuffering: Boolean = false,
    val currentTimeMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1f
) {
    val progress: Float
        get() = if (durationMs > 0) (currentTimeMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

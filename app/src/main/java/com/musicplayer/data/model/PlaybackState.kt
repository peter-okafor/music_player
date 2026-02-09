package com.musicplayer.data.model

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isLoaded: Boolean = false,
    val isBuffering: Boolean = false,
    val currentTimeMs: Long = 0,
    val durationMs: Long = 0
)

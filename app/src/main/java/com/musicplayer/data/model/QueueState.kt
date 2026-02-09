package com.musicplayer.data.model

data class QueueState(
    val tracks: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

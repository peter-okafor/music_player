package com.musicplayer.data.model

data class Playlist(
    val id: String,
    val name: String,
    val trackCount: Int,
    val tracks: List<Track> = emptyList()
)

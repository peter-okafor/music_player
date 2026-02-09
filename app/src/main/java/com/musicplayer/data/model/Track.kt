package com.musicplayer.data.model

import android.net.Uri

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUri: Uri?,
    val contentUri: Uri,
    val duration: Long // milliseconds
)

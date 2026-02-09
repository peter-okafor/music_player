package com.musicplayer.data.model

import android.net.Uri

data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val artworkUri: Uri?,
    val trackCount: Int
)

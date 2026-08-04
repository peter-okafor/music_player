package com.musicplayer.data.model

import android.net.Uri

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUri: Uri?,
    val contentUri: Uri,
    val duration: Long, // milliseconds
    val albumId: String = "",
    val trackNumber: Int = 0,
    val year: Int = 0,
    /** Seconds since epoch, as reported by MediaStore. */
    val dateAdded: Long = 0L,
    val filePath: String = ""
)

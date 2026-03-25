package com.musicplayer.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.musicplayer.data.model.Playlist
import com.musicplayer.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class RemoveTrackResult {
    data object Success : RemoveTrackResult()
    data object NotFound : RemoveTrackResult()
    data object Failed : RemoveTrackResult()
    /** Playlist was created by another app and cannot be modified on Android 11+ */
    data object PlaylistNotOwned : RemoveTrackResult()
}

@Singleton
class PlaylistRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Suppress("DEPRECATION")
    suspend fun loadPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        val playlists = mutableListOf<Playlist>()

        try {
            val collection = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
            )

            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Audio.Playlists.NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown Playlist"

                    // Get track count for this playlist
                    val trackCount = getPlaylistTrackCount(id)

                    playlists.add(
                        Playlist(
                            id = id.toString(),
                            name = name,
                            trackCount = trackCount
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaylistRepository", "Error loading playlists", e)
        }

        playlists
    }

    @Suppress("DEPRECATION")
    private fun getPlaylistTrackCount(playlistId: Long): Int {
        var count = 0
        try {
            val membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
            context.contentResolver.query(
                membersUri,
                arrayOf(MediaStore.Audio.Playlists.Members._ID),
                null,
                null,
                null
            )?.use { cursor ->
                count = cursor.count
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaylistRepository", "Error getting playlist track count", e)
        }
        return count
    }

    @Suppress("DEPRECATION")
    suspend fun createPlaylist(name: String): String? = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Playlists.NAME, name)
                put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                values
            )

            uri?.lastPathSegment
        } catch (e: Exception) {
            android.util.Log.e("PlaylistRepository", "Error creating playlist", e)
            null
        }
    }

    @Suppress("DEPRECATION")
    suspend fun deletePlaylist(playlistId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                playlistId.toLong()
            )
            val deleted = context.contentResolver.delete(uri, null, null)
            deleted > 0
        } catch (e: Exception) {
            android.util.Log.e("PlaylistRepository", "Error deleting playlist", e)
            false
        }
    }

    @Suppress("DEPRECATION")
    suspend fun addTrackToPlaylist(playlistId: String, track: Track) = withContext(Dispatchers.IO) {
        try {
            val membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId.toLong())

            // Get the current max play order
            var playOrder = 0
            context.contentResolver.query(
                membersUri,
                arrayOf(MediaStore.Audio.Playlists.Members.PLAY_ORDER),
                null,
                null,
                "${MediaStore.Audio.Playlists.Members.PLAY_ORDER} DESC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    playOrder = cursor.getInt(0) + 1
                }
            }

            val values = ContentValues().apply {
                put(MediaStore.Audio.Playlists.Members.AUDIO_ID, track.id.toLong())
                put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, playOrder)
            }

            context.contentResolver.insert(membersUri, values)
        } catch (e: Exception) {
            android.util.Log.e("PlaylistRepository", "Error adding track to playlist", e)
        }
    }

    @Suppress("DEPRECATION")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String): RemoveTrackResult = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PlaylistRepository", "Removing track $trackId from playlist $playlistId")
            val membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId.toLong())
            android.util.Log.d("PlaylistRepository", "Members URI: $membersUri")

            // Check playlist ownership on Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                checkPlaylistOwnership(playlistId.toLong())
            }

            // First, find the member _ID for this audio track
            var memberId: Long? = null
            context.contentResolver.query(
                membersUri,
                arrayOf(MediaStore.Audio.Playlists.Members._ID),
                "${MediaStore.Audio.Playlists.Members.AUDIO_ID} = ?",
                arrayOf(trackId),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    memberId = cursor.getLong(0)
                }
            }

            if (memberId == null) {
                android.util.Log.e("PlaylistRepository", "Could not find member ID for track $trackId")
                return@withContext RemoveTrackResult.NotFound
            }

            android.util.Log.d("PlaylistRepository", "Found member ID: $memberId")

            // Try Method 1: Delete using WHERE clause on collection URI
            var deleted = context.contentResolver.delete(
                membersUri,
                "${MediaStore.Audio.Playlists.Members._ID} = ?",
                arrayOf(memberId.toString())
            )
            android.util.Log.d("PlaylistRepository", "Method 1 (WHERE clause) deleted count: $deleted")

            // If Method 1 fails, try Method 2: Delete using specific member URI
            if (deleted == 0) {
                val memberUri = ContentUris.withAppendedId(membersUri, memberId!!)
                deleted = context.contentResolver.delete(memberUri, null, null)
                android.util.Log.d("PlaylistRepository", "Method 2 (URI) deleted count: $deleted")
            }

            if (deleted > 0) {
                RemoveTrackResult.Success
            } else {
                RemoveTrackResult.Failed
            }
        } catch (e: SecurityException) {
            android.util.Log.e("PlaylistRepository", "SecurityException - no write access", e)
            RemoveTrackResult.PlaylistNotOwned
        } catch (e: Exception) {
            android.util.Log.e("PlaylistRepository", "Error removing track from playlist", e)
            RemoveTrackResult.Failed
        }
    }

    @Suppress("DEPRECATION")
    private fun checkPlaylistOwnership(playlistId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    playlistId
                )
                context.contentResolver.query(
                    uri,
                    arrayOf(MediaStore.Audio.Playlists._ID, MediaStore.MediaColumns.OWNER_PACKAGE_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val ownerIndex = cursor.getColumnIndex(MediaStore.MediaColumns.OWNER_PACKAGE_NAME)
                        if (ownerIndex >= 0) {
                            val owner = cursor.getString(ownerIndex)
                            android.util.Log.d("PlaylistRepository", "Playlist $playlistId owner: $owner")
                            android.util.Log.d("PlaylistRepository", "This app package: ${context.packageName}")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistRepository", "Error checking playlist ownership", e)
            }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun loadPlaylistTracks(playlistId: String): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        try {
            val membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId.toLong())

            val projection = arrayOf(
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.ALBUM,
                MediaStore.Audio.Playlists.Members.ALBUM_ID,
                MediaStore.Audio.Playlists.Members.DURATION
            )

            context.contentResolver.query(
                membersUri,
                projection,
                null,
                null,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)

                    val contentUri = android.content.ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = android.content.ContentUris.withAppendedId(
                        android.net.Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    tracks.add(
                        Track(
                            id = id.toString(),
                            title = title,
                            artist = artist,
                            album = album,
                            artworkUri = artworkUri,
                            contentUri = contentUri,
                            duration = duration
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaylistRepository", "Error loading playlist tracks", e)
        }

        tracks
    }
}

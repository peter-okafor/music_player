package com.musicplayer.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import com.musicplayer.data.model.Playlist
import com.musicplayer.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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

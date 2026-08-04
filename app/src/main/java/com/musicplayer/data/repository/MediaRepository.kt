package com.musicplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.musicplayer.data.model.Album
import com.musicplayer.data.model.Artist
import com.musicplayer.data.model.Folder
import com.musicplayer.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the on-device music library from MediaStore.
 *
 * The full track list is loaded once and cached in memory. Everything the UI
 * needs — sorting, searching, favourites, album/artist/folder grouping — is
 * then a cheap in-memory operation instead of a fresh content-resolver query,
 * which is what makes instant search and re-sorting feel native.
 */
@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheLock = Mutex()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private var loaded = false

    private val audioCollection: Uri
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    private val trackProjection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.DATA
    )

    /** Loads (or returns the cached) full library. */
    suspend fun getAllTracks(forceRefresh: Boolean = false): List<Track> = cacheLock.withLock {
        if (loaded && !forceRefresh) return@withLock _tracks.value

        val result = queryTracks(
            selection = null,
            selectionArgs = null,
            sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )
        _tracks.value = result
        loaded = true
        result
    }

    suspend fun refresh(): List<Track> = getAllTracks(forceRefresh = true)

    suspend fun loadTracksByAlbum(albumId: String): List<Track> = queryTracks(
        selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?",
        selectionArgs = arrayOf(albumId),
        sortOrder = "${MediaStore.Audio.Media.TRACK} ASC"
    )

    suspend fun loadTracksByArtist(artistName: String): List<Track> = queryTracks(
        selection = "${MediaStore.Audio.Media.ARTIST} = ?",
        selectionArgs = arrayOf(artistName),
        sortOrder = "${MediaStore.Audio.Media.ALBUM} ASC, ${MediaStore.Audio.Media.TRACK} ASC"
    )

    suspend fun loadTracksByFolder(folderPath: String): List<Track> = queryTracks(
        selection = "${MediaStore.Audio.Media.DATA} LIKE ?",
        selectionArgs = arrayOf("$folderPath/%"),
        sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
    ).filter { it.filePath.substringBeforeLast("/") == folderPath }

    /** Resolves a set of track ids, preserving the order they were given in. */
    suspend fun loadTracksByIds(ids: List<String>): List<Track> {
        if (ids.isEmpty()) return emptyList()
        val byId = getAllTracks().associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    suspend fun loadAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()
        try {
            context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.ARTIST,
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS
                ),
                null,
                null,
                "${MediaStore.Audio.Albums.ALBUM} COLLATE NOCASE ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
                val songsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    albums.add(
                        Album(
                            id = id.toString(),
                            name = cursor.getString(albumColumn) ?: "Unknown Album",
                            artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                            artworkUri = albumArtUri(id),
                            trackCount = cursor.getInt(songsColumn)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error loading albums", e)
        }
        albums
    }

    suspend fun loadArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<Artist>()
        try {
            context.contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Audio.Artists._ID,
                    MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
                ),
                null,
                null,
                "${MediaStore.Audio.Artists.ARTIST} COLLATE NOCASE ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
                val tracksColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
                val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)

                while (cursor.moveToNext()) {
                    artists.add(
                        Artist(
                            id = cursor.getLong(idColumn).toString(),
                            name = cursor.getString(artistColumn) ?: "Unknown Artist",
                            trackCount = cursor.getInt(tracksColumn),
                            albumCount = cursor.getInt(albumsColumn)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error loading artists", e)
        }
        artists
    }

    suspend fun loadFolders(): List<Folder> {
        val counts = mutableMapOf<String, Int>()
        getAllTracks().forEach { track ->
            if (track.filePath.isNotBlank()) {
                val dir = track.filePath.substringBeforeLast("/")
                counts[dir] = (counts[dir] ?: 0) + 1
            }
        }
        return counts.map { (path, count) ->
            Folder(
                path = path,
                name = path.substringAfterLast("/"),
                trackCount = count
            )
        }.sortedBy { it.name.lowercase() }
    }

    /** Finds the artwork for an artist by falling back to their first album. */
    suspend fun artistArtwork(artistName: String): Uri? =
        getAllTracks().firstOrNull { it.artist == artistName }?.artworkUri

    // ------------------------------------------------------------- internals

    private suspend fun queryTracks(
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String
    ): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        try {
            context.contentResolver.query(
                audioCollection,
                trackProjection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val columns = TrackColumns(cursor)
                while (cursor.moveToNext()) {
                    tracks.add(columns.read(cursor))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Track query failed", e)
        }
        tracks
    }

    private class TrackColumns(cursor: Cursor) {
        val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val track = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
        val year = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
        val dateAdded = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
        val data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

        fun read(cursor: Cursor): Track {
            val mediaId = cursor.getLong(id)
            val albumIdValue = cursor.getLong(albumId)
            // MediaStore encodes track numbers as disc*1000 + track.
            val rawTrack = if (track >= 0) cursor.getInt(track) else 0
            return Track(
                id = mediaId.toString(),
                title = cursor.getString(title) ?: "Unknown",
                artist = cursor.getString(artist) ?: "Unknown Artist",
                album = cursor.getString(album) ?: "Unknown Album",
                artworkUri = MediaRepository.albumArtUri(albumIdValue),
                contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaId
                ),
                duration = cursor.getLong(duration),
                albumId = albumIdValue.toString(),
                trackNumber = if (rawTrack > 1000) rawTrack % 1000 else rawTrack,
                year = if (year >= 0) cursor.getInt(year) else 0,
                dateAdded = if (dateAdded >= 0) cursor.getLong(dateAdded) else 0L,
                filePath = if (data >= 0) cursor.getString(data).orEmpty() else ""
            )
        }
    }

    companion object {
        private const val TAG = "MediaRepository"
        private val ALBUM_ART_BASE: Uri = Uri.parse("content://media/external/audio/albumart")

        fun albumArtUri(albumId: Long): Uri = ContentUris.withAppendedId(ALBUM_ART_BASE, albumId)
    }
}

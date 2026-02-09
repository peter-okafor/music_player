package com.musicplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.musicplayer.data.model.Album
import com.musicplayer.data.model.Artist
import com.musicplayer.data.model.Folder
import com.musicplayer.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun loadTracks(
        offset: Int = 0,
        limit: Int = 50
    ): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                // Skip to offset
                if (offset > 0 && cursor.count > offset) {
                    cursor.moveToPosition(offset - 1)
                }

                var count = 0
                while (cursor.moveToNext() && count < limit) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
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
                    count++
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaRepository", "Error loading tracks", e)
        }

        tracks
    }

    suspend fun getAllTracks(): List<Track> = withContext(Dispatchers.IO) {
        val allTracks = mutableListOf<Track>()
        var offset = 0
        val pageSize = 100

        while (true) {
            val page = loadTracks(offset, pageSize)
            if (page.isEmpty()) break
            allTracks.addAll(page)
            offset += pageSize
        }

        allTracks
    }

    suspend fun loadAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()

        try {
            val collection = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
            )

            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Audio.Albums.ALBUM} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
                val songsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(albumColumn) ?: "Unknown Album"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val trackCount = cursor.getInt(songsColumn)

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        id
                    )

                    albums.add(
                        Album(
                            id = id.toString(),
                            name = name,
                            artist = artist,
                            artworkUri = artworkUri,
                            trackCount = trackCount
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaRepository", "Error loading albums", e)
        }

        albums
    }

    suspend fun loadTracksByAlbum(albumId: String): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.ALBUM_ID} = ?"
            val selectionArgs = arrayOf(albumId)
            val sortOrder = "${MediaStore.Audio.Media.TRACK} ASC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albId = cursor.getLong(albumIdCol)
                    val duration = cursor.getLong(durationColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albId
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
            android.util.Log.e("MediaRepository", "Error loading tracks by album", e)
        }

        tracks
    }

    suspend fun loadArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<Artist>()

        try {
            val collection = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
            )

            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Audio.Artists.ARTIST} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
                val tracksColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
                val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val trackCount = cursor.getInt(tracksColumn)
                    val albumCount = cursor.getInt(albumsColumn)

                    artists.add(
                        Artist(
                            id = id.toString(),
                            name = name,
                            trackCount = trackCount,
                            albumCount = albumCount
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaRepository", "Error loading artists", e)
        }

        artists
    }

    suspend fun loadTracksByArtist(artistName: String): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.ARTIST} = ?"
            val selectionArgs = arrayOf(artistName)
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
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
            android.util.Log.e("MediaRepository", "Error loading tracks by artist", e)
        }

        tracks
    }

    suspend fun loadFolders(): List<Folder> = withContext(Dispatchers.IO) {
        val foldersMap = mutableMapOf<String, Int>()

        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media.DATA
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                null
            )?.use { cursor ->
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataColumn) ?: continue
                    val folderPath = path.substringBeforeLast("/")
                    foldersMap[folderPath] = (foldersMap[folderPath] ?: 0) + 1
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaRepository", "Error loading folders", e)
        }

        foldersMap.map { (path, count) ->
            Folder(
                path = path,
                name = path.substringAfterLast("/"),
                trackCount = count
            )
        }.sortedBy { it.name }
    }

    suspend fun loadTracksByFolder(folderPath: String): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("$folderPath/%")
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val filePath = cursor.getString(dataColumn) ?: continue
                    // Only include files directly in this folder, not subfolders
                    if (filePath.substringBeforeLast("/") != folderPath) continue

                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
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
            android.util.Log.e("MediaRepository", "Error loading tracks by folder", e)
        }

        tracks
    }
}

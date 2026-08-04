package com.musicplayer.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts a usable accent colour from album artwork.
 *
 * Returns null on any failure (missing art, hardware bitmaps, decode errors)
 * so callers can simply fall back to the brand colour.
 */
object ArtworkPalette {

    suspend fun accentFor(context: Context, uri: Uri?): Int? {
        if (uri == null) return null
        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .size(SAMPLE_SIZE)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                if (result !is SuccessResult) return@withContext null

                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    ?: return@withContext null
                val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                    bitmap.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    bitmap
                } ?: return@withContext null

                val palette = Palette.from(safeBitmap).clearFilters().generate()
                palette.getVibrantColor(0).takeIf { it != 0 }
                    ?: palette.getDominantColor(0).takeIf { it != 0 }
                    ?: palette.getMutedColor(0).takeIf { it != 0 }
            } catch (e: Exception) {
                android.util.Log.d("ArtworkPalette", "Palette extraction skipped: ${e.message}")
                null
            }
        }
    }

    private const val SAMPLE_SIZE = 160
}

package com.musicplayer.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicplayer.ui.theme.Radius
import com.musicplayer.ui.theme.SurfaceElevated
import com.musicplayer.ui.theme.SurfaceLight
import com.musicplayer.ui.theme.TextMuted

/**
 * Square artwork with a graceful placeholder.
 *
 * Everything that shows album art goes through here so corner radius,
 * placeholder styling and crossfade behave identically across the app.
 */
@Composable
fun Artwork(
    uri: Uri?,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    shape: Shape = Radius.artworkSmall,
    placeholderIcon: ImageVector = Icons.Rounded.MusicNote,
    placeholderIconSize: Dp = 24.dp
) {
    val sized = if (size != null) modifier.size(size) else modifier
    Box(
        modifier = sized
            .clip(shape)
            .background(
                Brush.linearGradient(listOf(SurfaceElevated, SurfaceLight))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = placeholderIcon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(placeholderIconSize)
        )
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/** Circular variant used for artists. */
@Composable
fun CircularArtwork(
    uri: Uri?,
    size: Dp,
    modifier: Modifier = Modifier,
    placeholderIcon: ImageVector = Icons.Rounded.MusicNote
) {
    Artwork(
        uri = uri,
        modifier = modifier,
        size = size,
        shape = CircleShape,
        placeholderIcon = placeholderIcon,
        placeholderIconSize = size / 2.5f
    )
}

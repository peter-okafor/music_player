package com.musicplayer.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.theme.Radius
import com.musicplayer.ui.theme.SurfaceLight
import com.musicplayer.ui.theme.TextMuted
import com.musicplayer.ui.theme.TextSecondary

/** Album tile for the 2-column grid. */
@Composable
fun AlbumTile(
    name: String,
    artist: String,
    artworkUri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(Radius.card)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Artwork(
            uri = artworkUri,
            shape = Radius.artworkMedium,
            placeholderIcon = Icons.Rounded.Album,
            placeholderIconSize = 40.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = artist,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Generic row with a leading circle/square, title, subtitle and chevron. */
@Composable
fun CollectionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    artworkUri: Uri? = null,
    fallbackIcon: ImageVector = Icons.Rounded.QueueMusic,
    circular: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(Radius.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (artworkUri != null || !circular) {
            Artwork(
                uri = artworkUri,
                size = 50.dp,
                shape = if (circular) CircleShape else Radius.artworkSmall,
                placeholderIcon = fallbackIcon,
                placeholderIconSize = 22.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(SurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Convenience wrappers so screens read clearly. */
@Composable
fun ArtistRow(name: String, subtitle: String, artworkUri: Uri?, onClick: () -> Unit) =
    CollectionRow(
        title = name,
        subtitle = subtitle,
        onClick = onClick,
        artworkUri = artworkUri,
        fallbackIcon = Icons.Rounded.Person,
        circular = true
    )

@Composable
fun FolderRow(name: String, subtitle: String, onClick: () -> Unit) =
    CollectionRow(
        title = name,
        subtitle = subtitle,
        onClick = onClick,
        fallbackIcon = Icons.Rounded.Folder
    )

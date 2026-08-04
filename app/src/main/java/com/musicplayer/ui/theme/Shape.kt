package com.musicplayer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Corner radii used directly by custom components. */
object Radius {
    val artworkSmall = RoundedCornerShape(8.dp)
    val artworkMedium = RoundedCornerShape(12.dp)
    val artworkLarge = RoundedCornerShape(20.dp)
    val card = RoundedCornerShape(16.dp)
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}

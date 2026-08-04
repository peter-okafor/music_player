package com.musicplayer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.musicplayer.data.prefs.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = Color.Black,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = TextPrimary,
    secondary = Accent,
    onSecondary = Color.Black,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Primary,
    outline = Border,
    outlineVariant = Divider,
    error = ErrorRed,
    onError = Color.Black,
    scrim = Scrim
)

private val LightColors = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PrimarySoft,
    onPrimaryContainer = LightTextPrimary,
    secondary = Accent,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = PrimaryDark,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = ErrorRed,
    onError = Color.White
)

/**
 * Accent colour sampled from the current artwork. Screens that want an
 * artwork-tinted surface (player, queue, mini player) read this; everything
 * else keeps the stable brand palette so the app doesn't strobe between
 * tracks.
 */
val LocalAccentColor = compositionLocalOf { Primary }

@Composable
fun MusicPlayerTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentColor: Color = Primary,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

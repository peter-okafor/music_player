package com.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicplayer.ui.MusicPlayerApp
import com.musicplayer.ui.theme.MusicPlayerTheme
import com.musicplayer.ui.theme.Primary
import com.musicplayer.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val themeMode by appViewModel.themeMode.collectAsState()
            val accentArgb by appViewModel.accentColor.collectAsState()

            MusicPlayerTheme(
                themeMode = themeMode,
                accentColor = accentArgb?.let { readableAccent(Color(it)) } ?: Primary
            ) {
                MusicPlayerApp(appViewModel = appViewModel)
            }
        }
    }
}

/**
 * Album art can yield colours that are unusable as an accent — near-black on
 * a dark background, or so pale that white text on top disappears. Nudge the
 * extracted colour into a workable band instead of discarding it.
 */
private fun readableAccent(color: Color): Color {
    val luminance = color.luminance()
    return when {
        luminance < 0.16f -> lerpColor(color, Color.White, 0.35f)
        luminance > 0.78f -> lerpColor(color, Color.Black, 0.25f)
        else -> color
    }
}

private fun lerpColor(from: Color, to: Color, fraction: Float): Color = Color(
    red = from.red + (to.red - from.red) * fraction,
    green = from.green + (to.green - from.green) * fraction,
    blue = from.blue + (to.blue - from.blue) * fraction,
    alpha = 1f
)

package com.musicplayer.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.musicplayer.ui.components.MiniPlayer
import com.musicplayer.ui.screens.AlbumsScreen
import com.musicplayer.ui.screens.ArtistsScreen
import com.musicplayer.ui.screens.CategoryDetailScreen
import com.musicplayer.ui.screens.FoldersScreen
import com.musicplayer.ui.screens.HomeScreen
import com.musicplayer.ui.screens.PlayerScreen
import com.musicplayer.ui.screens.PlaylistsScreen
import com.musicplayer.ui.viewmodel.CategoryType
import com.musicplayer.ui.viewmodel.HomeViewModel
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Player : Screen("player")
    data object Folders : Screen("folders")
    data object Playlists : Screen("playlists")
    data object Albums : Screen("albums")
    data object Artists : Screen("artists")
    data object CategoryDetail : Screen("category/{type}/{id}/{name}") {
        fun createRoute(type: String, id: String, name: String): String {
            val encodedName = URLEncoder.encode(name, "UTF-8")
            val encodedId = URLEncoder.encode(id, "UTF-8")
            return "category/$type/$encodedId/$encodedName"
        }
    }
}

@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()

    val playbackState by homeViewModel.playbackState.collectAsState()
    val currentTrack by homeViewModel.currentTrack.collectAsState()

    // Track current route to hide mini player on PlayerScreen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showMiniPlayer = currentTrack != null && currentRoute != Screen.Player.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToFolders = { navController.navigate(Screen.Folders.route) },
                        onNavigateToPlaylists = { navController.navigate(Screen.Playlists.route) },
                        onNavigateToAlbums = { navController.navigate(Screen.Albums.route) },
                        onNavigateToArtists = { navController.navigate(Screen.Artists.route) },
                        bottomPadding = if (showMiniPlayer) 70f else 0f
                    )
                }

                composable(
                    route = Screen.Player.route,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    PlayerScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Folders.route) {
                    FoldersScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onFolderClick = { path, name ->
                            navController.navigate(
                                Screen.CategoryDetail.createRoute("folder", path, name)
                            )
                        },
                        bottomPadding = if (showMiniPlayer) 70f else 0f
                    )
                }

                composable(Screen.Playlists.route) {
                    PlaylistsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onPlaylistClick = { id, name ->
                            navController.navigate(
                                Screen.CategoryDetail.createRoute("playlist", id, name)
                            )
                        },
                        bottomPadding = if (showMiniPlayer) 70f else 0f
                    )
                }

                composable(Screen.Albums.route) {
                    AlbumsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAlbumClick = { id, name ->
                            navController.navigate(
                                Screen.CategoryDetail.createRoute("album", id, name)
                            )
                        },
                        bottomPadding = if (showMiniPlayer) 70f else 0f
                    )
                }

                composable(Screen.Artists.route) {
                    ArtistsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onArtistClick = { id, name ->
                            navController.navigate(
                                Screen.CategoryDetail.createRoute("artist", id, name)
                            )
                        },
                        bottomPadding = if (showMiniPlayer) 70f else 0f
                    )
                }

                composable(
                    route = Screen.CategoryDetail.route,
                    arguments = listOf(
                        navArgument("type") { type = NavType.StringType },
                        navArgument("id") { type = NavType.StringType },
                        navArgument("name") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type") ?: "album"
                    val id = URLDecoder.decode(
                        backStackEntry.arguments?.getString("id") ?: "",
                        "UTF-8"
                    )
                    val name = URLDecoder.decode(
                        backStackEntry.arguments?.getString("name") ?: "",
                        "UTF-8"
                    )

                    val categoryType = when (type) {
                        "album" -> CategoryType.ALBUM
                        "artist" -> CategoryType.ARTIST
                        "folder" -> CategoryType.FOLDER
                        "playlist" -> CategoryType.PLAYLIST
                        else -> CategoryType.ALBUM
                    }

                    CategoryDetailScreen(
                        categoryType = categoryType,
                        categoryId = id,
                        categoryName = name,
                        onNavigateBack = { navController.popBackStack() },
                        bottomPadding = if (showMiniPlayer) 70f else 0f
                    )
                }
            }

            // Mini Player overlay at bottom (hidden on PlayerScreen)
            if (showMiniPlayer) {
                MiniPlayer(
                    track = currentTrack,
                    isPlaying = playbackState.isPlaying,
                    progress = if (playbackState.durationMs > 0) {
                        playbackState.currentTimeMs.toFloat() / playbackState.durationMs
                    } else 0f,
                    onTogglePlayPause = { homeViewModel.togglePlayPause() },
                    onNext = { homeViewModel.next() },
                    onExpand = { navController.navigate(Screen.Player.route) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

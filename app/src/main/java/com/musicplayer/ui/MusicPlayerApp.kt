package com.musicplayer.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.musicplayer.ui.components.EmptyState
import com.musicplayer.ui.components.MiniPlayer
import com.musicplayer.ui.screens.AlbumsScreen
import com.musicplayer.ui.screens.ArtistsScreen
import com.musicplayer.ui.screens.CategoryDetailScreen
import com.musicplayer.ui.screens.EqualizerScreen
import com.musicplayer.ui.screens.FoldersScreen
import com.musicplayer.ui.screens.HomeScreen
import com.musicplayer.ui.screens.PlayerScreen
import com.musicplayer.ui.screens.PlaylistsScreen
import com.musicplayer.ui.screens.QueueScreen
import com.musicplayer.ui.screens.SettingsScreen
import com.musicplayer.ui.screens.SongsScreen
import com.musicplayer.ui.viewmodel.AppViewModel
import com.musicplayer.ui.viewmodel.CategoryType
import com.musicplayer.ui.viewmodel.PermissionStatus
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Songs : Screen("songs")
    data object Albums : Screen("albums")
    data object Artists : Screen("artists")
    data object Playlists : Screen("playlists")
    data object Folders : Screen("folders")
    data object Favorites : Screen("favorites")
    data object Queue : Screen("queue")
    data object Equalizer : Screen("equalizer")
    data object Settings : Screen("settings")
    data object Player : Screen("player")

    data object CategoryDetail : Screen("category/{type}/{id}/{name}") {
        fun createRoute(type: String, id: String, name: String): String {
            val encodedId = URLEncoder.encode(id, "UTF-8")
            val encodedName = URLEncoder.encode(name, "UTF-8")
            return "category/$type/$encodedId/$encodedName"
        }
    }
}

private data class BottomTab(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(Screen.Home, "Home", Icons.Rounded.Home),
    BottomTab(Screen.Songs, "Songs", Icons.Rounded.MusicNote),
    BottomTab(Screen.Albums, "Albums", Icons.Rounded.Album),
    BottomTab(Screen.Artists, "Artists", Icons.Rounded.Person),
    BottomTab(Screen.Playlists, "Playlists", Icons.Rounded.QueueMusic)
)

/** Routes that keep the bottom navigation and the mini player visible. */
private val topLevelRoutes = bottomTabs.map { it.screen.route }.toSet()

@Composable
fun MusicPlayerApp(appViewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionStatus by appViewModel.permissionStatus.collectAsState()
    val currentTrack by appViewModel.currentTrack.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isTopLevel = currentRoute in topLevelRoutes
    val isPlayerRoute = currentRoute == Screen.Player.route
    val showMiniPlayer = currentTrack != null && !isPlayerRoute

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> appViewModel.onPermissionResult(granted) }

    LaunchedEffect(permissionStatus) {
        if (permissionStatus == PermissionStatus.UNDETERMINED) {
            permissionLauncher.launch(appViewModel.requiredPermission)
        }
    }

    val showMessage: (String) -> Unit = { text ->
        scope.launch { snackbarHostState.showSnackbar(text) }
    }

    if (permissionStatus == PermissionStatus.DENIED) {
        PermissionScreen(onGrant = { permissionLauncher.launch(appViewModel.requiredPermission) })
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // NavigationBar consumes the system inset itself; without it we
            // add the padding here so no screen sits under the gesture bar.
            Column(
                modifier = if (isTopLevel) Modifier else Modifier.navigationBarsPadding()
            ) {
                if (showMiniPlayer) {
                    MiniPlayerBar(
                        appViewModel = appViewModel,
                        onExpand = { navController.navigate(Screen.Player.route) }
                    )
                }
                if (isTopLevel) {
                    BottomNavigation(navController = navController, currentRoute = currentRoute)
                }
            }
        }
    ) { scaffoldPadding ->
        val innerPadding = PaddingValues(bottom = scaffoldPadding.calculateBottomPadding())

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = scaffoldPadding.calculateTopPadding())
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenFavorites = { navController.navigate(Screen.Favorites.route) },
                    onOpenFolders = { navController.navigate(Screen.Folders.route) },
                    onOpenQueue = { navController.navigate(Screen.Queue.route) },
                    onOpenEqualizer = { navController.navigate(Screen.Equalizer.route) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenSongs = { navController.navigateToTab(Screen.Songs.route) },
                    contentPadding = innerPadding
                )
            }

            composable(Screen.Songs.route) {
                SongsScreen(
                    contentPadding = innerPadding,
                    onMessage = showMessage
                )
            }

            composable(Screen.Favorites.route) {
                SongsScreen(
                    favoritesOnly = true,
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = innerPadding,
                    onMessage = showMessage
                )
            }

            composable(Screen.Albums.route) {
                AlbumsScreen(
                    onAlbumClick = { id, name ->
                        navController.navigate(Screen.CategoryDetail.createRoute("album", id, name))
                    },
                    contentPadding = innerPadding
                )
            }

            composable(Screen.Artists.route) {
                ArtistsScreen(
                    onArtistClick = { id, name ->
                        navController.navigate(Screen.CategoryDetail.createRoute("artist", id, name))
                    },
                    contentPadding = innerPadding
                )
            }

            composable(Screen.Playlists.route) {
                PlaylistsScreen(
                    onPlaylistClick = { id, name ->
                        navController.navigate(
                            Screen.CategoryDetail.createRoute("playlist", id, name)
                        )
                    },
                    contentPadding = innerPadding,
                    onMessage = showMessage
                )
            }

            composable(Screen.Folders.route) {
                FoldersScreen(
                    onFolderClick = { path, name ->
                        navController.navigate(
                            Screen.CategoryDetail.createRoute("folder", path, name)
                        )
                    },
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = innerPadding
                )
            }

            composable(Screen.Queue.route) {
                QueueScreen(
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = innerPadding
                )
            }

            composable(Screen.Equalizer.route) {
                EqualizerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = innerPadding
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenEqualizer = { navController.navigate(Screen.Equalizer.route) },
                    contentPadding = innerPadding,
                    onMessage = showMessage
                )
            }

            composable(
                route = Screen.Player.route,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(280)
                    ) + fadeIn(tween(220))
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(280)
                    ) + fadeOut(tween(220))
                }
            ) {
                PlayerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenQueue = { navController.navigate(Screen.Queue.route) },
                    onOpenEqualizer = { navController.navigate(Screen.Equalizer.route) }
                )
            }

            composable(
                route = Screen.CategoryDetail.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { entry ->
                val type = entry.arguments?.getString("type") ?: "album"
                val id = URLDecoder.decode(entry.arguments?.getString("id").orEmpty(), "UTF-8")
                val name = URLDecoder.decode(entry.arguments?.getString("name").orEmpty(), "UTF-8")

                CategoryDetailScreen(
                    categoryType = when (type) {
                        "artist" -> CategoryType.ARTIST
                        "folder" -> CategoryType.FOLDER
                        "playlist" -> CategoryType.PLAYLIST
                        else -> CategoryType.ALBUM
                    },
                    categoryId = id,
                    categoryName = name,
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = innerPadding,
                    onMessage = showMessage
                )
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    appViewModel: AppViewModel,
    onExpand: () -> Unit
) {
    val currentTrack by appViewModel.currentTrack.collectAsState()
    val playbackState by appViewModel.playbackState.collectAsState()
    val favorites by appViewModel.favorites.collectAsState()

    MiniPlayer(
        track = currentTrack,
        isPlaying = playbackState.isPlaying,
        progress = playbackState.progress,
        isFavorite = currentTrack?.id?.let { favorites.contains(it) } == true,
        onTogglePlayPause = appViewModel::togglePlayPause,
        onNext = appViewModel::next,
        onPrevious = appViewModel::previous,
        onToggleFavorite = { currentTrack?.id?.let(appViewModel::toggleFavorite) },
        onExpand = onExpand
    )
}

@Composable
private fun BottomNavigation(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomTabs.forEach { tab ->
            val selected = currentRoute == tab.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTab(tab.screen.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                )
            )
        }
    }
}

/** Single-top tab navigation that keeps each tab's own back stack shallow. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun PermissionScreen(onGrant: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState(
                title = "Music access needed",
                subtitle = "Grant access to your audio files so the library can be loaded. " +
                    "Nothing leaves your device.",
                actionLabel = "Grant access",
                onAction = onGrant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

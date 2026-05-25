package com.mixelte.melodorium

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.common.util.concurrent.MoreExecutors
import com.mixelte.melodorium.player.PlaybackService
import com.mixelte.melodorium.ui.common.BottomNavigationBar
import com.mixelte.melodorium.ui.common.MiniPlayer
import com.mixelte.melodorium.ui.features.player.PlayerRoute
import com.mixelte.melodorium.ui.features.player.PlayerViewModel
import com.mixelte.melodorium.ui.features.settings.SettingsRoute
import com.mixelte.melodorium.ui.features.settings.SettingsViewModel
import com.mixelte.melodorium.ui.features.wave_settings.WaveSettingsRoute
import com.mixelte.melodorium.ui.features.wave_settings.WaveSettingsViewModel
import com.mixelte.melodorium.ui.theme.AppTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object WaveSettings : Screen("wave_settings")
    object Settings : Screen("settings")

    object FullscreenPlayer : Screen("fullscreen_player")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MelodoriumApplication

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    app.settingsRepository.musicDatafile, app.settingsRepository.musicRootFolder
                ) { file, root ->
                    if (file != null && root != null) Pair(file, root) else null
                }.collect { pair ->
                    pair?.let { (file, root) ->
                        app.musicRepository.loadMusicData(datafileUri = file, rootFolderUri = root)
                    }
                }
            }
        }

        setContent {
            val playerViewModel: PlayerViewModel = viewModel {
                PlayerViewModel(app.playbackManager, app.musicFilterManager, app.musicRepository)
            }
            val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()

            val settingsViewModel: SettingsViewModel = viewModel {
                SettingsViewModel(app.settingsRepository, app.musicRepository)
            }

            val waveSettingsViewModel: WaveSettingsViewModel = viewModel {
                WaveSettingsViewModel(app.musicFilterManager)
            }

            val startDestination = intent.getStringExtra("navigate_to") ?: Screen.WaveSettings.route
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val isPlayerOpen = currentRoute == Screen.FullscreenPlayer.route

            AppTheme {
                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = !isPlayerOpen,
                            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)),
                            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400))
                        ) {
                            Column {
                                MiniPlayer(
                                    state = playerState,
                                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                                    onPreviousClick = { playerViewModel.prevTrack() },
                                    onNextClick = { playerViewModel.nextTrack() },
                                    onPlayerClick = { navController.navigate(Screen.FullscreenPlayer.route) })

                                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
                            }
                        }
                    }) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable(Screen.WaveSettings.route) {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                WaveSettingsRoute(waveSettingsViewModel)
                            }
                        }
                        composable(Screen.Library.route) {
                            Box(modifier = Modifier.padding(innerPadding)) {
//                            LibraryScreen()
                            }
                        }
                        composable(Screen.Settings.route) {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                SettingsRoute(settingsViewModel)
                            }
                        }
                        composable(
                            route = Screen.FullscreenPlayer.route,
                            enterTransition = {
                                slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(400)
                                )
                            },
                            exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) },
                            popExitTransition = {
                                slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(400)
                                )
                            }
                        ) {
                            PlayerRoute(
                                playerViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                val app = application as MelodoriumApplication
                val controller = controllerFuture.get()
                app.playbackManager.setMediaController(controller)
            }, MoreExecutors.directExecutor()
        )
    }
}
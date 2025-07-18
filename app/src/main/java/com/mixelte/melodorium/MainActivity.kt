package com.mixelte.melodorium

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.common.util.concurrent.MoreExecutors
import com.mixelte.melodorium.data.MusicData
import com.mixelte.melodorium.pages.MusicList
import com.mixelte.melodorium.pages.Playlist
import com.mixelte.melodorium.pages.Settings
import com.mixelte.melodorium.player.PlaybackService
import com.mixelte.melodorium.player.Player
import com.mixelte.melodorium.ui.theme.MelodoriumTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class Routes {
    @Serializable
    object MusicList : Routes()
    @Serializable
    object Playlist : Routes()
    @Serializable
    object Settings : Routes()

    companion object {
        fun serialize(route: Routes): String =
            Json.encodeToString(route)

        fun deserialize(serialized: String): Routes? =
            runCatching { Json.decodeFromString<Routes>(serialized) }.getOrNull()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val navigate = { route: Any -> navController.navigate(route) }
            MusicData.MusicDataLoader()
            val route = Routes.deserialize(intent.getStringExtra("navigate_to") ?: "")
            MelodoriumTheme {
                enableEdgeToEdge(
                    navigationBarStyle = SystemBarStyle.auto(
                        NavigationBarDefaults.containerColor.toArgb(),
                        NavigationBarDefaults.containerColor.toArgb(),
                    )
                )
                NavHost(navController, route ?: Routes.MusicList) {
                    composable<Routes.MusicList> {
                        Layout(Routes.MusicList, navigate) { MusicList() }
                    }
                    composable<Routes.Playlist> {
                        Layout(Routes.Playlist, navigate) { Playlist() }
                    }
                    composable<Routes.Settings> {
                        Layout(Routes.Settings, navigate) { Settings() }
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
                Player.setMediaController(controllerFuture.get())
            },
            MoreExecutors.directExecutor()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Layout(route: Any, navigate: (route: Any) -> Unit, page: @Composable () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        when (route) {
                            Routes.MusicList -> "Music"
                            Routes.Playlist -> "Playlist"
                            Routes.Settings -> "Settings"
                            else -> ""
                        }
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                data class Route(val route: Any, val name: String, val iconFilled: ImageVector, val iconOutlined: ImageVector)
                listOf(
                    Route(Routes.MusicList, "MusicList", Icons.Filled.Home, Icons.Outlined.Home),
                    Route(Routes.Playlist, "Playlist", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
                    Route(Routes.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
                ).forEach {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (route == it.route) it.iconFilled else it.iconOutlined,
                                contentDescription = it.name
                            )
                        },
//                        label = { Text(it.name) },
                        selected = route == it.route,
                        onClick = { navigate(it.route) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        ) {
            page()
        }
    }
}

package com.mixelte.melodorium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mixelte.melodorium.ui.theme.MelodoriumTheme
import kotlinx.serialization.Serializable

class Routes {
    @Serializable
    object MusicList;
    @Serializable
    object Playlist;
    @Serializable
    object Settings;
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.light(
                    MaterialTheme.colorScheme.primary.toArgb(),
                    MaterialTheme.colorScheme.inversePrimary.toArgb()
                )
            )
            val navController = rememberNavController()
            val navigate = {route: Any -> navController.navigate(route)}
            MusicData.MusicDataLoader()
            MelodoriumTheme {
                NavHost(navController, Routes.MusicList) {
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
                        })
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
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            page()
        }
    }
}

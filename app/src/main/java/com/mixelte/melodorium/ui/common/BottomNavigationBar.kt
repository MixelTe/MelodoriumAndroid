package com.mixelte.melodorium.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mixelte.melodorium.Quadruple
import com.mixelte.melodorium.Screen

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        val items = listOf(
            Quadruple(Screen.Library.route, "Библиотека", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
            Quadruple(Screen.WaveSettings.route, "Волна", Icons.Filled.MusicNote, Icons.Outlined.MusicNote),
            Quadruple(Screen.Settings.route, "Настройки", Icons.Filled.Settings, Icons.Outlined.Settings)
        )

        items.forEach { (route, label, iconFilled, iconOutlined) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                label = { Text(label) },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == route) iconFilled else iconOutlined,
                        contentDescription = label
                    )
                }
            )
        }
    }
}
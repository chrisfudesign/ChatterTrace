package com.washington.chattertrace

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.washington.chattertrace.DataLogic.DataManager
import com.washington.chattertrace.RecordingLogic.RecordingManager
import com.washington.chattertrace.home.HomeScreen
import com.washington.chattertrace.recordings.RecordingsScreen
import com.washington.chattertrace.recordings.RecordingDetailScreen
import com.washington.chattertrace.reflections.ReflectionDetailScreen

/*
 * Navigation Router
 */
@Composable
fun Navigation(navController: NavHostController, recordingManager: RecordingManager?, dataManager: DataManager?) {
    NavHost(navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            HomeScreen(recordingManager = recordingManager, dataManager = dataManager)
        }
        composable(NavigationItem.Recordings.route) {
            RecordingsScreen(navController)
        }
        composable(NavigationItem.Reflections.route) {
            ReflectionsScreen(navController)
        }
        composable("recordingDetail/{date}") {
            val date = it.arguments?.getString("date")
            if (date != null) {
                RecordingDetailScreen(date)
            }
        }
        composable("reflectionDetail/{date}") {
            val date = it.arguments?.getString("date")
            if (date != null) {
                ReflectionDetailScreen(navController, date)
            }
        }
    }
}

/*
 * Bottom Navigation Bar
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Recordings,
        NavigationItem.Reflections
    )
    BottomNavigation(
        backgroundColor = colorResource(id = R.color.light_surface),
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                label = { Text(text = item.title) },
                selectedContentColor = Color.Black,
                alwaysShowLabel = true,
                selected = false,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
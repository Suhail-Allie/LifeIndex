package com.lifeindex.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.ui.screens.CalendarScreen
import com.lifeindex.app.ui.screens.DocumentsScreen
import com.lifeindex.app.ui.screens.HomeScreen
import com.lifeindex.app.ui.screens.LoginScreen
import com.lifeindex.app.ui.screens.RegisterScreen
import com.lifeindex.app.ui.screens.SettingsScreen
import com.lifeindex.app.ui.screens.TrackerDetailScreen
import com.lifeindex.app.ui.screens.TrackersScreen

@Composable
fun LifeIndexNavGraph() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val sessionManager = SessionManager(context)

    val startDestination =
        if (sessionManager.getAccessToken() != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }

    val screens = listOf(
        Screen.Home,
        Screen.Trackers,
        Screen.Calendar,
        Screen.Documents,
        Screen.Settings
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute !in listOf(
        Screen.Login.route,
        Screen.Register.route,
        "tracker/{trackerId}"
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {

                    screens.forEach { screen ->

                        val icon = when (screen) {
                            Screen.Home -> Icons.Filled.Home
                            Screen.Trackers -> Icons.Filled.List
                            Screen.Calendar -> Icons.Filled.CalendarMonth
                            Screen.Documents -> Icons.Filled.Description
                            Screen.Settings -> Icons.Filled.Settings

                            // Login and Register are not included in `screens`,
                            // but the sealed class requires exhaustive handling.
                            else -> Icons.Filled.Home
                        }

                        NavigationBarItem(
                            selected = currentRoute == screen.route,

                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }

                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },

                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.route
                                )
                            },

                            label = {
                                Text(
                                    screen.route.replaceFirstChar {
                                        it.uppercase()
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Login.route) {

                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    },

                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {

                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) {
                                inclusive = true
                            }
                        }
                    },

                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen()
            }

            composable(Screen.Trackers.route) {

                TrackersScreen(
                    onTrackerClick = { trackerId ->
                        navController.navigate("tracker/$trackerId")
                    }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen()
            }

            composable(Screen.Documents.route) {
                DocumentsScreen()
            }

            composable(Screen.Settings.route) {

                SettingsScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("tracker/{trackerId}") { backStackEntry ->

                val trackerId =
                    backStackEntry.arguments?.getString("trackerId") ?: ""

                TrackerDetailScreen(
                    trackerId = trackerId,

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
package com.lifeindex.app.navigation

sealed class Screen(val route: String) {

    data object Login : Screen("login")

    data object Register : Screen("register")

    data object Home : Screen("home")

    data object Trackers : Screen("trackers")

    data object Calendar : Screen("calendar")

    data object Documents : Screen("documents")

    data object Settings : Screen("settings")
}
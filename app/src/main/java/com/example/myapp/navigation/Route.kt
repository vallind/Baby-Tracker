package com.example.myapp.navigation

sealed class Route(val route: String) {
    // 底部导航 Tab
    data object Home : Route("home")
    data object Records : Route("records")
    data object Stats : Route("stats")
    data object Messages : Route("messages")
    data object Profile : Route("profile")

    // 功能页面（无底部导航）
    data object Feeding : Route("feeding")
    data object AddFeeding : Route("add_feeding")
    data object Sleep : Route("sleep")
    data object AddSleep : Route("add_sleep")
    data object Growth : Route("growth")
    data object AddGrowth : Route("add_growth")
    data object Vaccine : Route("vaccine")
    data object AddVaccine : Route("add_vaccine")
    data object Health : Route("health")
    data object Settings : Route("settings")
    data object About : Route("about")

    companion object {
        val bottomNavRoutes = listOf(Home, Records, Stats, Messages, Profile)
        fun isTopLevel(route: String) = bottomNavRoutes.any { it.route == route }
    }
}

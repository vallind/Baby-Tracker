package com.example.myapp.navigation

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Feeding : Route("feeding")
    data object AddFeeding : Route("add_feeding")
    data object Sleep : Route("sleep")
    data object AddSleep : Route("add_sleep")
    data object Growth : Route("growth")
    data object AddGrowth : Route("add_growth")
    data object Vaccine : Route("vaccine")
    data object AddVaccine : Route("add_vaccine")
    data object Health : Route("health")
    data object Stats : Route("stats")
    data object Settings : Route("settings")
    data object About : Route("about")
}

package com.babytracker.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.babytracker.ui.home.HomeScreen
import com.babytracker.ui.feeding.FeedingListScreen
import com.babytracker.ui.timeline.TimelineScreen
import com.babytracker.ui.sleep.SleepListScreen
import com.babytracker.ui.growth.GrowthScreen
import com.babytracker.ui.vaccination.VaccinationListScreen
import com.babytracker.ui.health.HealthScreen
import com.babytracker.ui.diaper.DiaperListScreen
import com.babytracker.ui.stats.StatsScreen
import com.babytracker.ui.settings.SettingsScreen
import com.babytracker.ui.settings.BabyManagementScreen
import com.babytracker.ui.settings.BackupScreen
import com.babytracker.ui.message.MessageScreen
import com.babytracker.ui.development.DevelopmentAssessmentScreen
import com.babytracker.ui.reminder.ReminderScreen

sealed class Screen(val route: String) {
    object Home : Screen("/")
    object Timeline : Screen("/timeline")
    object Feeding : Screen("/feeding")
    object Sleep : Screen("/sleep")
    object Growth : Screen("/growth")
    object Vaccination : Screen("/vaccination")
    object Health : Screen("/health")
    object Diaper : Screen("/diaper")
    object Stats : Screen("/stats")
    object Settings : Screen("/settings")
    object BabyManagement : Screen("/settings/babies")
    object Backup : Screen("/settings/backup")
    object Message : Screen("/message")
    object DevelopmentAssessment : Screen("/development_assessment")
    object Reminder : Screen("/reminder")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Timeline.route) { TimelineScreen(navController) }
        composable(Screen.Feeding.route) { FeedingListScreen(navController) }
        composable(Screen.Sleep.route) { SleepListScreen(navController) }
        composable(Screen.Growth.route) { GrowthScreen(navController) }
        composable(Screen.Vaccination.route) { VaccinationListScreen(navController) }
        composable(Screen.Health.route) { HealthScreen(navController) }
        composable(Screen.Diaper.route) { DiaperListScreen(navController) }
        composable(Screen.Stats.route) { StatsScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.BabyManagement.route) { BabyManagementScreen(navController) }
        composable(Screen.Backup.route) { BackupScreen(navController) }
        composable(Screen.Message.route) { MessageScreen(navController) }
        composable(Screen.DevelopmentAssessment.route) { DevelopmentAssessmentScreen(navController) }
        composable(Screen.Reminder.route) { ReminderScreen(navController) }
    }
}

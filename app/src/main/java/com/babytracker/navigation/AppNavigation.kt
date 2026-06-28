package com.babytracker.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.babytracker.feature.home.HomeScreen
import com.babytracker.feature.feeding.FeedingListScreen
import com.babytracker.feature.timeline.TimelineScreen
import com.babytracker.feature.sleep.SleepListScreen
import com.babytracker.feature.growth.GrowthScreen
import com.babytracker.feature.vaccination.VaccinationListScreen
import com.babytracker.feature.health.HealthScreen
import com.babytracker.feature.diaper.DiaperListScreen
import com.babytracker.feature.stats.StatsScreen
import com.babytracker.feature.settings.SettingsScreen
import com.babytracker.feature.settings.BabyProfileScreen
import com.babytracker.feature.settings.BabyManagementScreen
import com.babytracker.feature.settings.BackupScreen
import com.babytracker.feature.message.MessageScreen
import com.babytracker.feature.development.DevelopmentAssessmentScreen
import com.babytracker.feature.reminder.ReminderScreen
import com.babytracker.feature.auth.LoginScreen
import com.babytracker.feature.family.FamilyPage

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
    object BabyProfile : Screen("/settings/baby/profile")
    object Backup : Screen("/settings/backup")
    object Family : Screen("/settings/family")
    object Message : Screen("/message")
    object DevelopmentAssessment : Screen("/development_assessment")
    object Reminder : Screen("/reminder")
    object Login : Screen("/login")
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
        composable(Screen.BabyProfile.route) { BabyProfileScreen(navController) }
        composable(Screen.Backup.route) { BackupScreen(navController) }
        composable(Screen.Family.route) { FamilyPage(navController) }
        composable(Screen.Message.route) { MessageScreen(navController) }
        composable(Screen.DevelopmentAssessment.route) { DevelopmentAssessmentScreen(navController) }
        composable(Screen.Reminder.route) { ReminderScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
    }
}

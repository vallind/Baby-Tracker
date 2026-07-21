package com.babytracker.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.*
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
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
import com.babytracker.feature.settings.LogViewerScreen
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
    object LogViewer : Screen("/settings/logviewer")
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
        instantComposable(Screen.Home.route) { HomeScreen(navController) }
        instantComposable(Screen.Timeline.route) { TimelineScreen(navController) }
        instantComposable(Screen.Feeding.route) { FeedingListScreen(navController) }
        instantComposable(Screen.Sleep.route) { SleepListScreen(navController) }
        instantComposable(Screen.Growth.route) { GrowthScreen(navController) }
        instantComposable(Screen.Vaccination.route) { VaccinationListScreen(navController) }
        instantComposable(Screen.Health.route) { HealthScreen(navController) }
        instantComposable(Screen.Diaper.route) { DiaperListScreen(navController) }
        instantComposable(Screen.Stats.route) { StatsScreen(navController) }
        instantComposable(Screen.Settings.route) { SettingsScreen(navController) }
        instantComposable(Screen.BabyManagement.route) { BabyManagementScreen(navController) }
        instantComposable(Screen.BabyProfile.route) { BabyProfileScreen(navController) }
        instantComposable(Screen.Backup.route) { BackupScreen(navController) }
        instantComposable(Screen.LogViewer.route) { LogViewerScreen(navController) }
        instantComposable(Screen.Family.route) { FamilyPage(navController) }
        instantComposable(Screen.Message.route) { MessageScreen(navController) }
        instantComposable(Screen.DevelopmentAssessment.route) { DevelopmentAssessmentScreen(navController) }
        instantComposable(Screen.Reminder.route) { ReminderScreen(navController) }
        instantComposable(Screen.Login.route) { LoginScreen(navController) }
    }
}

/**
 * 无动画 composable 封装：切页面立即显示，不等待过渡动画。
 * 解决默认 fade 动画在低端设备或复杂页面上的卡顿问题。
 */
private fun NavGraphBuilder.instantComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { null },
        exitTransition = { null },
        popEnterTransition = { null },
        popExitTransition = { null },
        content = content,
    )
}

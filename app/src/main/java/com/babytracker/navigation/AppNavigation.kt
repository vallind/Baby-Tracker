package com.babytracker.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.babytracker.feature.settings.SyncSettingsScreen
import com.babytracker.feature.settings.PreferenceSettingsScreen
import com.babytracker.feature.settings.DataSettingsScreen
import com.babytracker.feature.settings.SupportSettingsScreen
import com.babytracker.feature.message.MessageScreen
import com.babytracker.feature.development.DevelopmentAssessmentScreen
import com.babytracker.feature.reminder.ReminderScreen
import com.babytracker.feature.auth.LoginScreen
import com.babytracker.feature.family.FamilyPage
import com.babytracker.feature.ai.AiChatScreen
import com.babytracker.feature.ai.AiSettingsScreen

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
    object PreferenceSettings : Screen("/settings/preferences")
    object DataSettings : Screen("/settings/data")
    object SupportSettings : Screen("/settings/support")
    object BabyManagement : Screen("/settings/babies")
    object BabyProfile : Screen("/settings/baby/profile")
    object Backup : Screen("/settings/backup")
    object LogViewer : Screen("/settings/logviewer")
    object SyncSettings : Screen("/settings/sync")
    object Family : Screen("/settings/family")
    object Message : Screen("/message")
    object DevelopmentAssessment : Screen("/development_assessment")
    object Reminder : Screen("/reminder")
    object Login : Screen("/login")
    object AiAssistant : Screen("/ai-assistant")
    object AiSettings : Screen("/ai-assistant/settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Home.route) {
        fadeComposable(Screen.Home.route) { HomeScreen(navController) }
        fadeComposable(Screen.Timeline.route) { TimelineScreen(navController) }
        fadeComposable(Screen.Feeding.route) { FeedingListScreen(navController) }
        fadeComposable(Screen.Sleep.route) { SleepListScreen(navController) }
        fadeComposable(Screen.Growth.route) { GrowthScreen(navController) }
        fadeComposable(Screen.Vaccination.route) { VaccinationListScreen(navController) }
        fadeComposable(Screen.Health.route) { HealthScreen(navController) }
        fadeComposable(Screen.Diaper.route) { DiaperListScreen(navController) }
        fadeComposable(Screen.Stats.route) { StatsScreen(navController) }
        fadeComposable(Screen.Settings.route) { SettingsScreen(navController) }
        slideComposable(Screen.PreferenceSettings.route) { PreferenceSettingsScreen(navController) }
        slideComposable(Screen.DataSettings.route) { DataSettingsScreen(navController) }
        slideComposable(Screen.SupportSettings.route) { SupportSettingsScreen(navController) }
        slideComposable(Screen.BabyManagement.route) { BabyManagementScreen(navController) }
        slideComposable(Screen.BabyProfile.route) { BabyProfileScreen(navController) }
        slideComposable(Screen.Backup.route) { BackupScreen(navController) }
        slideComposable(Screen.LogViewer.route) { LogViewerScreen(navController) }
        slideComposable(Screen.SyncSettings.route) { SyncSettingsScreen(navController) }
        slideComposable(Screen.Family.route) { FamilyPage(navController) }
        fadeComposable(Screen.Message.route) { MessageScreen(navController) }
        slideComposable(Screen.DevelopmentAssessment.route) { DevelopmentAssessmentScreen(navController) }
        slideComposable(Screen.Reminder.route) { ReminderScreen(navController) }
        slideComposable(Screen.Login.route) { LoginScreen(navController) }
        slideComposable(Screen.AiAssistant.route) { AiChatScreen(navController) }
        slideComposable(Screen.AiSettings.route) { AiSettingsScreen(navController) }
    }
}

/**
 * 淡入淡出转场 — 主 tab 级页面切换（Apple 克制风格，150ms）。
 */
private fun NavGraphBuilder.fadeComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { fadeIn(tween(150)) },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(150)) },
        popExitTransition = { fadeOut(tween(150)) },
        content = content,
    )
}

/**
 * 滑动转场 — 层级 push 页面（淡入 + 轻微右滑入，pop 反向）。
 */
private fun NavGraphBuilder.slideComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { fadeIn(tween(150)) + slideInHorizontally(tween(150)) { it / 16 } },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(150)) + slideInHorizontally(tween(150)) { -it / 16 } },
        popExitTransition = { fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { it / 16 } },
        content = content,
    )
}

package com.babytracker.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.*
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import com.babytracker.feature.home.HomeRoute
import com.babytracker.feature.feeding.FeedingListScreen
import com.babytracker.feature.timeline.TimelineRoute
import com.babytracker.feature.sleep.SleepListScreen
import com.babytracker.feature.growth.GrowthScreen
import com.babytracker.feature.vaccination.VaccinationListScreen
import com.babytracker.feature.health.HealthScreen
import com.babytracker.feature.diaper.DiaperListScreen
import com.babytracker.feature.stats.StatsRoute
import com.babytracker.feature.settings.SettingsScreen
import com.babytracker.feature.settings.BabyProfileScreen
import com.babytracker.feature.settings.BabyManagementScreen
import com.babytracker.feature.settings.BackupScreen
import com.babytracker.feature.settings.LogViewerScreen
import com.babytracker.feature.settings.SyncSettingsRoute
import com.babytracker.feature.settings.PreferenceSettingsScreen
import com.babytracker.feature.settings.DataSettingsScreen
import com.babytracker.feature.settings.SupportSettingsScreen
import com.babytracker.feature.message.MessageRoute
import com.babytracker.feature.development.DevelopmentAssessmentRoute
import com.babytracker.feature.reminder.ReminderRoute
import com.babytracker.feature.auth.LoginRoute
import com.babytracker.feature.family.FamilyRoute
import com.babytracker.feature.ai.AiChatRoute
import com.babytracker.feature.ai.AiSettingsRoute

/**
 * 类型安全路由：每个页面一个 @Serializable data object，
 * 由 Navigation 2.8+ 在编译期生成路由，杜绝手写字符串拼错。
 */
@Serializable data object Home
@Serializable data object Timeline
@Serializable data object Feeding
@Serializable data object Sleep
@Serializable data object Growth
@Serializable data object Vaccination
@Serializable data object Health
@Serializable data object Diaper
@Serializable data object Stats
@Serializable data object Settings
@Serializable data object PreferenceSettings
@Serializable data object DataSettings
@Serializable data object SupportSettings
@Serializable data object BabyManagement
@Serializable data object BabyProfile
@Serializable data object Backup
@Serializable data object LogViewer
@Serializable data object SyncSettings
@Serializable data object Family
@Serializable data object Message
@Serializable data object DevelopmentAssessment
@Serializable data object Reminder
@Serializable data object Login
@Serializable data object AiAssistant
@Serializable data object AiSettings

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Home) {
        instantComposable<Home> { HomeRoute(navController) }
        instantComposable<Timeline> { TimelineRoute(navController) }
        instantComposable<Feeding> { FeedingListScreen(navController) }
        instantComposable<Sleep> { SleepListScreen(navController) }
        instantComposable<Growth> { GrowthScreen(navController) }
        instantComposable<Vaccination> { VaccinationListScreen(navController) }
        instantComposable<Health> { HealthScreen(navController) }
        instantComposable<Diaper> { DiaperListScreen(navController) }
        instantComposable<Stats> { StatsRoute(navController) }
        instantComposable<Settings> { SettingsScreen(navController) }
        instantComposable<PreferenceSettings> { PreferenceSettingsScreen(navController) }
        instantComposable<DataSettings> { DataSettingsScreen(navController) }
        instantComposable<SupportSettings> { SupportSettingsScreen(navController) }
        instantComposable<BabyManagement> { BabyManagementScreen(navController) }
        instantComposable<BabyProfile> { BabyProfileScreen(navController) }
        instantComposable<Backup> { BackupScreen(navController) }
        instantComposable<LogViewer> { LogViewerScreen(navController) }
        instantComposable<SyncSettings> { SyncSettingsRoute(navController) }
        instantComposable<Family> { FamilyRoute(navController) }
        instantComposable<Message> { MessageRoute(navController) }
        instantComposable<DevelopmentAssessment> { DevelopmentAssessmentRoute(navController) }
        instantComposable<Reminder> { ReminderRoute(navController) }
        instantComposable<Login> { LoginRoute(navController) }
        instantComposable<AiAssistant> { AiChatRoute(navController) }
        instantComposable<AiSettings> { AiSettingsRoute(navController) }
    }
}

/**
 * 无动画 composable 封装：切页面立即显示，不等待过渡动画。
 * 解决默认 fade 动画在低端设备或复杂页面上的卡顿问题。
 */
private inline fun <reified T : Any> NavGraphBuilder.instantComposable(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T>(
        enterTransition = { null },
        exitTransition = { null },
        popEnterTransition = { null },
        popExitTransition = { null },
        content = content,
    )
}

/**
 * 以根级 Tab 语义导航：弹栈到起始页（保存状态）+ 单顶复用。
 * 首页宫格与底部导航共用，避免导航选项四处复制。
 */
internal fun <T : Any> NavController.navigateToRoot(route: T) {
    val startDestinationId = graph.findStartDestination().id
    navigate(route) {
        popUpTo(startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

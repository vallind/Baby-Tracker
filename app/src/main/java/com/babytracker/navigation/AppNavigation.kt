package com.babytracker.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.babytracker.core.ui.shouldShowSplitPane
import com.babytracker.feature.ai.AiChatScreen
import com.babytracker.feature.ai.AiSettingsScreen
import com.babytracker.feature.auth.LoginScreen
import com.babytracker.feature.development.DevelopmentAssessmentScreen
import com.babytracker.feature.diaper.DiaperListScreen
import com.babytracker.feature.family.FamilyPage
import com.babytracker.feature.feeding.FeedingListScreen
import com.babytracker.feature.growth.GrowthScreen
import com.babytracker.feature.health.HealthScreen
import com.babytracker.feature.home.HomeScreen
import com.babytracker.feature.message.MessageScreen
import com.babytracker.feature.reminder.ReminderScreen
import com.babytracker.feature.settings.BabyManagementScreen
import com.babytracker.feature.settings.BabyProfileScreen
import com.babytracker.feature.settings.BackupScreen
import com.babytracker.feature.settings.DataSettingsScreen
import com.babytracker.feature.settings.LogViewerScreen
import com.babytracker.feature.settings.PreferenceSettingsScreen
import com.babytracker.feature.settings.SettingsScreen
import com.babytracker.feature.settings.SupportSettingsScreen
import com.babytracker.feature.settings.SyncSettingsScreen
import com.babytracker.feature.sleep.SleepListScreen
import com.babytracker.feature.stats.StatsScreen
import com.babytracker.feature.timeline.TimelineScreen
import com.babytracker.feature.vaccination.VaccinationListScreen
import com.babytracker.i18n.AppStrings
import io.elyon.kmp.basic.NavigationRail
import io.elyon.kmp.basic.NavigationRailItem
import io.elyon.kmp.nav.core.NavCornerClipMode
import io.elyon.kmp.nav.core.NavDisplay
import io.elyon.kmp.nav.core.NavDisplayEffects
import io.elyon.kmp.nav.core.rememberNavBackStack
import io.elyon.kmp.nav.core.rememberNavSystemCornerRadius
import io.elyon.kmp.nav.transition.NavTransitions
import io.elyon.kmp.theme.ElyonTheme

private data class RailItem(
    val route: Route,
    val icon: ImageVector,
    val label: String,
)

/**
 * elyon-nav 应用导航根。
 *
 * 路由清单与 AppRouteGraph / RouteGraphTest 保持一致；
 * NavDisplayEffects 启用圆角裁剪、压暗与页面背景兜底，配合底部导航毛玻璃。
 */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack<Route>(Route.Home)
    val navigator = remember(backStack) { Navigator(backStack) }
    AppBackHandler(navigator)

    val railItems = remember {
        listOf(
            RailItem(Route.Home, Icons.Outlined.Home, AppStrings.home),
            RailItem(Route.Timeline, Icons.AutoMirrored.Outlined.List, AppStrings.records),
            RailItem(Route.Stats, Icons.Outlined.BarChart, AppStrings.stats),
            RailItem(Route.Message, Icons.AutoMirrored.Outlined.Message, AppStrings.messages),
            RailItem(Route.Settings, Icons.Outlined.Person, AppStrings.profile),
        )
    }

    val navCornerRadius = rememberNavSystemCornerRadius()
    val backdropColor = ElyonTheme.colorScheme.surface
    val effects = remember(navCornerRadius, backdropColor) {
        NavDisplayEffects(
            enableCornerClip = true,
            cornerClipRadius = navCornerRadius,
            cornerClipMode = NavCornerClipMode.Leading,
            dimAmount = 0.5f,
            blockInputDuringTransition = false,
            backdropColor = backdropColor,
        )
    }

    val isWideScreen = shouldShowSplitPane()
    Row(Modifier.fillMaxSize()) {
        if (isWideScreen) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                color = ElyonTheme.colorScheme.surface,
            ) {
                railItems.forEach { item ->
                    NavigationRailItem(
                        selected = navigator.current() == item.route,
                        onClick = { navigator.switchTab(item.route) },
                        icon = item.icon,
                        label = item.label,
                    )
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxHeight()) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.pop() },
                transition = NavTransitions.ElyonDefault,
                effects = effects,
            ) {
                entry<Route.Home> { HomeScreen(navigator) }
                entry<Route.Timeline> { TimelineScreen(navigator) }
                entry<Route.Feeding> { FeedingListScreen(navigator) }
                entry<Route.Sleep> { SleepListScreen(navigator) }
                entry<Route.Growth> { GrowthScreen(navigator) }
                entry<Route.Vaccination> { VaccinationListScreen(navigator) }
                entry<Route.Health> { HealthScreen(navigator) }
                entry<Route.Diaper> { DiaperListScreen(navigator) }
                entry<Route.Stats> { StatsScreen(navigator) }
                entry<Route.Settings> { SettingsScreen(navigator) }
                entry<Route.PreferenceSettings> { PreferenceSettingsScreen(navigator) }
                entry<Route.DataSettings> { DataSettingsScreen(navigator) }
                entry<Route.SupportSettings> { SupportSettingsScreen(navigator) }
                entry<Route.BabyManagement> { BabyManagementScreen(navigator) }
                entry<Route.BabyProfile> { BabyProfileScreen(navigator) }
                entry<Route.Backup> { BackupScreen(navigator) }
                entry<Route.LogViewer> { LogViewerScreen(navigator) }
                entry<Route.SyncSettings> { SyncSettingsScreen(navigator) }
                entry<Route.Family> { FamilyPage(navigator) }
                entry<Route.Message> { MessageScreen(navigator) }
                entry<Route.DevelopmentAssessment> { DevelopmentAssessmentScreen(navigator) }
                entry<Route.Reminder> { ReminderScreen(navigator) }
                entry<Route.Login> { LoginScreen(navigator) }
                entry<Route.AiAssistant> { AiChatScreen(navigator) }
                entry<Route.AiSettings> { AiSettingsScreen(navigator) }
            }
        }
    }
}

/**
 * 系统返回手势/按键桥接：栈深度 > 1 时启用返回，交给 Navigator.pop()。
 */
@Composable
private fun AppBackHandler(navigator: Navigator) {
    val canGoBack = navigator.backStack.size > 1
    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = canGoBack,
        onBackCompleted = { navigator.pop() },
    )
}

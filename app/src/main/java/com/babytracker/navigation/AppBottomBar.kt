package com.babytracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.babytracker.core.data.repository.MessageRepository
import com.babytracker.designsystem.components.navigation.AppNavigationBar
import com.babytracker.designsystem.components.navigation.AppNavigationItem
import com.babytracker.designsystem.i18n.AppStrings
import org.koin.compose.koinInject

/**
 * App 层底部导航壳 —— 业务依赖（消息未读数、5 个根级 Tab 路由）只允许出现在这里，
 * 视觉与交互全部委托给纯 DS 组件 [AppNavigationBar]。
 */
@Composable
fun AppBottomBar(navController: NavController) {
    val messageRepo: MessageRepository = koinInject()
    val unreadCount by messageRepo.watchUnreadCount().collectAsState(initial = 0)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        AppNavigationItem(label = AppStrings.home, icon = Icons.Outlined.Home, badgeCount = 0),
        AppNavigationItem(label = AppStrings.records, icon = Icons.AutoMirrored.Outlined.List, badgeCount = 0),
        AppNavigationItem(label = AppStrings.stats, icon = Icons.Outlined.BarChart, badgeCount = 0),
        AppNavigationItem(label = AppStrings.messages, icon = Icons.AutoMirrored.Outlined.Message, badgeCount = unreadCount),
        AppNavigationItem(label = AppStrings.profile, icon = Icons.Outlined.Person, badgeCount = 0),
    )

    val selectedIndex = currentDestination?.let { dest ->
        when {
            dest.hierarchy.any { it.hasRoute<Home>() } -> 0
            dest.hierarchy.any { it.hasRoute<Timeline>() } -> 1
            dest.hierarchy.any { it.hasRoute<Stats>() } -> 2
            dest.hierarchy.any { it.hasRoute<Message>() } -> 3
            dest.hierarchy.any { it.hasRoute<Settings>() } -> 4
            else -> 0
        }
    } ?: 0

    AppNavigationBar(
        items = items,
        selectedIndex = selectedIndex,
        onItemClick = { index ->
            val route = when (index) {
                0 -> Home
                1 -> Timeline
                2 -> Stats
                3 -> Message
                else -> Settings
            }
            navController.navigateToRoot(route)
        },
    )
}
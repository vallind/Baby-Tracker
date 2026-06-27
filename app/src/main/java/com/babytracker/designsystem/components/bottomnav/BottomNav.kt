package com.babytracker.designsystem.components.bottomnav

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import com.babytracker.designsystem.theme.LocalAppTypography
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.babytracker.designsystem.components.bottomnav.BottomBarDefaults
import com.babytracker.designsystem.theme.LocalThemeColors
import com.babytracker.core.data.repository.MessageRepository
import com.babytracker.navigation.Screen
import org.koin.compose.koinInject

/**
 * 共享底部导航栏 — 5 Tab（首页 / 记录 / 统计 / 消息 / 我的）
 *
 * 修正历史 bug：原先 "消息" Tab 错误地路由到 Vaccination，
 * 现在正确路由到 `Screen.Message.route`。
 *
 * 用法：
 *   Scaffold(bottomBar = { BottomNavBar(navController) }) { ... }
 */
@Composable
fun BottomNavBar(navController: NavController) {
    val c = LocalThemeColors.current
    val messageRepo: MessageRepository = koinInject()
    val unreadCount by messageRepo.watchUnreadCount().collectAsState(initial = 0)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = c.card,
        contentColor = c.textPrimary,
        tonalElevation = 0.dp,
        modifier = Modifier.height(BottomBarDefaults.height()),
    ) {
        val tabs = listOf(
            BottomTab("首页", Icons.Outlined.Home, Screen.Home.route, badgeCount = 0),
            BottomTab("记录", Icons.AutoMirrored.Outlined.List, Screen.Timeline.route, badgeCount = 0),
            BottomTab("统计", Icons.Outlined.BarChart, Screen.Stats.route, badgeCount = 0),
            BottomTab("消息", Icons.AutoMirrored.Outlined.Message, Screen.Message.route, badgeCount = unreadCount),
            BottomTab("我的", Icons.Outlined.Person, Screen.Settings.route, badgeCount = 0),
        )
        tabs.forEach { tab ->
            NavigationBarItem(
                icon = {
                    if (tab.badgeCount > 0) {
                        BadgedBox(badge = { Badge { Text(if (tab.badgeCount > 99) "99+" else tab.badgeCount.toString()) } }) {
                            Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(BottomBarDefaults.iconSize()))
                        }
                    } else {
                        Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(BottomBarDefaults.iconSize()))
                    }
                },
                label = { Text(tab.label, style = LocalAppTypography.current.labelSmall) },
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = c.primary,
                    selectedTextColor = c.primary,
                    unselectedIconColor = c.textSecondary,
                    unselectedTextColor = c.textSecondary,
                    indicatorColor = c.primaryLight,
                ),
            )
        }
    }
}

private data class BottomTab(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0,
)

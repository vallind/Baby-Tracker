package com.babytracker.designsystem.components.bottomnav

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.babytracker.designsystem.components.bottomnav.BottomBarDefaults
import com.babytracker.core.data.repository.MessageRepository
import com.babytracker.navigation.Home
import com.babytracker.navigation.Message
import com.babytracker.navigation.Settings
import com.babytracker.navigation.Stats
import com.babytracker.navigation.Timeline
import com.babytracker.navigation.navigateToRoot
import org.koin.compose.koinInject

/**
 * 共享底部导航栏 — 5 Tab（首页 / 记录 / 统计 / 消息 / 我的）
 *
 * 修正历史 bug：原先 "消息" Tab 错误地路由到 Vaccination，
 * 现在正确路由到类型安全路由 `Message`。
 *
 * 用法：
 *   Scaffold(bottomBar = { BottomNavBar(navController) }) { ... }
 */
@Composable
fun BottomNavBar(navController: NavController) {
    val messageRepo: MessageRepository = koinInject()
    val unreadCount by messageRepo.watchUnreadCount().collectAsState(initial = 0)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = BottomBarDefaults.containerColor(),
        contentColor = BottomBarDefaults.contentColor(),
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(BottomBarDefaults.height())
            .navigationBarsPadding(),
    ) {
        val tabs = listOf(
            BottomTab(
                label = "首页",
                icon = Icons.Outlined.Home,
                badgeCount = 0,
                isSelected = { it?.hierarchy?.any { d -> d.hasRoute<Home>() } == true },
                onSelected = { it.navigateToRoot(Home) },
            ),
            BottomTab(
                label = "记录",
                icon = Icons.AutoMirrored.Outlined.List,
                badgeCount = 0,
                isSelected = { it?.hierarchy?.any { d -> d.hasRoute<Timeline>() } == true },
                onSelected = { it.navigateToRoot(Timeline) },
            ),
            BottomTab(
                label = "统计",
                icon = Icons.Outlined.BarChart,
                badgeCount = 0,
                isSelected = { it?.hierarchy?.any { d -> d.hasRoute<Stats>() } == true },
                onSelected = { it.navigateToRoot(Stats) },
            ),
            BottomTab(
                label = "消息",
                icon = Icons.AutoMirrored.Outlined.Message,
                badgeCount = unreadCount,
                isSelected = { it?.hierarchy?.any { d -> d.hasRoute<Message>() } == true },
                onSelected = { it.navigateToRoot(Message) },
            ),
            BottomTab(
                label = "我的",
                icon = Icons.Outlined.Person,
                badgeCount = 0,
                isSelected = { it?.hierarchy?.any { d -> d.hasRoute<Settings>() } == true },
                onSelected = { it.navigateToRoot(Settings) },
            ),
        )
        tabs.forEach { tab ->
            NavigationBarItem(
                icon = {
                    if (tab.badgeCount > 0) {
                        BadgedBox(badge = { Badge { Text(if (tab.badgeCount > 99) "99+" else tab.badgeCount.toString()) } }) {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(BottomBarDefaults.iconSize()))
                        }
                    } else {
                        Icon(tab.icon, contentDescription = null, modifier = Modifier.size(BottomBarDefaults.iconSize()))
                    }
                },
                label = { Text(tab.label, style = LocalAppTypography.current.labelSmall) },
                selected = tab.isSelected(currentDestination),
                onClick = { tab.onSelected(navController) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BottomBarDefaults.selectedColor(),
                    selectedTextColor = BottomBarDefaults.selectedColor(),
                    unselectedIconColor = BottomBarDefaults.unselectedColor(),
                    unselectedTextColor = BottomBarDefaults.unselectedColor(),
                    indicatorColor = BottomBarDefaults.indicatorColor(),
                ),
            )
        }
    }
}

private data class BottomTab(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
    val isSelected: (NavDestination?) -> Boolean,
    val onSelected: (NavController) -> Unit,
)

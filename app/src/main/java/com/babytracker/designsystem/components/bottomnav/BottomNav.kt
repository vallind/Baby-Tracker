package com.babytracker.designsystem.components.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.babytracker.core.data.repository.MessageRepository
import com.babytracker.designsystem.components.bottomnav.BottomBarDefaults
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.dialog.AppDialog
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.fab.FabDefaults
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.navigation.Screen
import org.koin.compose.koinInject

/**
 * 共享底部导航栏 — 4 Tab（首页 / 记录 / 消息 / 我的）+ 中央 FAB
 *
 * 用法：
 *   Scaffold(bottomBar = { BottomNavBar(navController) }) { ... }
 */
@Composable
fun BottomNavBar(navController: NavController) {
    val messageRepo: MessageRepository = koinInject()
    val unreadCount by messageRepo.watchUnreadCount().collectAsState(initial = 0)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showTypePicker by remember { mutableStateOf(false) }
    val fabSize = FabDefaults.size()

    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar(
            containerColor = BottomBarDefaults.containerColor(),
            contentColor = BottomBarDefaults.contentColor(),
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(BottomBarDefaults.height())
                .navigationBarsPadding(),
        ) {
            val tabs = listOf(
                BottomTab("首页", Icons.Outlined.Home, Screen.Home.route, badgeCount = 0),
                BottomTab("记录", Icons.AutoMirrored.Outlined.List, Screen.Timeline.route, badgeCount = 0),
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
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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

        AppFAB(
            icon = Icons.Default.Add,
            onClick = { showTypePicker = true },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -(fabSize / 2)),
        )
    }

    if (showTypePicker) {
        AppDialog(
            show = true,
            title = "选择记录类型",
            onDismiss = { showTypePicker = false },
            content = {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val types = listOf(
                        Screen.Feeding to "🤱 喂养",
                        Screen.Sleep to "😴 睡眠",
                        Screen.Diaper to "🧷 尿布",
                        Screen.Growth to "📏 生长",
                        Screen.Vaccination to "💉 疫苗",
                        Screen.Health to "❤️ 健康",
                    )
                    types.forEach { (screen, label) ->
                        AppTextButton(
                            onClick = {
                                showTypePicker = false
                                navController.navigate(screen.route)
                            },
                            label = label,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        )
                    }
                }
            },
        )
    }
}

private data class BottomTab(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0,
)

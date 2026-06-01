package com.example.myapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapp.ui.feeding.AddFeedingScreen
import com.example.myapp.ui.feeding.FeedingEvent
import com.example.myapp.ui.feeding.FeedingScreen
import com.example.myapp.ui.growth.AddGrowthScreen
import com.example.myapp.ui.growth.GrowthEvent
import com.example.myapp.ui.growth.GrowthScreen
import com.example.myapp.ui.health.HealthScreen
import com.example.myapp.ui.home.HomeScreen
import com.example.myapp.ui.messages.MessagesScreen
import com.example.myapp.ui.profile.ProfileScreen
import com.example.myapp.ui.records.RecordsScreen
import com.example.myapp.ui.sleep.AddSleepScreen
import com.example.myapp.ui.sleep.SleepEvent
import com.example.myapp.ui.sleep.SleepScreen
import com.example.myapp.ui.vaccine.AddVaccineScreen
import com.example.myapp.ui.vaccine.VaccineEvent
import com.example.myapp.ui.about.AboutScreen
import com.example.myapp.ui.settings.SettingsScreen
import com.example.myapp.ui.stats.StatsScreen
import com.example.myapp.ui.vaccine.VaccineScreen
import org.koin.androidx.compose.koinViewModel

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route
)

@Composable
fun AppNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomItems = listOf(
        BottomNavItem("首页", Icons.Default.Home, Route.Home),
        BottomNavItem("记录", Icons.Default.List, Route.Records),
        BottomNavItem("统计", Icons.Default.TrendingUp, Route.Stats),
        BottomNavItem("消息", Icons.Default.Notifications, Route.Messages),
        BottomNavItem("我的", Icons.Default.Person, Route.Profile)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route.route,
                        onClick = {
                            if (currentRoute != item.route.route) {
                                navController.navigate(item.route.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Home.route) {
                HomeScreen(
                    viewModel = koinViewModel(),
                    onNavigate = { navController.navigate(it) }
                )
            }
            composable(Route.Records.route) {
                RecordsScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Route.Stats.route) {
                StatsScreen(viewModel = koinViewModel())
            }
            composable(Route.Messages.route) {
                MessagesScreen()
            }
            composable(Route.Profile.route) {
                ProfileScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Route.Feeding.route) {
                FeedingScreen(viewModel = koinViewModel(), onAddClick = { navController.navigate(Route.AddFeeding.route) })
            }
            composable(Route.AddFeeding.route) {
                val vm = koinViewModel<com.example.myapp.ui.feeding.FeedingViewModel>()
                AddFeedingScreen(onSave = { type, amount, unit, note ->
                    vm.onEvent(FeedingEvent.Add(type, amount, unit, note))
                    navController.popBackStack()
                }, onBack = { navController.popBackStack() })
            }
            composable(Route.Sleep.route) {
                SleepScreen(viewModel = koinViewModel(), onAddClick = { navController.navigate(Route.AddSleep.route) })
            }
            composable(Route.AddSleep.route) {
                val vm = koinViewModel<com.example.myapp.ui.sleep.SleepViewModel>()
                AddSleepScreen(onSave = { startTime, endTime, type ->
                    vm.onEvent(SleepEvent.Add(startTime, endTime, type))
                    navController.popBackStack()
                }, onBack = { navController.popBackStack() })
            }
            composable(Route.Growth.route) {
                GrowthScreen(viewModel = koinViewModel(), onAddClick = { navController.navigate(Route.AddGrowth.route) })
            }
            composable(Route.AddGrowth.route) {
                val vm = koinViewModel<com.example.myapp.ui.growth.GrowthViewModel>()
                AddGrowthScreen(onSave = { height, weight, head, date ->
                    vm.onEvent(GrowthEvent.Add(height, weight, head, date))
                    navController.popBackStack()
                }, onBack = { navController.popBackStack() })
            }
            composable(Route.Vaccine.route) {
                VaccineScreen(viewModel = koinViewModel(), onAddClick = { navController.navigate(Route.AddVaccine.route) })
            }
            composable(Route.AddVaccine.route) {
                val vm = koinViewModel<com.example.myapp.ui.vaccine.VaccineViewModel>()
                AddVaccineScreen(onSave = { name, dose, plannedDate ->
                    vm.onEvent(VaccineEvent.Add(name, dose, plannedDate))
                    navController.popBackStack()
                }, onBack = { navController.popBackStack() })
            }
            composable(Route.Health.route) { HealthScreen(viewModel = koinViewModel()) }
            composable(Route.Settings.route) { SettingsScreen(viewModel = koinViewModel()) }
            composable(Route.About.route) { AboutScreen() }
        }
    }
}

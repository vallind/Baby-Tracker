package com.example.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapp.ui.feeding.AddFeedingScreen
import com.example.myapp.ui.feeding.FeedingEvent
import com.example.myapp.ui.feeding.FeedingScreen
import com.example.myapp.ui.growth.AddGrowthScreen
import com.example.myapp.ui.growth.GrowthEvent
import com.example.myapp.ui.growth.GrowthScreen
import com.example.myapp.ui.home.HomeScreen
import com.example.myapp.ui.sleep.AddSleepScreen
import com.example.myapp.ui.sleep.SleepEvent
import com.example.myapp.ui.sleep.SleepScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Route.Home.route) {
        composable(Route.Home.route) {
            HomeScreen(
                viewModel = koinViewModel(),
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Route.Feeding.route) {
            FeedingScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate(Route.AddFeeding.route) }
            )
        }
        composable(Route.AddFeeding.route) {
            val feedingViewModel = koinViewModel<com.example.myapp.ui.feeding.FeedingViewModel>()
            AddFeedingScreen(
                onSave = { type, amount, unit, note ->
                    feedingViewModel.onEvent(FeedingEvent.Add(type, amount, unit, note))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Sleep.route) {
            SleepScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate(Route.AddSleep.route) }
            )
        }
        composable(Route.AddSleep.route) {
            val sleepViewModel = koinViewModel<com.example.myapp.ui.sleep.SleepViewModel>()
            AddSleepScreen(
                onSave = { startTime, endTime, type ->
                    sleepViewModel.onEvent(SleepEvent.Add(startTime, endTime, type))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Growth.route) {
            GrowthScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate(Route.AddGrowth.route) }
            )
        }
        composable(Route.AddGrowth.route) {
            val growthViewModel = koinViewModel<com.example.myapp.ui.growth.GrowthViewModel>()
            AddGrowthScreen(
                onSave = { height, weight, head, date ->
                    growthViewModel.onEvent(GrowthEvent.Add(height, weight, head, date))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

package com.babytracker.feature.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.babytracker.core.util.BabyController
import com.babytracker.navigation.AppBottomBar
import com.babytracker.navigation.Growth
import com.babytracker.navigation.Health
import com.babytracker.navigation.Vaccination
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * 时间线路由 — 只负责 DI 装配与导航映射，UI 渲染委托给 [TimelineScreen]。
 */
@Composable
fun TimelineRoute(navController: NavController) {
    val viewModel: TimelineViewModel = koinViewModel()
    val babyCtrl: BabyController = koinInject()
    val currentBabyId = babyCtrl.currentBabyId

    LaunchedEffect(currentBabyId) {
        if (currentBabyId != 0) viewModel.load(currentBabyId)
    }

    TimelineScreen(
        viewModel = viewModel,
        currentBabyId = currentBabyId,
        onOpenGrowth = { navController.navigate(Growth) },
        onOpenVaccination = { navController.navigate(Vaccination) },
        onOpenHealth = { navController.navigate(Health) },
        bottomBar = { AppBottomBar(navController) },
    )
}
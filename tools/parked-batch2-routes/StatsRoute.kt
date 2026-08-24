package com.babytracker.feature.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.babytracker.core.util.BabyController
import com.babytracker.navigation.AppBottomBar
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * 统计分析路由 — 只负责 DI 装配与导航映射，UI 渲染委托给 [StatsScreen]。
 */
@Composable
fun StatsRoute(navController: NavController) {
    val viewModel: StatsViewModel = koinViewModel()
    val babyCtrl: BabyController = koinInject()
    val currentBabyId = babyCtrl.currentBabyId

    LaunchedEffect(currentBabyId) {
        if (currentBabyId != 0) viewModel.loadData(currentBabyId)
    }

    StatsScreen(
        viewModel = viewModel,
        currentBabyId = currentBabyId,
        bottomBar = { AppBottomBar(navController) },
    )
}
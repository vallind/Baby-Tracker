package com.babytracker.feature.development

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.util.BabyController
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * 发育评估路由 — 只负责 DI 装配与导航映射。
 *
 * - koinViewModel 装配 [DevelopmentAssessmentViewModel] 并注入 Screen；
 * - babyRepo.watchAll() 流在路由层收集宝宝列表，babyCtrl 提供当前宝宝 id，
 *   均作为普通状态参数传入 Screen；
 * - 返回动作映射为 onBack 回调。
 */
@Composable
fun DevelopmentAssessmentRoute(navController: NavController) {
    val viewModel: DevelopmentAssessmentViewModel = koinViewModel()
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())

    DevelopmentAssessmentScreen(
        viewModel = viewModel,
        babies = babies,
        babyId = babyCtrl.currentBabyId,
        onBack = { navController.popBackStack() },
    )
}
package com.babytracker.feature.development

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 发育评估路由（组合根）— 只做 DI 装配与导航映射。
 *
 * - koinViewModel 装配 [DevelopmentAssessmentViewModel]，state / baby 作为状态传给 Screen；
 * - 当前宝宝解析、数据加载全部在 ViewModel（init 内监听当前宝宝自动加载），
 *   本路由不 koinInject 业务对象、不解析宝宝、不承载加载逻辑；
 * - 返回动作映射为 onBack 回调。
 */
@Composable
fun DevelopmentAssessmentRoute(navController: NavController) {
    val viewModel: DevelopmentAssessmentViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val baby by viewModel.baby.collectAsState()

    DevelopmentAssessmentScreen(
        state = state,
        baby = baby,
        onBack = { navController.popBackStack() },
        onSubmitAssessment = viewModel::insert,
    )
}
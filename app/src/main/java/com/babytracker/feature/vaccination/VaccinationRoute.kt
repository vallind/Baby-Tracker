package com.babytracker.feature.vaccination

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 疫苗接种记录路由（组合根）— 只做 DI 装配与导航映射。
 *
 * - koinViewModel 装配 [VaccinationViewModel]，state / baby 作为状态传给 Screen；
 * - 当前宝宝解析、数据加载、删除/恢复/保存/生成计划全部在 ViewModel，
 *   本路由不 koinInject 业务对象、不解析宝宝、不承载加载逻辑；
 * - 返回动作映射为 onBack 回调；表单保存、删除、撤销、生成计划包成回调。
 */
@Composable
fun VaccinationRoute(navController: NavController) {
    val viewModel: VaccinationViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val baby by viewModel.baby.collectAsState()

    VaccinationListScreen(
        state = state,
        baby = baby,
        onBack = { navController.popBackStack() },
        onDelete = viewModel::delete,
        onRestore = viewModel::restore,
        onSave = viewModel::save,
        onGenerateSchedule = viewModel::generateSchedule,
    )
}
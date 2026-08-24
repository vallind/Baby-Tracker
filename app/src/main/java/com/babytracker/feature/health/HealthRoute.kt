package com.babytracker.feature.health

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.Vaccination
import org.koin.androidx.compose.koinViewModel

/**
 * 健康档案路由（组合根）— 只做 DI 装配与导航映射。
 *
 * - koinViewModel 装配 [HealthViewModel]，state 作为状态传给 Screen；
 * - 当前宝宝解析、数据加载、删除/恢复/保存全部在 ViewModel，
 *   本路由不 koinInject 业务对象、不解析宝宝、不承载加载逻辑；
 * - 返回动作映射为 onBack；疫苗卡跳转映射为 onOpenVaccination；
 *   删除/撤销/保存包成回调。
 */
@Composable
fun HealthRoute(navController: NavController) {
    val viewModel: HealthViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    HealthScreen(
        state = state,
        onBack = { navController.popBackStack() },
        onOpenVaccination = { navController.navigate(Vaccination) },
        onDelete = viewModel::delete,
        onRestore = viewModel::restore,
        onSave = viewModel::save,
    )
}
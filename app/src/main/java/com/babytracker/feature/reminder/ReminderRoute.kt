package com.babytracker.feature.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 提醒中心路由（组合根）— 只做 DI 装配与导航映射。
 *
 * - koinViewModel 装配 [ReminderViewModel]，state / baby 作为状态传给 Screen；
 * - 当前宝宝解析、数据加载、删除/恢复写操作全部在 ViewModel，
 *   本路由不 koinInject 业务对象、不解析宝宝、不承载加载逻辑；
 * - 返回动作映射为 onBack 回调；删除/撤销包成 onDelete / onRestore 回调。
 */
@Composable
fun ReminderRoute(navController: NavController) {
    val viewModel: ReminderViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val baby by viewModel.baby.collectAsState()

    ReminderScreen(
        state = state,
        baby = baby,
        onBack = { navController.popBackStack() },
        onMarkDone = viewModel::markDone,
        onToggleEnabled = viewModel::setEnabled,
        onDelete = viewModel::delete,
        onRestore = viewModel::restore,
    )
}
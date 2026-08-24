package com.babytracker.feature.diaper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.AppBottomBar
import org.koin.androidx.compose.koinViewModel

/**
 * 尿布记录路由（组合根）— 只做 DI 装配与导航映射。
 *
 * - koinViewModel 装配 [DiaperViewModel]，state 作为状态传给 Screen；
 * - 当前宝宝解析、数据加载、删除/撤销/保存写操作全部在 ViewModel，
 *   本路由不 koinInject 业务对象、不解析宝宝、不承载加载逻辑；
 * - 返回动作映射为 onBack 回调；底部导航槽位在此注入。
 */
@Composable
fun DiaperRoute(navController: NavController) {
    val viewModel: DiaperViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    DiaperListScreen(
        state = state,
        bottomBar = { AppBottomBar(navController) },
        onBack = { navController.popBackStack() },
        onDelete = viewModel::delete,
        onUndoDelete = viewModel::undoDelete,
        onSave = viewModel::save,
    )
}
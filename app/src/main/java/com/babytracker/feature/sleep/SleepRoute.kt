package com.babytracker.feature.sleep

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.AppBottomBar
import org.koin.androidx.compose.koinViewModel

/**
 * 睡眠记录路由（组合根）— 只做 DI 装配与导航映射；
 * 当前宝宝解析与记录加载在 ViewModel；日期筛选等 UI 态留在 Screen。
 */
@Composable
fun SleepRoute(navController: NavController) {
    val viewModel: SleepViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val babyId by viewModel.babyId.collectAsState()

    SleepListScreen(
        state = state,
        babyId = babyId,
        bottomBar = { AppBottomBar(navController) },
        onBack = { navController.popBackStack() },
        onAdd = viewModel::add,
        onUpdate = viewModel::update,
        onDelete = viewModel::delete,
        onUndoDelete = viewModel::undoDelete,
    )
}
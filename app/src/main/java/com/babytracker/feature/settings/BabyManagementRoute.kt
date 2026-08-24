package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 宝宝管理路由（组合根）— 只做 DI 装配与导航映射；
 * 宝宝增删改与切换在 BabyManagementViewModel；切换后弹回上一页在此映射。
 */
@Composable
fun BabyManagementRoute(navController: NavController) {
    val viewModel: BabyManagementViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    BabyManagementScreen(
        state = state,
        onBack = { navController.popBackStack() },
        onSelectBaby = { id ->
            viewModel.selectBaby(id)
            navController.popBackStack()
        },
        onAddOrUpdate = viewModel::addOrUpdate,
        onDelete = viewModel::delete,
        onRestore = viewModel::restore,
    )
}
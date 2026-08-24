package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.BabyManagement
import org.koin.androidx.compose.koinViewModel

/**
 * 宝宝资料路由（组合根）— 只做 DI 装配与导航映射；
 * 当前宝宝解析与生长数据加载在 ViewModel（flatMapLatest 串接）。
 */
@Composable
fun BabyProfileRoute(navController: NavController) {
    val viewModel: BabyProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    BabyProfileScreen(
        state = state,
        onBack = { navController.popBackStack() },
        onOpenBabyManagement = { navController.navigate(BabyManagement) },
        onSaveBaby = viewModel::updateBaby,
    )
}
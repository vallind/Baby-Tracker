package com.babytracker.feature.family

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 家庭共享页路由：负责创建 ViewModel 并把导航动作转换为回调注入页面，
 * 页面本身不感知导航细节。
 */
@Composable
fun FamilyRoute(navController: NavController) {
    val viewModel: FamilyViewModel = koinViewModel()
    FamilyPage(
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
    )
}
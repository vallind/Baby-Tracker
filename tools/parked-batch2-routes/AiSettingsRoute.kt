package com.babytracker.feature.ai

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * AI 设置路由 — 只负责 DI 装配与导航映射，UI 渲染委托给 [AiSettingsScreen]。
 */
@Composable
fun AiSettingsRoute(navController: NavController) {
    val viewModel: AiSettingsViewModel = koinViewModel()
    AiSettingsScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
    )
}
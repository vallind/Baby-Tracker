package com.babytracker.feature.ai

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.babytracker.core.util.BabyController
import com.babytracker.navigation.AiSettings
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * AI 聊天路由 — 只负责 DI 装配、当前宝宝上下文与导航映射，UI 渲染委托给 [AiChatScreen]。
 */
@Composable
fun AiChatRoute(navController: NavController) {
    val viewModel: AiChatViewModel = koinViewModel()
    val babyCtrl: BabyController = koinInject()
    AiChatScreen(
        viewModel = viewModel,
        currentBabyId = babyCtrl.currentBabyId,
        onBack = { navController.popBackStack() },
        onOpenAiSettings = { navController.navigate(AiSettings) },
    )
}
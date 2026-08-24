package com.babytracker.feature.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.AiSettings
import org.koin.androidx.compose.koinViewModel

/**
 * AI 聊天路由 — 组合根：只做 DI 装配、状态收集与导航映射，不承载业务逻辑。
 * 当前宝宝上下文由 [AiChatViewModel] 内部监听 BabyController 维护，Route 无需解析。
 */
@Composable
fun AiChatRoute(navController: NavController) {
    val viewModel: AiChatViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    AiChatScreen(
        state = state,
        onBack = { navController.popBackStack() },
        onOpenAiSettings = { navController.navigate(AiSettings) },
        onSelectModel = viewModel::selectModel,
        onRefreshConfig = viewModel::refreshConfig,
        onPrepareAnalysis = viewModel::prepareAnalysis,
        onSelectAnalysisPeriod = viewModel::selectAnalysisPeriod,
        onUpdateInput = viewModel::updateInput,
        onSend = viewModel::send,
        onStop = viewModel::stop,
        onRetry = viewModel::retry,
        onRemoveAnalysisContext = viewModel::removeAnalysisContext,
        onRegenerateLastAnswer = viewModel::regenerateLastAnswer,
        onEditLastQuestion = viewModel::editLastQuestion,
        onUpdateHistoryQuery = viewModel::updateHistoryQuery,
        onNewConversation = viewModel::newConversation,
        onLoadConversation = viewModel::loadConversation,
        onDeleteConversation = viewModel::deleteConversation,
    )
}
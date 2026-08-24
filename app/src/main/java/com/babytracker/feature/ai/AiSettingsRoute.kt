package com.babytracker.feature.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * AI 设置路由（组合根）：只负责 DI 装配、ViewModel 创建与导航映射，
 * 页面状态收集后交给 [AiSettingsScreen] 纯 UI 渲染。
 */
@Composable
fun AiSettingsRoute(navController: NavController) {
    val viewModel: AiSettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    AiSettingsScreen(
        state = state,
        onBack = { navController.popBackStack() },
        onRefreshConfig = viewModel::refreshConfig,
        onSetAssistantEnabled = viewModel::setAssistantEnabled,
        onSetDefaultModel = viewModel::setDefaultModel,
        onSetContextRounds = viewModel::setContextRounds,
        onSetMaxOutputTokens = viewModel::setMaxOutputTokens,
        onSetStreaming = viewModel::setStreaming,
        onSetThinkingMode = viewModel::setThinkingMode,
        onSetReasoningEffort = viewModel::setReasoningEffort,
        onSetCustomTemperature = viewModel::setCustomTemperature,
        onSetTemperatureTenths = viewModel::setTemperatureTenths,
        onSetAnswerDetail = viewModel::setAnswerDetail,
        onSetAnswerTone = viewModel::setAnswerTone,
        onSetActionChecklist = viewModel::setActionChecklist,
        onSetUseRecentRecords = viewModel::setUseRecentRecords,
        onSetUseFeeding = viewModel::setUseFeeding,
        onSetUseSleep = viewModel::setUseSleep,
        onSetUseDiaper = viewModel::setUseDiaper,
        onSetUseGrowth = viewModel::setUseGrowth,
        onSetUseHealth = viewModel::setUseHealth,
        onSetRecommendedQuestions = viewModel::setRecommendedQuestions,
        onClearConversation = viewModel::clearConversation,
    )
}
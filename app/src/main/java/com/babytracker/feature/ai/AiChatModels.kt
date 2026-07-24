package com.babytracker.feature.ai

import com.babytracker.core.ai.AiModelOption
import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.domain.model.Baby

enum class AiChatRole {
    USER,
    ASSISTANT,
}

enum class AiAnalysisSource {
    SLEEP,
    FEEDING,
    HEALTH,
    OVERVIEW,
}

enum class AiAnalysisUnavailableReason {
    DATA_DISABLED,
    NO_RECORDS,
}

internal fun analysisSuggestedQuestion(source: AiAnalysisSource): String = when (source) {
    AiAnalysisSource.SLEEP -> "帮我分析最近的睡眠情况"
    AiAnalysisSource.FEEDING -> "最近喂养记录有什么变化"
    AiAnalysisSource.HEALTH -> "帮我整理近期健康情况"
    AiAnalysisSource.OVERVIEW -> "综合解读宝宝的近期记录"
}

data class AiChatEntry(
    val id: Long,
    val role: AiChatRole,
    val content: String,
    val reasoningContent: String = "",
    val providerId: String? = null,
    val model: String? = null,
    val references: List<String> = emptyList(),
    val riskLevel: AiRiskLevel? = null,
    val safetyStatus: AiAnswerSafetyStatus? = null,
)

enum class AiChatError {
    INPUT_TOO_LONG,
    CONFIG_UNAVAILABLE,
    AUTHENTICATION,
    INSUFFICIENT_BALANCE,
    INVALID_REQUEST,
    RATE_LIMIT,
    SERVICE_UNAVAILABLE,
    NETWORK,
    UNKNOWN,
}

enum class AiChatPrerequisite {
    READY,
    DISABLED,
    NOT_LOGGED_IN,
    NO_FAMILY,
    FAMILY_VERIFYING,
    FAMILY_UNVERIFIED,
    NO_BABY,
    CONFIG_LOADING,
    CONFIG_UNAVAILABLE,
}

data class AiChatUiState(
    val baby: Baby? = null,
    val modelOptions: List<AiModelOption> = emptyList(),
    val selectedOptionId: String? = null,
    val messages: List<AiChatEntry> = emptyList(),
    val input: String = "",
    val isConfigRefreshing: Boolean = false,
    val isSending: Boolean = false,
    val error: AiChatError? = null,
    val prerequisite: AiChatPrerequisite = AiChatPrerequisite.CONFIG_LOADING,
    val familyId: String? = null,
    val preferences: AiAssistantPreferences = AiAssistantPreferences(),
    val analysisContext: AiAnalysisSource? = null,
    val availableAnalyses: Set<AiAnalysisSource> = emptySet(),
    val isAnalysisAvailabilityLoading: Boolean = false,
    val analysisUnavailableSource: AiAnalysisSource? = null,
    val analysisUnavailableReason: AiAnalysisUnavailableReason? = null,
) {
    val canSend: Boolean
        get() = prerequisite == AiChatPrerequisite.READY &&
            input.isNotBlank() &&
            input.length <= AiChatViewModel.MAX_INPUT_LENGTH &&
            !isSending

    val hasStreamingAnswer: Boolean
        get() = isSending && messages.lastOrNull()?.role == AiChatRole.ASSISTANT

    fun isStreaming(message: AiChatEntry): Boolean =
        hasStreamingAnswer && messages.lastOrNull()?.id == message.id
}

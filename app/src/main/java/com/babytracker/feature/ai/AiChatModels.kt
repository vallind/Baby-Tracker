package com.babytracker.feature.ai

import com.babytracker.core.ai.AiModelOption
import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.data.repository.AiConversationSummary
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

enum class AiAnalysisPeriod(val days: Long) {
    DAYS_7(7),
    DAYS_14(14),
    DAYS_30(30),
}

enum class AiAnalysisUnavailableReason {
    DATA_DISABLED,
    NO_RECORDS,
}

internal fun analysisSuggestedQuestion(
    source: AiAnalysisSource,
    period: AiAnalysisPeriod,
): String = when (source) {
    AiAnalysisSource.SLEEP -> "对比最近${period.days}天与前${period.days}天的睡眠变化"
    AiAnalysisSource.FEEDING -> "对比最近${period.days}天与前${period.days}天的喂养变化"
    AiAnalysisSource.HEALTH -> "对比最近${period.days}天与前${period.days}天的健康记录"
    AiAnalysisSource.OVERVIEW -> "综合对比宝宝最近${period.days}天的多类记录变化"
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

enum class AiHistorySaveStatus {
    IDLE,
    SAVING,
    SAVED,
    FAILED,
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
    val analysisPeriod: AiAnalysisPeriod = AiAnalysisPeriod.DAYS_7,
    val availableAnalyses: Set<AiAnalysisSource> = emptySet(),
    val isAnalysisAvailabilityLoading: Boolean = false,
    val analysisUnavailableSource: AiAnalysisSource? = null,
    val analysisUnavailableReason: AiAnalysisUnavailableReason? = null,
    val conversationId: Long? = null,
    val conversationTitle: String? = null,
    val conversations: List<AiConversationSummary> = emptyList(),
    val historyQuery: String = "",
    val isHistoryLoading: Boolean = false,
    val historySaveStatus: AiHistorySaveStatus = AiHistorySaveStatus.IDLE,
) {
    val canSend: Boolean
        get() = prerequisite == AiChatPrerequisite.READY &&
            input.isNotBlank() &&
            input.length <= AiChatViewModel.MAX_INPUT_LENGTH &&
            !isSending

    val hasStreamingAnswer: Boolean
        get() = isSending && messages.lastOrNull()?.role == AiChatRole.ASSISTANT

    val canReviseLastAnswer: Boolean
        get() = !isSending &&
            messages.size >= 2 &&
            messages.last().role == AiChatRole.ASSISTANT &&
            messages[messages.lastIndex - 1].role == AiChatRole.USER

    fun isStreaming(message: AiChatEntry): Boolean =
        hasStreamingAnswer && messages.lastOrNull()?.id == message.id

    val filteredConversations: List<AiConversationSummary>
        get() = filterAiConversations(conversations, historyQuery)
}

package com.babytracker.feature.ai

import com.babytracker.core.ai.AiModelOption
import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.domain.model.Baby

enum class AiChatRole {
    USER,
    ASSISTANT,
}

data class AiChatEntry(
    val id: Long,
    val role: AiChatRole,
    val content: String,
    val providerId: String? = null,
    val model: String? = null,
    val references: List<String> = emptyList(),
    val riskLevel: AiRiskLevel? = null,
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
) {
    val canSend: Boolean
        get() = prerequisite == AiChatPrerequisite.READY &&
            input.isNotBlank() &&
            input.length <= AiChatViewModel.MAX_INPUT_LENGTH &&
            !isSending
}

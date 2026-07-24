package com.babytracker.core.ai.settings

import com.babytracker.core.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
enum class AiAnswerDetail {
    CONCISE,
    BALANCED,
    DETAILED,
}

@Serializable
enum class AiAnswerTone {
    PRACTICAL,
    GENTLE,
    PROFESSIONAL,
}

@Serializable
enum class AiThinkingMode {
    AUTO,
    ENABLED,
    DISABLED,
}

@Serializable
enum class AiReasoningEffort {
    AUTO,
    LOW,
    MEDIUM,
    HIGH,
    MAX,
}

@Serializable
data class AiAssistantPreferences(
    val assistantEnabled: Boolean = true,
    val answerDetail: AiAnswerDetail = AiAnswerDetail.BALANCED,
    val answerTone: AiAnswerTone = AiAnswerTone.PRACTICAL,
    val includeActionChecklist: Boolean = true,
    val contextRounds: Int = 5,
    val maxOutputTokens: Int = 0,
    val streamingEnabled: Boolean = true,
    val thinkingMode: AiThinkingMode = AiThinkingMode.AUTO,
    val reasoningEffort: AiReasoningEffort = AiReasoningEffort.AUTO,
    val customTemperature: Boolean = false,
    val temperatureTenths: Int = 10,
    val useRecentRecords: Boolean = true,
    val useFeedingRecords: Boolean = true,
    val useSleepRecords: Boolean = true,
    val useDiaperRecords: Boolean = true,
    val useGrowthRecords: Boolean = true,
    val useHealthRecords: Boolean = true,
    val showRecommendedQuestions: Boolean = true,
    val autoScroll: Boolean = true,
    val renderMarkdown: Boolean = true,
    val showCopyFeedback: Boolean = true,
)

class AiSettingsStore(private val settingsStore: SettingsStore) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val preferences: StateFlow<AiAssistantPreferences> = settingsStore.settings
        .map { it.ai.preferences }
        .stateIn(scope, SharingStarted.Eagerly, AiAssistantPreferences())
    val defaultModels: StateFlow<Map<String, String>> = settingsStore.settings
        .map { it.ai.defaultModels }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    private val _clearConversationRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val clearConversationRequests: SharedFlow<Unit> = _clearConversationRequests.asSharedFlow()

    fun update(transform: (AiAssistantPreferences) -> AiAssistantPreferences) {
        scope.launch {
            settingsStore.update { current ->
                current.copy(
                    ai = current.ai.copy(
                        preferences = transform(current.ai.preferences),
                    ),
                )
            }
        }
    }

    fun setDefaultModel(familyId: String, optionId: String) {
        if (familyId.isBlank() || optionId.isBlank()) return
        scope.launch {
            settingsStore.update { current ->
                current.copy(
                    ai = current.ai.copy(
                        defaultModels = current.ai.defaultModels + (familyId to optionId),
                    ),
                )
            }
        }
    }

    fun defaultModel(familyId: String?): String? = familyId?.let(defaultModels.value::get)

    fun requestClearConversation() {
        _clearConversationRequests.tryEmit(Unit)
    }
}

package com.babytracker.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.ai.AiModelOption
import com.babytracker.core.ai.config.AiConfigCoordinator
import com.babytracker.core.ai.settings.AiAnswerDetail
import com.babytracker.core.ai.settings.AiAnswerTone
import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.ai.settings.AiSettingsStore
import com.babytracker.core.data.FamilyService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AiSettingsUiState(
    val preferences: AiAssistantPreferences = AiAssistantPreferences(),
    val familyId: String? = null,
    val modelOptions: List<AiModelOption> = emptyList(),
    val selectedModelId: String? = null,
    val configVersion: Int? = null,
    val expiresAt: Long? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class AiSettingsViewModel(
    private val settingsStore: AiSettingsStore,
    private val configCoordinator: AiConfigCoordinator,
    familyService: FamilyService,
) : ViewModel() {
    val state: StateFlow<AiSettingsUiState> = combine(
        settingsStore.preferences,
        settingsStore.defaultModels,
        configCoordinator.state,
        familyService.sessionState,
    ) { preferences, defaults, runtime, familyState ->
        val familyId = familyState.activeFamily?.id
        val options = runtime.bundle?.config?.options.orEmpty()
        val selected = defaults[familyId]
            ?.takeIf { id -> options.any { it.id == id } }
            ?: runtime.bundle?.config?.defaultOption
        AiSettingsUiState(
            preferences = preferences,
            familyId = familyId,
            modelOptions = options,
            selectedModelId = selected,
            configVersion = runtime.bundle?.config?.configVersion,
            expiresAt = runtime.bundle?.expiresAt,
            isRefreshing = runtime.isRefreshing,
            errorMessage = runtime.errorMessage,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AiSettingsUiState(),
    )

    fun setAssistantEnabled(value: Boolean) = update { it.copy(assistantEnabled = value) }
    fun setAnswerDetail(value: AiAnswerDetail) = update { it.copy(answerDetail = value) }
    fun setAnswerTone(value: AiAnswerTone) = update { it.copy(answerTone = value) }
    fun setActionChecklist(value: Boolean) = update { it.copy(includeActionChecklist = value) }
    fun setUseRecentRecords(value: Boolean) = update { it.copy(useRecentRecords = value) }
    fun setUseFeeding(value: Boolean) = update { it.copy(useFeedingRecords = value) }
    fun setUseSleep(value: Boolean) = update { it.copy(useSleepRecords = value) }
    fun setUseDiaper(value: Boolean) = update { it.copy(useDiaperRecords = value) }
    fun setUseGrowth(value: Boolean) = update { it.copy(useGrowthRecords = value) }
    fun setUseHealth(value: Boolean) = update { it.copy(useHealthRecords = value) }
    fun setRecommendedQuestions(value: Boolean) = update { it.copy(showRecommendedQuestions = value) }
    fun setAutoScroll(value: Boolean) = update { it.copy(autoScroll = value) }
    fun setRenderMarkdown(value: Boolean) = update { it.copy(renderMarkdown = value) }
    fun setCopyFeedback(value: Boolean) = update { it.copy(showCopyFeedback = value) }

    fun setDefaultModel(optionId: String) {
        state.value.familyId?.let { settingsStore.setDefaultModel(it, optionId) }
    }

    fun refreshConfig() {
        viewModelScope.launch { configCoordinator.refreshNow() }
    }

    fun clearConversation() {
        settingsStore.requestClearConversation()
    }

    private fun update(transform: (AiAssistantPreferences) -> AiAssistantPreferences) {
        settingsStore.update(transform)
    }
}

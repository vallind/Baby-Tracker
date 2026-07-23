package com.babytracker.core.ai.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AiAnswerDetail {
    CONCISE,
    BALANCED,
    DETAILED,
}

enum class AiAnswerTone {
    PRACTICAL,
    GENTLE,
    PROFESSIONAL,
}

enum class AiThinkingMode {
    AUTO,
    ENABLED,
    DISABLED,
}

enum class AiReasoningEffort {
    AUTO,
    LOW,
    MEDIUM,
    HIGH,
    MAX,
}

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

class AiSettingsStore(private val prefs: SharedPreferences) {
    private val _preferences = MutableStateFlow(load())
    val preferences: StateFlow<AiAssistantPreferences> = _preferences.asStateFlow()

    private val _defaultModels = MutableStateFlow(loadDefaultModels())
    val defaultModels: StateFlow<Map<String, String>> = _defaultModels.asStateFlow()

    private val _clearConversationRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val clearConversationRequests: SharedFlow<Unit> = _clearConversationRequests.asSharedFlow()

    fun update(transform: (AiAssistantPreferences) -> AiAssistantPreferences) {
        val updated = transform(_preferences.value)
        persist(updated)
        _preferences.value = updated
    }

    fun setDefaultModel(familyId: String, optionId: String) {
        if (familyId.isBlank() || optionId.isBlank()) return
        prefs.edit().putString(KEY_DEFAULT_MODEL_PREFIX + familyId, optionId).apply()
        _defaultModels.update { it + (familyId to optionId) }
    }

    fun defaultModel(familyId: String?): String? = familyId?.let(_defaultModels.value::get)

    fun requestClearConversation() {
        _clearConversationRequests.tryEmit(Unit)
    }

    private fun load() = AiAssistantPreferences(
        assistantEnabled = prefs.getBoolean(KEY_ENABLED, true),
        answerDetail = enumValue(prefs.getString(KEY_DETAIL, null), AiAnswerDetail.BALANCED),
        answerTone = enumValue(prefs.getString(KEY_TONE, null), AiAnswerTone.PRACTICAL),
        includeActionChecklist = prefs.getBoolean(KEY_ACTION_CHECKLIST, true),
        contextRounds = prefs.getInt(KEY_CONTEXT_ROUNDS, 5).coerceAtLeast(0),
        maxOutputTokens = prefs.getInt(KEY_MAX_OUTPUT_TOKENS, 0).coerceAtLeast(0),
        streamingEnabled = prefs.getBoolean(KEY_STREAMING, true),
        thinkingMode = enumValue(prefs.getString(KEY_THINKING_MODE, null), AiThinkingMode.AUTO),
        reasoningEffort = enumValue(
            prefs.getString(KEY_REASONING_EFFORT, null),
            AiReasoningEffort.AUTO,
        ),
        customTemperature = prefs.getBoolean(KEY_CUSTOM_TEMPERATURE, false),
        temperatureTenths = prefs.getInt(KEY_TEMPERATURE_TENTHS, 10).coerceIn(0, 20),
        useRecentRecords = prefs.getBoolean(KEY_USE_RECORDS, true),
        useFeedingRecords = prefs.getBoolean(KEY_USE_FEEDING, true),
        useSleepRecords = prefs.getBoolean(KEY_USE_SLEEP, true),
        useDiaperRecords = prefs.getBoolean(KEY_USE_DIAPER, true),
        useGrowthRecords = prefs.getBoolean(KEY_USE_GROWTH, true),
        useHealthRecords = prefs.getBoolean(KEY_USE_HEALTH, true),
        showRecommendedQuestions = prefs.getBoolean(KEY_RECOMMENDED, true),
        autoScroll = prefs.getBoolean(KEY_AUTO_SCROLL, true),
        renderMarkdown = prefs.getBoolean(KEY_MARKDOWN, true),
        showCopyFeedback = prefs.getBoolean(KEY_COPY_FEEDBACK, true),
    )

    private fun persist(value: AiAssistantPreferences) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, value.assistantEnabled)
            .putString(KEY_DETAIL, value.answerDetail.name)
            .putString(KEY_TONE, value.answerTone.name)
            .putBoolean(KEY_ACTION_CHECKLIST, value.includeActionChecklist)
            .putInt(KEY_CONTEXT_ROUNDS, value.contextRounds.coerceAtLeast(0))
            .putInt(KEY_MAX_OUTPUT_TOKENS, value.maxOutputTokens.coerceAtLeast(0))
            .putBoolean(KEY_STREAMING, value.streamingEnabled)
            .putString(KEY_THINKING_MODE, value.thinkingMode.name)
            .putString(KEY_REASONING_EFFORT, value.reasoningEffort.name)
            .putBoolean(KEY_CUSTOM_TEMPERATURE, value.customTemperature)
            .putInt(KEY_TEMPERATURE_TENTHS, value.temperatureTenths.coerceIn(0, 20))
            .putBoolean(KEY_USE_RECORDS, value.useRecentRecords)
            .putBoolean(KEY_USE_FEEDING, value.useFeedingRecords)
            .putBoolean(KEY_USE_SLEEP, value.useSleepRecords)
            .putBoolean(KEY_USE_DIAPER, value.useDiaperRecords)
            .putBoolean(KEY_USE_GROWTH, value.useGrowthRecords)
            .putBoolean(KEY_USE_HEALTH, value.useHealthRecords)
            .putBoolean(KEY_RECOMMENDED, value.showRecommendedQuestions)
            .putBoolean(KEY_AUTO_SCROLL, value.autoScroll)
            .putBoolean(KEY_MARKDOWN, value.renderMarkdown)
            .putBoolean(KEY_COPY_FEEDBACK, value.showCopyFeedback)
            .apply()
    }

    private fun loadDefaultModels(): Map<String, String> = prefs.all.mapNotNull { (key, value) ->
        if (key.startsWith(KEY_DEFAULT_MODEL_PREFIX) && value is String) {
            key.removePrefix(KEY_DEFAULT_MODEL_PREFIX) to value
        } else {
            null
        }
    }.toMap()

    private inline fun <reified T : Enum<T>> enumValue(value: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: fallback

    companion object {
        private const val KEY_ENABLED = "ai_enabled"
        private const val KEY_DETAIL = "ai_answer_detail"
        private const val KEY_TONE = "ai_answer_tone"
        private const val KEY_ACTION_CHECKLIST = "ai_action_checklist"
        private const val KEY_CONTEXT_ROUNDS = "ai_context_rounds"
        private const val KEY_MAX_OUTPUT_TOKENS = "ai_max_output_tokens"
        private const val KEY_STREAMING = "ai_streaming"
        private const val KEY_THINKING_MODE = "ai_thinking_mode"
        private const val KEY_REASONING_EFFORT = "ai_reasoning_effort"
        private const val KEY_CUSTOM_TEMPERATURE = "ai_custom_temperature"
        private const val KEY_TEMPERATURE_TENTHS = "ai_temperature_tenths"
        private const val KEY_USE_RECORDS = "ai_use_recent_records"
        private const val KEY_USE_FEEDING = "ai_use_feeding"
        private const val KEY_USE_SLEEP = "ai_use_sleep"
        private const val KEY_USE_DIAPER = "ai_use_diaper"
        private const val KEY_USE_GROWTH = "ai_use_growth"
        private const val KEY_USE_HEALTH = "ai_use_health"
        private const val KEY_RECOMMENDED = "ai_recommended_questions"
        private const val KEY_AUTO_SCROLL = "ai_auto_scroll"
        private const val KEY_MARKDOWN = "ai_render_markdown"
        private const val KEY_COPY_FEEDBACK = "ai_copy_feedback"
        private const val KEY_DEFAULT_MODEL_PREFIX = "ai_default_model_"
    }
}

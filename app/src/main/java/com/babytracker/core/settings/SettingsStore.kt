package com.babytracker.core.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.babytracker.core.ai.settings.AiAnswerDetail
import com.babytracker.core.ai.settings.AiAnswerTone
import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.ai.settings.AiReasoningEffort
import com.babytracker.core.ai.settings.AiThinkingMode
import com.babytracker.core.sync.BgInterval
import com.babytracker.core.sync.SyncConfig
import com.babytracker.core.sync.SyncDelay
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class SettingsStore(
    context: Context,
    legacyPreferences: SharedPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val ready = CompletableDeferred<Unit>()
    private val dataStore: DataStore<AppSettings> = DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { AppSettings() },
        migrations = listOf(LegacySettingsMigration(legacyPreferences)),
        scope = scope,
        produceFile = { context.dataStoreFile(FILE_NAME) },
    )

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        scope.launch {
            dataStore.data.collect { value ->
                _settings.value = value
                if (!ready.isCompleted) ready.complete(Unit)
            }
        }
    }

    suspend fun awaitReady() {
        ready.await()
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.updateData { current ->
            transform(current).copy(schemaVersion = AppSettings.CURRENT_SCHEMA_VERSION)
        }
    }

    companion object {
        private const val FILE_NAME = "app_settings.json"
    }
}

internal object AppSettingsSerializer : Serializer<AppSettings> {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override val defaultValue: AppSettings = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings {
        return try {
            json.decodeFromString<AppSettings>(input.readBytes().decodeToString())
        } catch (exception: SerializationException) {
            throw CorruptionException("设置文件无法解析", exception)
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        output.write(json.encodeToString(AppSettings.serializer(), t).encodeToByteArray())
    }
}

private class LegacySettingsMigration(
    private val preferences: SharedPreferences,
) : DataMigration<AppSettings> {
    override suspend fun shouldMigrate(currentData: AppSettings): Boolean =
        !preferences.getBoolean(KEY_MIGRATED, false)

    override suspend fun migrate(currentData: AppSettings): AppSettings {
        val defaultModels = preferences.all.mapNotNull { (key, value) ->
            if (key.startsWith(KEY_DEFAULT_MODEL_PREFIX) && value is String) {
                key.removePrefix(KEY_DEFAULT_MODEL_PREFIX) to value
            } else {
                null
            }
        }.toMap()

        return AppSettings(
            appearance = AppearanceSettings(
                themeName = preferences.getString(KEY_THEME, "pure") ?: "pure",
            ),
            sync = SyncConfig(
                autoSync = preferences.getBoolean(KEY_AUTO_SYNC, true),
                syncDelay = enumValue(
                    preferences.getString(KEY_SYNC_DELAY, null),
                    SyncDelay.SECONDS_2,
                ),
                bgInterval = enumValue(
                    preferences.getString(KEY_BG_INTERVAL, null),
                    BgInterval.OFF,
                ),
                wifiOnly = preferences.getBoolean(KEY_WIFI_ONLY, false),
                syncOnExit = preferences.getBoolean(KEY_SYNC_ON_EXIT, false),
            ),
            ai = AiSettings(
                preferences = AiAssistantPreferences(
                    assistantEnabled = preferences.getBoolean(KEY_AI_ENABLED, true),
                    answerDetail = enumValue(
                        preferences.getString(KEY_AI_DETAIL, null),
                        AiAnswerDetail.BALANCED,
                    ),
                    answerTone = enumValue(
                        preferences.getString(KEY_AI_TONE, null),
                        AiAnswerTone.PRACTICAL,
                    ),
                    includeActionChecklist = preferences.getBoolean(KEY_AI_CHECKLIST, true),
                    contextRounds = preferences.getInt(KEY_AI_CONTEXT_ROUNDS, 5).coerceAtLeast(0),
                    maxOutputTokens = preferences.getInt(KEY_AI_MAX_TOKENS, 0).coerceAtLeast(0),
                    streamingEnabled = preferences.getBoolean(KEY_AI_STREAMING, true),
                    thinkingMode = enumValue(
                        preferences.getString(KEY_AI_THINKING, null),
                        AiThinkingMode.AUTO,
                    ),
                    reasoningEffort = enumValue(
                        preferences.getString(KEY_AI_EFFORT, null),
                        AiReasoningEffort.AUTO,
                    ),
                    customTemperature = preferences.getBoolean(KEY_AI_CUSTOM_TEMPERATURE, false),
                    temperatureTenths = preferences.getInt(KEY_AI_TEMPERATURE, 10).coerceIn(0, 20),
                    useRecentRecords = preferences.getBoolean(KEY_AI_USE_RECORDS, true),
                    useFeedingRecords = preferences.getBoolean(KEY_AI_USE_FEEDING, true),
                    useSleepRecords = preferences.getBoolean(KEY_AI_USE_SLEEP, true),
                    useDiaperRecords = preferences.getBoolean(KEY_AI_USE_DIAPER, true),
                    useGrowthRecords = preferences.getBoolean(KEY_AI_USE_GROWTH, true),
                    useHealthRecords = preferences.getBoolean(KEY_AI_USE_HEALTH, true),
                    showRecommendedQuestions = preferences.getBoolean(KEY_AI_RECOMMENDED, true),
                    autoScroll = preferences.getBoolean(KEY_AI_AUTO_SCROLL, true),
                    renderMarkdown = preferences.getBoolean(KEY_AI_MARKDOWN, true),
                    showCopyFeedback = preferences.getBoolean(KEY_AI_COPY_FEEDBACK, true),
                ),
                defaultModels = defaultModels,
            ),
            diagnostics = DiagnosticsSettings(
                logCaptureEnabled = preferences.getBoolean(KEY_LOG_CAPTURE, false),
            ),
        )
    }

    override suspend fun cleanUp() {
        preferences.edit().putBoolean(KEY_MIGRATED, true).apply()
    }

    private inline fun <reified T : Enum<T>> enumValue(value: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: fallback

    companion object {
        private const val KEY_MIGRATED = "app_settings_datastore_migrated_v1"
        private const val KEY_THEME = "theme_name"
        private const val KEY_AUTO_SYNC = "sync_auto"
        private const val KEY_SYNC_DELAY = "sync_delay"
        private const val KEY_BG_INTERVAL = "sync_bg"
        private const val KEY_WIFI_ONLY = "sync_wifi_only"
        private const val KEY_SYNC_ON_EXIT = "sync_on_exit"
        private const val KEY_LOG_CAPTURE = "log_capture_enabled"
        private const val KEY_AI_ENABLED = "ai_enabled"
        private const val KEY_AI_DETAIL = "ai_answer_detail"
        private const val KEY_AI_TONE = "ai_answer_tone"
        private const val KEY_AI_CHECKLIST = "ai_action_checklist"
        private const val KEY_AI_CONTEXT_ROUNDS = "ai_context_rounds"
        private const val KEY_AI_MAX_TOKENS = "ai_max_output_tokens"
        private const val KEY_AI_STREAMING = "ai_streaming"
        private const val KEY_AI_THINKING = "ai_thinking_mode"
        private const val KEY_AI_EFFORT = "ai_reasoning_effort"
        private const val KEY_AI_CUSTOM_TEMPERATURE = "ai_custom_temperature"
        private const val KEY_AI_TEMPERATURE = "ai_temperature_tenths"
        private const val KEY_AI_USE_RECORDS = "ai_use_recent_records"
        private const val KEY_AI_USE_FEEDING = "ai_use_feeding"
        private const val KEY_AI_USE_SLEEP = "ai_use_sleep"
        private const val KEY_AI_USE_DIAPER = "ai_use_diaper"
        private const val KEY_AI_USE_GROWTH = "ai_use_growth"
        private const val KEY_AI_USE_HEALTH = "ai_use_health"
        private const val KEY_AI_RECOMMENDED = "ai_recommended_questions"
        private const val KEY_AI_AUTO_SCROLL = "ai_auto_scroll"
        private const val KEY_AI_MARKDOWN = "ai_render_markdown"
        private const val KEY_AI_COPY_FEEDBACK = "ai_copy_feedback"
        private const val KEY_DEFAULT_MODEL_PREFIX = "ai_default_model_"
    }
}

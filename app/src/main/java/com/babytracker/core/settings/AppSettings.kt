package com.babytracker.core.settings

import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.sync.SyncConfig
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val appearance: AppearanceSettings = AppearanceSettings(),
    val sync: SyncConfig = SyncConfig(),
    val ai: AiSettings = AiSettings(),
    val diagnostics: DiagnosticsSettings = DiagnosticsSettings(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

@Serializable
data class AppearanceSettings(
    val themeName: String = "pure",
)

@Serializable
data class AiSettings(
    val preferences: AiAssistantPreferences = AiAssistantPreferences(),
    val defaultModels: Map<String, String> = emptyMap(),
)

@Serializable
data class DiagnosticsSettings(
    val logCaptureEnabled: Boolean = false,
)

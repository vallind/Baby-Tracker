package com.babytracker.core.ai.config

import android.content.SharedPreferences
import com.babytracker.core.ai.AiRuntimeBundle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AiConfigStore(private val prefs: SharedPreferences) {
    companion object {
        private const val KEY_RUNTIME_BUNDLE = "ai_runtime_bundle_v1"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(userId: String, familyId: String): AiRuntimeBundle? {
        val bundle = peek() ?: return null
        return bundle.takeIf {
            it.userId == userId && it.familyId == familyId && it.isValidAt()
        }
    }

    fun peek(): AiRuntimeBundle? = prefs.getString(KEY_RUNTIME_BUNDLE, null)?.let { value ->
        runCatching { json.decodeFromString<AiRuntimeBundle>(value) }.getOrNull()
    }

    fun save(bundle: AiRuntimeBundle) {
        prefs.edit().putString(KEY_RUNTIME_BUNDLE, json.encodeToString(bundle)).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_RUNTIME_BUNDLE).apply()
    }
}

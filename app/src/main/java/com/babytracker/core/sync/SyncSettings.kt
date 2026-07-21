package com.babytracker.core.sync

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** 仅负责同步配置持久化，不在 setter 中执行调度副作用。 */
class SyncSettings(private val prefs: SharedPreferences) {
    val flow: Flow<SyncConfig> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in CONFIG_KEYS) trySend(readConfig())
        }
        trySend(readConfig())
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    val current: SyncConfig get() = readConfig()

    fun updateAutoSync(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    fun updateDelay(delay: SyncDelay) {
        prefs.edit().putString(KEY_SYNC_DELAY, delay.storedValue).apply()
    }

    fun updateBackgroundInterval(interval: BackgroundSyncInterval) {
        prefs.edit().putString(KEY_BG_INTERVAL, interval.storedValue).apply()
    }

    fun updateUnmeteredOnly(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_UNMETERED_ONLY, enabled).apply()
    }

    fun needsReconciliation(familyId: String): Boolean =
        familyId !in (prefs.getStringSet(KEY_RECONCILED_FAMILIES, emptySet()) ?: emptySet())

    fun markReconciled(familyId: String) {
        val reconciled = (prefs.getStringSet(KEY_RECONCILED_FAMILIES, emptySet()) ?: emptySet()).toMutableSet()
        reconciled += familyId
        prefs.edit().putStringSet(KEY_RECONCILED_FAMILIES, reconciled).apply()
    }

    private fun readConfig() = SyncConfig(
        autoSync = prefs.getBoolean(KEY_AUTO_SYNC, true),
        delay = SyncDelay.fromStored(prefs.getString(KEY_SYNC_DELAY, null)),
        backgroundInterval = BackgroundSyncInterval.fromStored(prefs.getString(KEY_BG_INTERVAL, null)),
        unmeteredOnly = prefs.getBoolean(KEY_UNMETERED_ONLY, false),
    )

    private companion object {
        const val KEY_AUTO_SYNC = "sync_auto"
        const val KEY_SYNC_DELAY = "sync_delay"
        const val KEY_BG_INTERVAL = "sync_bg_interval"
        const val KEY_UNMETERED_ONLY = "sync_unmetered_only"
        const val KEY_RECONCILED_FAMILIES = "sync_reconciled_families"
        val CONFIG_KEYS = setOf(KEY_AUTO_SYNC, KEY_SYNC_DELAY, KEY_BG_INTERVAL, KEY_UNMETERED_ONLY)
    }
}

data class SyncConfig(
    val autoSync: Boolean = true,
    val delay: SyncDelay = SyncDelay.TWO_SECONDS,
    val backgroundInterval: BackgroundSyncInterval = BackgroundSyncInterval.THIRTY_MINUTES,
    val unmeteredOnly: Boolean = false,
)

enum class SyncDelay(val storedValue: String, val millis: Long?, val label: String) {
    IMMEDIATE("immediate", 0L, "立即"),
    TWO_SECONDS("2s", 2_000L, "2 秒"),
    FIVE_SECONDS("5s", 5_000L, "5 秒"),
    TEN_SECONDS("10s", 10_000L, "10 秒"),
    THIRTY_SECONDS("30s", 30_000L, "30 秒"),
    ON_BACKGROUND("on_background", null, "退出到后台时"),
    ;

    companion object {
        fun fromStored(value: String?): SyncDelay = entries.firstOrNull { it.storedValue == value } ?: TWO_SECONDS
    }
}

enum class BackgroundSyncInterval(val storedValue: String, val minutes: Long?, val label: String) {
    OFF("off", null, "关闭"),
    THIRTY_MINUTES("30min", 30L, "每 30 分钟"),
    ONE_HOUR("1h", 60L, "每 1 小时"),
    TWO_HOURS("2h", 120L, "每 2 小时"),
    ;

    companion object {
        fun fromStored(value: String?): BackgroundSyncInterval =
            entries.firstOrNull { it.storedValue == value } ?: THIRTY_MINUTES
    }
}

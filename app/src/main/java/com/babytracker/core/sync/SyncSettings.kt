package com.babytracker.core.sync

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncSettings(private val prefs: SharedPreferences) {

    private val _config = MutableStateFlow(load())
    val config: StateFlow<SyncConfig> = _config.asStateFlow()

    fun updateAutoSync(enabled: Boolean) {
        _config.value = _config.value.copy(autoSync = enabled)
        persist()
    }

    fun updateSyncDelay(delay: SyncDelay) {
        _config.value = _config.value.copy(syncDelay = delay)
        persist()
    }

    fun updateBgInterval(interval: BgInterval) {
        _config.value = _config.value.copy(bgInterval = interval)
        persist()
    }

    fun updateWifiOnly(enabled: Boolean) {
        _config.value = _config.value.copy(wifiOnly = enabled)
        persist()
    }

    fun updateSyncOnExit(enabled: Boolean) {
        _config.value = _config.value.copy(syncOnExit = enabled)
        persist()
    }

    private fun load(): SyncConfig = SyncConfig(
        autoSync = prefs.getBoolean(KEY_AUTO_SYNC, true),
        syncDelay = SyncDelay.valueOf(prefs.getString(KEY_SYNC_DELAY, SyncDelay.SECONDS_2.name)!!),
        bgInterval = BgInterval.valueOf(prefs.getString(KEY_BG_INTERVAL, BgInterval.OFF.name)!!),
        wifiOnly = prefs.getBoolean(KEY_WIFI_ONLY, false),
        syncOnExit = prefs.getBoolean(KEY_SYNC_ON_EXIT, false),
    )

    private fun persist() {
        val c = _config.value
        prefs.edit()
            .putBoolean(KEY_AUTO_SYNC, c.autoSync)
            .putString(KEY_SYNC_DELAY, c.syncDelay.name)
            .putString(KEY_BG_INTERVAL, c.bgInterval.name)
            .putBoolean(KEY_WIFI_ONLY, c.wifiOnly)
            .putBoolean(KEY_SYNC_ON_EXIT, c.syncOnExit)
            .apply()
    }

    companion object {
        private const val KEY_AUTO_SYNC = "sync_auto"
        private const val KEY_SYNC_DELAY = "sync_delay"
        private const val KEY_BG_INTERVAL = "sync_bg"
        private const val KEY_WIFI_ONLY = "sync_wifi_only"
        private const val KEY_SYNC_ON_EXIT = "sync_on_exit"
    }
}

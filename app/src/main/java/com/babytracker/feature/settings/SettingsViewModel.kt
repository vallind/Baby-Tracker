package com.babytracker.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import com.babytracker.core.sync.BgInterval
import com.babytracker.core.sync.RealtimeManager
import com.babytracker.core.sync.RealtimeState
import com.babytracker.core.sync.SyncConfig
import com.babytracker.core.sync.SyncDelay
import com.babytracker.core.sync.SyncEngine
import com.babytracker.core.sync.SyncSettings
import com.babytracker.core.sync.SyncState
import com.babytracker.core.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val syncEngine: SyncEngine,
    private val realtimeManager: RealtimeManager,
    private val authService: AuthService,
    private val familyService: FamilyService,
    private val context: Context,
    private val syncSettings: SyncSettings,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncEngine.syncState
    val connectionState: StateFlow<RealtimeState> = realtimeManager.connectionState
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
    val isUnmetered: StateFlow<Boolean> = networkMonitor.isUnmetered
    val syncConfig: StateFlow<SyncConfig> = syncSettings.config

    val isLoggedIn: StateFlow<Boolean> = authService.observeAuthState()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), authService.isLoggedIn())

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult.asStateFlow()

    val syncStatusText: StateFlow<String> = combine(syncState, connectionState, isLoggedIn, isOnline, syncResult) { sync, conn, loggedIn, online, result ->
        val base = when {
            !loggedIn -> "未登录"
            sync == SyncState.SYNCING || sync == SyncState.PUSHING || sync == SyncState.PULLING -> "同步中..."
            !online -> "离线"
            conn == RealtimeState.CONNECTED -> "已连接"
            conn == RealtimeState.CONNECTING -> "连接中..."
            conn == RealtimeState.ERROR -> "连接失败"
            else -> "已同步"
        }
        if (result != null && base != "同步中..." && base != "离线") "$base·${result}" else base
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "待同步")

    // ── 同步配置更新 ──

    fun updateAutoSync(enabled: Boolean) = syncSettings.updateAutoSync(enabled)
    fun updateSyncDelay(delay: SyncDelay) = syncSettings.updateSyncDelay(delay)
    fun updateBgInterval(interval: BgInterval) = syncSettings.updateBgInterval(interval)
    fun updateWifiOnly(enabled: Boolean) = syncSettings.updateWifiOnly(enabled)

    // ── 手动同步（使用独立 scope，页面离开不停止）──
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun manualSync() {
        if (!isOnline.value) {
            _syncResult.value = "当前离线，无法同步"
            return
        }
        syncScope.launch {
            try {
                if (syncEngine.currentFamilyId == null) {
                    syncEngine.currentFamilyId = ensureFamily()
                }
                if (syncEngine.currentFamilyId == null) {
                    _syncResult.value = "请先创建或加入家庭"
                    return@launch
                }
                val pending = syncEngine.pendingCount()
                if (pending == 0) syncEngine.markExistingPending()
                val result = syncEngine.fullSync()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _syncResult.value = when {
                        pending == 0 && result.total == 0 -> "无数据需同步"
                        result.total > 0 -> "同步完成 ✓ 推送${result.pushed}拉取${result.pulled}"
                        else -> "待推送${pending}条，同步失败（网络或权限）"
                    }
                }
            } catch (e: Exception) {
                _syncResult.value = "同步失败：${e.message}"
            }
        }
    }

    fun clearSyncResult() { _syncResult.value = null }

    // ── 家庭 ID 获取 ──

    private suspend fun ensureFamily(): String? {
        familyService.currentFamily.value?.let { return it.id }
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.getString("current_family_id", null)?.let { return it }
        return try {
            val families = familyService.loadMyFamilies()
            families.firstOrNull()?.id
        } catch (_: Exception) {
            null
        }
    }
}

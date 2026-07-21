package com.babytracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.auth.AuthService
import com.babytracker.core.sync.RealtimeManager
import com.babytracker.core.sync.RealtimeState
import com.babytracker.core.sync.SyncCoordinator
import com.babytracker.core.sync.BackgroundSyncInterval
import com.babytracker.core.sync.SyncConfig
import com.babytracker.core.sync.SyncDelay
import com.babytracker.core.sync.SyncSettings
import com.babytracker.core.sync.SyncOutcome
import com.babytracker.core.sync.SyncReason
import com.babytracker.core.sync.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val coordinator: SyncCoordinator,
    realtimeManager: RealtimeManager,
    authService: AuthService,
    private val syncSettings: SyncSettings,
) : ViewModel() {
    val syncState: StateFlow<SyncState> = coordinator.syncState
    val connectionState: StateFlow<RealtimeState> = realtimeManager.connectionState
    val isOnline: StateFlow<Boolean> = coordinator.isOnline
    val syncConfig: StateFlow<SyncConfig> = syncSettings.flow
        .stateIn(viewModelScope, SharingStarted.Eagerly, syncSettings.current)

    val isLoggedIn: StateFlow<Boolean> = authService.observeAuthState()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), authService.isLoggedIn())

    val syncStatusText: StateFlow<String> = combine(
        syncState,
        connectionState,
        isLoggedIn,
        isOnline,
    ) { sync, connection, loggedIn, online ->
        when {
            !loggedIn -> "未登录"
            sync == SyncState.SYNCING || sync == SyncState.PUSHING || sync == SyncState.PULLING -> "同步中..."
            !online -> "离线"
            connection == RealtimeState.CONNECTED -> "已连接"
            connection == RealtimeState.CONNECTING -> "连接中..."
            connection == RealtimeState.ERROR -> "连接失败"
            else -> "待同步"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "待同步")

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult.asStateFlow()

    fun manualSync() {
        viewModelScope.launch {
            if (!isOnline.value) {
                _syncResult.value = "当前离线，无法同步"
                return@launch
            }
            val result = coordinator.syncNow(SyncReason.MANUAL)
            _syncResult.value = when {
                result == null -> "请先登录并创建或加入家庭"
                result.outcome == SyncOutcome.PARTIAL_FAILURE ->
                    "部分同步完成：推送${result.pushed} 拉取${result.pulled}，失败${result.failures.size}条"
                result.outcome == SyncOutcome.FAILURE ->
                    "同步失败：${result.failures.firstOrNull()?.message ?: "未知错误"}"
                result.total == 0 -> "无数据需同步"
                else -> "同步完成 ✓ 推送${result.pushed} 拉取${result.pulled}"
            }
        }
    }

    fun clearSyncResult() {
        _syncResult.value = null
    }

    fun updateAutoSync(enabled: Boolean) = syncSettings.updateAutoSync(enabled)

    fun updateSyncDelay(delay: SyncDelay) = syncSettings.updateDelay(delay)

    fun updateBackgroundInterval(interval: BackgroundSyncInterval) =
        syncSettings.updateBackgroundInterval(interval)

    fun updateUnmeteredOnly(enabled: Boolean) = syncSettings.updateUnmeteredOnly(enabled)
}

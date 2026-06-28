package com.babytracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.auth.AuthService
import com.babytracker.core.sync.RealtimeManager
import com.babytracker.core.sync.RealtimeState
import com.babytracker.core.sync.SyncEngine
import com.babytracker.core.sync.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 设置页 ViewModel —— 管理同步状态与手动/自动同步触发。
 *
 * 职责：
 * - 暴露 [SyncEngine.syncState] + [RealtimeManager.connectionState] 给 UI
 * - 提供 [manualSync] 手动触发双向同步
 * - 监听登录态变化，自动启动/停止 Realtime 订阅
 * - [lastSyncTime] 上次同步时间，格式化为可读字符串
 */
class SettingsViewModel(
    private val syncEngine: SyncEngine,
    private val realtimeManager: RealtimeManager,
    private val authService: AuthService,
) : ViewModel() {

    /** 同步引擎状态（IDLE / SYNCING / PUSHING / PULLING） */
    val syncState: StateFlow<SyncState> = syncEngine.syncState

    /** Realtime 连接状态（DISCONNECTED / CONNECTING / CONNECTED / ERROR） */
    val connectionState: StateFlow<RealtimeState> = realtimeManager.connectionState

    /** 当前是否已登录 */
    val isLoggedIn: StateFlow<Boolean> = authService.observeAuthState()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), authService.isLoggedIn())

    /** 综合同步状态文本，用于 UI 展示 */
    val syncStatusText: StateFlow<String> = combine(syncState, connectionState, isLoggedIn) { sync, conn, loggedIn ->
        when {
            !loggedIn -> "未登录"
            sync == SyncState.SYNCING || sync == SyncState.PUSHING || sync == SyncState.PULLING -> "同步中..."
            conn == RealtimeState.CONNECTED -> "已连接"
            conn == RealtimeState.CONNECTING -> "连接中..."
            conn == RealtimeState.ERROR -> "连接失败"
            else -> "待同步"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "待同步")

    init {
        // 监听登录态变化，自动启动/停止 Realtime 订阅
        viewModelScope.launch {
            authService.observeAuthState().collect { user ->
                if (user != null) {
                    // 登录后启动 Realtime 订阅 + 自动同步
                    realtimeManager.subscribeAll()
                    syncEngine.fullSync()
                } else {
                    // 退出后停止 Realtime
                    realtimeManager.unsubscribe()
                }
            }
        }
    }

    /** 手动触发完整双向同步 */
    fun manualSync() {
        viewModelScope.launch {
            syncEngine.fullSync()
        }
    }
}

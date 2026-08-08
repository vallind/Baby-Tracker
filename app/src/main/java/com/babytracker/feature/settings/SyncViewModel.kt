package com.babytracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import com.babytracker.core.sync.RealtimeManager
import com.babytracker.core.sync.RealtimeState
import com.babytracker.core.sync.SyncEngine
import com.babytracker.core.sync.SyncState
import com.babytracker.core.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncViewModel(
    private val syncEngine: SyncEngine,
    realtimeManager: RealtimeManager,
    private val authService: AuthService,
    private val familyService: FamilyService,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    val syncState: StateFlow<SyncState> = syncEngine.syncState
    val connectionState: StateFlow<RealtimeState> = realtimeManager.connectionState
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult.asStateFlow()

    /** 每次 manualSync 完成自增；字符串结果可能被 StateFlow 去重，UI 需用它复位"同步中"状态 */
    private val _syncRunId = MutableStateFlow(0)
    val syncRunId: StateFlow<Int> = _syncRunId.asStateFlow()

    fun manualSync() {
        if (!isOnline.value) {
            _syncResult.value = "当前离线，无法同步"
            _syncRunId.value++
            return
        }
        viewModelScope.launch {
            try {
                val verifiedUserId = authService.verifiedUserId()
                if (verifiedUserId == null) {
                    _syncResult.value = "登录会话尚未验证"
                    return@launch
                }
                familyService.refreshForUser(verifiedUserId)
                syncEngine.currentFamilyId =
                    familyService.sessionState.value.verifiedFamilyForSync?.id
                if (syncEngine.currentFamilyId == null) {
                    _syncResult.value = "请先创建或加入家庭"
                    return@launch
                }
                val pending = syncEngine.pendingCount()
                if (pending == 0) syncEngine.markExistingPending()
                val result = syncEngine.fullSync()
                _syncResult.value = when {
                    pending == 0 && result.total == 0 -> "无数据需同步"
                    result.total > 0 -> "同步完成 ✓ 推送${result.pushed}拉取${result.pulled}"
                    else -> "待推送${pending}条，同步失败（网络或权限）"
                }
            } catch (exception: Exception) {
                _syncResult.value = "同步失败：${exception.message}"
            } finally {
                _syncRunId.value++
            }
        }
    }
}

package com.babytracker.feature.settings

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import com.babytracker.core.sync.RealtimeManager
import com.babytracker.core.sync.RealtimeState
import com.babytracker.core.sync.SyncEngine
import com.babytracker.core.sync.EngineSyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val syncEngine: SyncEngine,
    private val realtimeManager: RealtimeManager,
    private val authService: AuthService,
    private val familyService: FamilyService,
    private val context: Context,
) : ViewModel() {

    val syncState: StateFlow<EngineSyncState> = syncEngine.syncState
    val connectionState: StateFlow<RealtimeState> = realtimeManager.connectionState

    val isLoggedIn: StateFlow<Boolean> = authService.observeAuthState()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), authService.isLoggedIn())

    /** 网络是否可用 */
    private val _isOnline = MutableStateFlow(checkNetwork())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    /** 防止并发创建多个家庭 */
    private val ensureFamilyMutex = Mutex()

    /** 综合同步状态文本 */
    val syncStatusText: StateFlow<String> = combine(syncState, connectionState, isLoggedIn, isOnline) { sync, conn, loggedIn, online ->
        when {
            !loggedIn -> "未登录"
            sync == EngineSyncState.SYNCING || sync == EngineSyncState.PUSHING || sync == EngineSyncState.PULLING -> "同步中..."
            !online -> "离线"
            conn == RealtimeState.CONNECTED -> "已连接"
            conn == RealtimeState.CONNECTING -> "连接中..."
            conn == RealtimeState.ERROR -> "连接失败"
            else -> "待同步"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "待同步")

    /** 上次已同步的家庭 ID，用于检测家庭切换并触发全量同步 */
    private var lastSyncedFamilyId: String? = null

    init {
        // 启动时从本地恢复 familyId（Supabase 挂了也能同步）
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        lastSyncedFamilyId = prefs.getString("current_family_id", null)
        syncEngine.currentFamilyId = lastSyncedFamilyId

        registerNetworkCallback()
        viewModelScope.launch {
            authService.observeAuthState().collect { user ->
                if (user != null && _isOnline.value) {
                    tryAutoSync()
                } else if (user == null) {
                    realtimeManager.unsubscribe()
                    syncEngine.currentFamilyId = null
                    lastSyncedFamilyId = null
                }
            }
        }
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online && authService.isLoggedIn()) {
                    tryAutoSync()
                }
            }
        }
        viewModelScope.launch {
            familyService.currentFamily.collect { family ->
                val newId = family?.id
                syncEngine.currentFamilyId = newId
                // 持久化到本地，Supabase 不通时仍可同步
                newId?.let { prefs.edit().putString("current_family_id", it).apply() }

                // 加入/切换到新家庭时，触发全量同步（拉取该家庭的历史数据）
                val isNewFamily = newId != null && newId != lastSyncedFamilyId
                if (isNewFamily && _isOnline.value) {
                    viewModelScope.launch {
                        syncEngine.resetLastSync()  // 清除增量锚点，执行全量拉取
                        syncEngine.fullSync()       // 拉取新家庭所有历史数据
                        lastSyncedFamilyId = newId
                    }
                } else {
                    lastSyncedFamilyId = newId
                }

                // 同步完成后可能需要刷新 Realtime 订阅（RLS 已随家庭成员变化更新）
                if (isNewFamily) {
                    realtimeManager.subscribeAll()
                }
            }
        }
    }

    private suspend fun tryAutoSync() {
        try {
            syncEngine.currentFamilyId = ensureFamily()
            if (syncEngine.currentFamilyId == null) return
            syncEngine.markExistingPending()  // 首次同步标记存量
            realtimeManager.subscribeAll()
            syncEngine.fullSync()
        } catch (_: Exception) { }
    }

    /**
     * 获取当前家庭 ID（多层回退）：
     * 1. 内存 currentFamily → 2. Supabase API → 3. SharedPreferences 离线兜底
     * 不自动创建——家庭需用户主动创建或加入
     */
    private suspend fun ensureFamily(): String? = ensureFamilyMutex.withLock {
        try {
            familyService.currentFamily.value?.let { return@withLock it.id }
            val families = familyService.loadMyFamilies()
            families.firstOrNull()?.id
        } catch (_: Exception) {
            // 离线回退：App 重启后内存/Supabase 都为空时，从本地持久化恢复
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.getString("current_family_id", null)
        }
    }

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult.asStateFlow()

    fun manualSync() {
        viewModelScope.launch {
            if (!_isOnline.value) {
                _syncResult.value = "当前离线，无法同步"
                return@launch
            }
            try {
                if (syncEngine.currentFamilyId == null) {
                    syncEngine.currentFamilyId = ensureFamily()
                }
                if (syncEngine.currentFamilyId == null) {
                    _syncResult.value = "请先创建或加入家庭"
                    return@launch
                }
                // 首次同步：标记存量数据为 pending
                syncEngine.markExistingPending()
                val pending = syncEngine.pendingCount()
                val result = syncEngine.fullSync()
                val count = result.first + result.second
                _syncResult.value = when {
                    pending == 0 && count == 0 -> "无数据需同步"
                    count > 0 -> "同步完成 ✓ 推送${result.first}拉取${result.second}"
                    else -> "待推送${pending}条，同步失败（网络或权限）"
                }
            } catch (e: Exception) {
                _syncResult.value = "同步失败：${e.message}"
            }
        }
    }

    fun clearSyncResult() { _syncResult.value = null }

    // ── 网络监听 ──

    private fun checkNetwork(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            val network = cm.activeNetwork ?: return true // 无法判断时假设在线
            val caps = cm.getNetworkCapabilities(network) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true // 缺权限或异常时假设在线，避免误伤
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) { _isOnline.value = true }
                override fun onLost(network: Network) { _isOnline.value = false }
            })
        } catch (_: Exception) { }
    }
}

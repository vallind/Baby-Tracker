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
import com.babytracker.core.sync.SyncState
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

    val syncState: StateFlow<SyncState> = syncEngine.syncState
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
            sync == SyncState.SYNCING || sync == SyncState.PUSHING || sync == SyncState.PULLING -> "同步中..."
            !online -> "离线"
            conn == RealtimeState.CONNECTED -> "已连接"
            conn == RealtimeState.CONNECTING -> "连接中..."
            conn == RealtimeState.ERROR -> "连接失败"
            else -> "待同步"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "待同步")

    init {
        // 网络状态监听
        registerNetworkCallback()
        // 登录态变化
        viewModelScope.launch {
            authService.observeAuthState().collect { user ->
                if (user != null && _isOnline.value) {
                    tryAutoSync()
                } else if (user == null) {
                    realtimeManager.unsubscribe()
                    syncEngine.currentFamilyId = null
                }
            }
        }
        // 网络恢复时自动重试
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online && authService.isLoggedIn()) {
                    tryAutoSync()
                }
            }
        }
        // 家庭变化
        viewModelScope.launch {
            familyService.currentFamily.collect { family ->
                syncEngine.currentFamilyId = family?.id
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

    private suspend fun ensureFamily(): String? = ensureFamilyMutex.withLock {
        try {
            familyService.currentFamily.value?.let { return@withLock it.id }
            val families = familyService.loadMyFamilies()
            if (families.isNotEmpty()) return@withLock families.first().id
            familyService.createFamily("我的家庭").getOrNull()?.id
        } catch (_: Exception) { null }
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
                val result = syncEngine.fullSync()
                val count = result.first + result.second
                _syncResult.value = if (count > 0) "同步完成 ✓ 推送${result.first}条 拉取${result.second}条" else "无数据需同步"
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

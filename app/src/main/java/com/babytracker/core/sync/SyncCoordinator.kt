package com.babytracker.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import com.babytracker.core.database.dao.SyncMetadataDao
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/** 应用级同步生命周期：不依赖任何页面是否被打开。 */
class SyncCoordinator(
    private val context: Context,
    private val authService: AuthService,
    private val familyService: FamilyService,
    private val syncEngine: SyncEngine,
    private val realtimeManager: RealtimeManager,
    private val syncMetadataDao: SyncMetadataDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)
    private val syncMutex = Mutex()
    private var validatedUserId: String? = null

    private val _isOnline = MutableStateFlow(checkNetwork())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _lastResult = MutableStateFlow<SyncRunResult?>(null)
    val lastResult: StateFlow<SyncRunResult?> = _lastResult.asStateFlow()
    val syncState: StateFlow<SyncState> = syncEngine.syncState

    fun start() {
        if (!started.compareAndSet(false, true)) return
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        syncEngine.currentFamilyId = prefs.getString("current_family_id", null)
        registerNetworkCallback()
        schedulePeriodicSync()

        scope.launch {
            authService.observeAuthState().collect { user ->
                if (user == null) {
                    realtimeManager.unsubscribe()
                    syncEngine.currentFamilyId = null
                    validatedUserId = null
                } else if (_isOnline.value) {
                    syncNow(SyncReason.AUTH_CHANGED)
                }
            }
        }
        scope.launch {
            familyService.currentFamily.collect { family ->
                val familyId = family?.id ?: return@collect
                val changed = familyId != syncEngine.currentFamilyId
                syncEngine.currentFamilyId = familyId
                prefs.edit().putString("current_family_id", familyId).apply()
                if (changed) realtimeManager.subscribeAll()
                if (_isOnline.value && authService.isLoggedIn()) {
                    syncNow(SyncReason.FAMILY_CHANGED)
                }
            }
        }
        observePendingWrites()
    }

    suspend fun syncNow(reason: SyncReason): SyncRunResult? = syncMutex.withLock {
        if (!_isOnline.value || !authService.isLoggedIn()) return@withLock null
        val familyId = ensureFamily() ?: return@withLock null
        syncEngine.currentFamilyId = familyId
        return@withLock try {
            syncEngine.markExistingPending()
            realtimeManager.subscribeAll()
            val result = syncEngine.fullSync()
            _lastResult.value = result
            if (result.failures.isEmpty()) {
                Timber.tag("Sync").d("sync reason=%s pushed=%d pulled=%d", reason, result.pushed, result.pulled)
            } else {
                Timber.tag("Sync").e(
                    "sync reason=%s partial pushed=%d pulled=%d failures=%d",
                    reason,
                    result.pushed,
                    result.pulled,
                    result.failures.size,
                )
            }
            result
        } catch (e: Exception) {
            val result = SyncRunResult(failures = listOf(SyncFailure("sync", message = e.message ?: "同步失败")))
            _lastResult.value = result
            Timber.tag("Sync").e(e, "sync reason=%s failed", reason)
            result
        }
    }

    private suspend fun ensureFamily(): String? {
        val userId = authService.currentUserId() ?: return null
        if (validatedUserId == userId) return familyService.currentFamily.value?.id
        return try {
            familyService.loadMyFamilies()
            validatedUserId = userId
            familyService.currentFamily.value?.id
        } catch (e: Exception) {
            Timber.tag("Sync").e(e, "load family failed")
            null
        }
    }

    @OptIn(FlowPreview::class)
    private fun observePendingWrites() {
        scope.launch {
            syncMetadataDao.watchPendingCount()
                .distinctUntilChanged()
                .debounce(1_500)
                .collect { pending ->
                    if (pending > 0 && _isOnline.value && authService.isLoggedIn()) {
                        syncNow(SyncReason.LOCAL_WRITE)
                    }
                }
        }
    }

    private fun registerNetworkCallback() {
        try {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            manager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                    scope.launch { syncNow(SyncReason.NETWORK_RESTORED) }
                }

                override fun onLost(network: Network) {
                    _isOnline.value = checkNetwork()
                }
            })
        } catch (e: Exception) {
            Timber.tag("Sync").e(e, "register network callback failed")
        }
    }

    private fun checkNetwork(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    companion object {
        const val PERIODIC_WORK_NAME = "baby-tracker-sync"
    }
}

enum class SyncReason { AUTH_CHANGED, FAMILY_CHANGED, NETWORK_RESTORED, LOCAL_WRITE, MANUAL, BACKGROUND }

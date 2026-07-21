package com.babytracker.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import com.babytracker.core.database.dao.SyncMetadataDao
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val syncSettings: SyncSettings,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)
    private val syncMutex = Mutex()
    private var validatedUserId: String? = null
    private var automaticNetworkAllowed = false

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
        observeBackgroundConfig()

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
                if (changed && syncSettings.current.autoSync && isNetworkAllowed(syncSettings.current)) {
                    realtimeManager.subscribeAll()
                }
                if (_isOnline.value && authService.isLoggedIn()) {
                    syncNow(SyncReason.FAMILY_CHANGED)
                }
            }
        }
        observePendingWrites()
    }

    suspend fun syncNow(reason: SyncReason, mode: SyncMode = SyncMode.FULL): SyncRunResult? = syncMutex.withLock {
        val config = syncSettings.current
        if (reason != SyncReason.MANUAL && (!config.autoSync || !isNetworkAllowed(config))) return@withLock null
        if (!_isOnline.value || !authService.isLoggedIn()) return@withLock null
        val familyId = ensureFamily() ?: return@withLock null
        syncEngine.currentFamilyId = familyId
        return@withLock try {
            val reconciliationFailures = if (mode == SyncMode.FULL && syncSettings.needsReconciliation(familyId)) {
                syncEngine.markExistingPending().also { failures ->
                    if (failures.isEmpty()) syncSettings.markReconciled(familyId)
                }
            } else {
                emptyList()
            }
            if (config.autoSync && isNetworkAllowed(config)) realtimeManager.subscribeAll()
            val runResult = when (mode) {
                SyncMode.FULL -> syncEngine.fullSync()
                SyncMode.PUSH_ONLY -> syncEngine.push().let { SyncRunResult(pushed = it.successCount, failures = it.failures) }
            }
            val result = runResult.copy(failures = reconciliationFailures + runResult.failures)
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

    fun onAppBackgrounded() {
        val config = syncSettings.current
        if (!config.autoSync || config.delay != SyncDelay.ON_BACKGROUND) return
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf(SyncWorker.KEY_MODE to SyncMode.PUSH_ONLY.name))
            .setConstraints(networkConstraints(config))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            EXIT_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePendingWrites() {
        scope.launch {
            combine(syncMetadataDao.watchPendingCount(), syncSettings.flow) { pending, config ->
                pending to config
            }.flatMapLatest { (pending, config) ->
                val delayMillis = config.delay.millis
                if (pending <= 0 || !config.autoSync || delayMillis == null) {
                    emptyFlow()
                } else {
                    flow {
                        delay(delayMillis)
                        emit(Unit)
                    }
                }
            }.collect { syncNow(SyncReason.LOCAL_WRITE) }
        }
    }

    private fun observeBackgroundConfig() {
        scope.launch {
            syncSettings.flow.distinctUntilChanged().collect { config ->
                scheduleOrCancelPeriodicSync(config)
                if (!config.autoSync || !isNetworkAllowed(config)) {
                    automaticNetworkAllowed = false
                    realtimeManager.unsubscribe()
                } else if (_isOnline.value && authService.isLoggedIn()) {
                    automaticNetworkAllowed = true
                    syncNow(SyncReason.SETTINGS_CHANGED)
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
                    scope.launch {
                        val config = syncSettings.current
                        automaticNetworkAllowed = config.autoSync && isNetworkAllowed(config)
                        if (!automaticNetworkAllowed) realtimeManager.unsubscribe()
                        syncNow(SyncReason.NETWORK_RESTORED)
                    }
                }

                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    scope.launch {
                        val config = syncSettings.current
                        val allowed = config.autoSync && isNetworkAllowed(config)
                        val changed = allowed != automaticNetworkAllowed
                        automaticNetworkAllowed = allowed
                        if (!allowed) {
                            realtimeManager.unsubscribe()
                        } else if (changed) {
                            syncNow(SyncReason.NETWORK_RESTORED)
                        }
                    }
                }

                override fun onLost(network: Network) {
                    _isOnline.value = checkNetwork()
                    if (!_isOnline.value) {
                        automaticNetworkAllowed = false
                        realtimeManager.unsubscribe()
                    }
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

    private fun isNetworkAllowed(config: SyncConfig): Boolean {
        if (!config.unmeteredOnly) return true
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        return !manager.isActiveNetworkMetered
    }

    private fun networkConstraints(config: SyncConfig): Constraints = Constraints.Builder()
        .setRequiredNetworkType(if (config.unmeteredOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
        .build()

    private fun scheduleOrCancelPeriodicSync(config: SyncConfig) {
        val interval = config.backgroundInterval.minutes
        val workManager = WorkManager.getInstance(context)
        if (!config.autoSync || interval == null) {
            workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<SyncWorker>(interval, TimeUnit.MINUTES)
            .setConstraints(networkConstraints(config))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    companion object {
        const val PERIODIC_WORK_NAME = "baby-tracker-sync"
        const val EXIT_WORK_NAME = "baby-tracker-exit-sync"
    }
}

enum class SyncReason {
    AUTH_CHANGED,
    FAMILY_CHANGED,
    NETWORK_RESTORED,
    LOCAL_WRITE,
    SETTINGS_CHANGED,
    MANUAL,
    BACKGROUND,
}
enum class SyncMode { FULL, PUSH_ONLY }

package com.babytracker.core.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import com.babytracker.core.database.dao.SyncMetadataDao
import com.babytracker.core.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 待同步变更通知 — Repository 写入 sync_metadata(pending) 后发出事件，
 * 绕过 Room Flow 失效传播的不可靠性。
 */
object PendingChangeNotifier {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    fun changed() {
        _events.tryEmit(Unit)
    }
}

class SyncTrigger(
    private val syncMeta: SyncMetadataDao,
    private val syncSettings: SyncSettings,
    private val syncEngine: SyncEngine,
    private val authService: AuthService,
    private val familyService: FamilyService,
    private val networkMonitor: NetworkMonitor,
    private val realtimeManager: RealtimeManager,
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _lastSyncResult = MutableStateFlow<String?>(null)
    val lastSyncResult: StateFlow<String?> = _lastSyncResult.asStateFlow()

    private var existingPendingMarked = prefs.getBoolean("sync_pending_marked", false)
    private var lastSyncedFamilyId: String? = prefs.getString("sync_last_family", null)
    private var lastTriggeredUserId: String? = prefs.getString("sync_last_user", null)

    companion object {
        private const val SYNC_WORK_NAME = "bg_sync"
    }

    fun start() {
        Timber.tag("Sync").d("SyncTrigger start")
        observeFamily()
        observeAuth()
        observeNetwork()
        observeAutoTrigger()
        observeBgInterval()
    }

    fun onAppBackgrounded() {
        val config = syncSettings.config.value
        if (config.syncDelay != SyncDelay.ON_EXIT) return
        if (!shouldSync(config)) return
        scope.launch {
            try {
                syncEngine.push()
                _lastSyncResult.value = "同步完成"
            } catch (e: Exception) {
                Timber.tag("Sync").e(e, "onAppBackgrounded push failed")
                _lastSyncResult.value = "同步失败"
            }
        }
    }

    private fun observeFamily() {
        scope.launch {
            Timber.tag("Sync").d("observeFamily start")
            familyService.currentFamily.collect { family ->
                val newId = family?.id
                Timber.tag("Sync").d("observeFamily family=%s", newId)
                syncEngine.currentFamilyId = newId
                com.babytracker.core.data.repository.currentSyncFamilyId = newId
                if (newId != null) {
                    val isNewFamily = newId != lastSyncedFamilyId
                    if (isNewFamily) {
                        syncEngine.resetLastSync()
                        lastSyncedFamilyId = newId
                        prefs.edit().putString("sync_last_family", newId).apply()
                    }
                    if (!existingPendingMarked) {
                        syncEngine.markExistingPending()
                        existingPendingMarked = true
                        prefs.edit().putBoolean("sync_pending_marked", true).apply()
                    }
                    triggerSync()
                    authService.currentUserId()?.let {
                        realtimeManager.subscribeAll()
                    }
                } else {
                    realtimeManager.unsubscribe()
                }
            }
        }
    }

    private fun observeAuth() {
        scope.launch {
            authService.observeAuthState().collect { user ->
                val uid = user?.id
                if (uid != null && uid != lastTriggeredUserId) {
                    lastTriggeredUserId = uid
                    prefs.edit().putString("sync_last_user", uid).apply()
                    triggerSync()
                } else if (user == null) {
                    realtimeManager.unsubscribe()
                }
            }
        }
    }

    private fun observeNetwork() {
        scope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online && authService.isLoggedIn() && syncEngine.currentFamilyId != null) {
                    triggerSync()
                }
            }
        }
    }

    private fun triggerSync() {
        scope.launch {
            try {
                syncEngine.push()
                Timber.tag("Sync").d("auto trigger sync done")
            } catch (e: Exception) {
                Timber.tag("Sync").e(e, "auto trigger sync failed")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAutoTrigger() {
        Timber.tag("Sync").d("observeAutoTrigger start")
        scope.launch {
            PendingChangeNotifier.events.collect {
                val config = syncSettings.config.value
                if (config.syncDelay == SyncDelay.ON_EXIT) return@collect
                val fid = syncEngine.currentFamilyId
                if (fid == null) return@collect
                if (!config.autoSync) { Timber.tag("Sync").d("autoTrigger skip: autoSync off"); return@collect }
                val loggedIn = authService.currentUserId() != null
                val online = networkMonitor.isOnline.value
                val unmetered = networkMonitor.isUnmetered.value
                if (!loggedIn) return@collect
                if (!online) { Timber.tag("Sync").d("autoTrigger skip: offline"); return@collect }
                if (config.wifiOnly && !unmetered) { Timber.tag("Sync").d("autoTrigger skip: not unmetered"); return@collect }
                val pending = syncMeta.pendingCount(fid)
                if (pending <= 0) return@collect
                val delay = config.syncDelay.millis
                if (delay > 0L) {
                    Timber.tag("Sync").d("autoTrigger debounce %dms pending=%d", delay, pending)
                    kotlinx.coroutines.delay(delay)
                    val stillPending = syncMeta.pendingCount(fid)
                    if (stillPending <= 0) return@collect
                }
                Timber.tag("Sync").d("autoTrigger push pending=%d", pending)
                doPush()
            }
        }
    }

    private fun observeBgInterval() {
        scope.launch {
            syncSettings.config.collect { config ->
                scheduleBgSync(config)
            }
        }
    }

    private fun scheduleBgSync(config: SyncConfig) {
        if (config.bgInterval == BgInterval.OFF) {
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
            return
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (config.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .build()
        val request = PeriodicWorkRequestBuilder<SyncWorker>(config.bgInterval.periodMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
    }

    private fun shouldSync(config: SyncConfig): Boolean {
        val fid = syncEngine.currentFamilyId ?: return false
        if (!config.autoSync) return false
        val user = authService.currentUserId()
        if (user == null) return false
        if (!networkMonitor.isOnline.value) return false
        if (config.wifiOnly && !networkMonitor.isUnmetered.value) return false
        return true
    }

    private suspend fun doPush() {
        try {
            syncEngine.push()
            Timber.tag("Sync").d("auto push done")
        } catch (e: Exception) {
            Timber.tag("Sync").e(e, "auto push failed")
        }
    }
}

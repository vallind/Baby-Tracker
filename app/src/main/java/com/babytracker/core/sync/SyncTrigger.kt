package com.babytracker.core.sync

import android.content.Context
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _lastSyncResult = MutableStateFlow<String?>(null)
    val lastSyncResult: StateFlow<String?> = _lastSyncResult.asStateFlow()

    private var existingPendingMarked = false

    companion object {
        private const val SYNC_WORK_NAME = "bg_sync"
    }

    fun start() {
        Timber.tag("Sync").d("SyncTrigger start")
        observeFamily()
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
                if (newId != null) {
                    if (!existingPendingMarked) {
                        Timber.tag("Sync").d("observeFamily markExistingPending + push")
                        syncEngine.markExistingPending()
                        existingPendingMarked = true
                        syncEngine.push()
                    }
                    authService.currentUserId()?.let {
                        realtimeManager.subscribeAll()
                    }
                } else {
                    realtimeManager.unsubscribe()
                }
            }
        }
    }

    private fun observeAutoTrigger() {
        Timber.tag("Sync").d("observeAutoTrigger start")
        scope.launch {
            PendingChangeNotifier.events.collect {
                Timber.tag("Sync").d("autoTrigger event received")
                val config = syncSettings.config.value
                val fid = syncEngine.currentFamilyId
                if (fid == null) { Timber.tag("Sync").d("autoTrigger skip: no fid"); return@collect }
                if (!config.autoSync) { Timber.tag("Sync").d("autoTrigger skip: autoSync off"); return@collect }
                val loggedIn = authService.currentUserId() != null
                val online = networkMonitor.isOnline.value
                val unmetered = networkMonitor.isUnmetered.value
                if (!loggedIn) { Timber.tag("Sync").d("autoTrigger skip: not logged in"); return@collect }
                if (!online) { Timber.tag("Sync").d("autoTrigger skip: offline"); return@collect }
                if (config.wifiOnly && !unmetered) { Timber.tag("Sync").d("autoTrigger skip: not unmetered"); return@collect }
                val pending = syncMeta.pendingCount(fid)
                Timber.tag("Sync").d("autoTrigger eval: pending=%d", pending)
                if (pending <= 0) { Timber.tag("Sync").d("autoTrigger skip: no pending"); return@collect }
                Timber.tag("Sync").d("autoTrigger firing push")
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

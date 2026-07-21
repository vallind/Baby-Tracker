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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

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

    /** 是否已对当前家庭执行过存量标记 */
    private var existingPendingMarked = false

    companion object {
        private const val SYNC_WORK_NAME = "bg_sync"
    }

    fun start() {
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
            familyService.currentFamily.collect { family ->
                val newId = family?.id
                syncEngine.currentFamilyId = newId
                if (newId != null) {
                    if (!existingPendingMarked) {
                        syncEngine.markExistingPending()
                        existingPendingMarked = true
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAutoTrigger() {
        syncSettings.config.flatMapLatest { config ->
            val fid = familyService.currentFamily.value?.id
            if (fid == null || !config.autoSync) {
                return@flatMapLatest flowOf<Boolean>()
            }
            val conditions: Flow<Boolean> = combine(
                syncMeta.watchPendingCountByFamily(fid),
                authService.observeAuthState().map { user -> user != null },
                networkMonitor.isOnline,
                networkMonitor.isUnmetered,
            ) { pending: Int, loggedIn: Boolean, online: Boolean, unmetered: Boolean ->
                pending > 0 && loggedIn && online && (!config.wifiOnly || unmetered)
            }
            when (config.syncDelay) {
                SyncDelay.IMMEDIATE -> conditions.filter { it }
                SyncDelay.ON_EXIT -> flowOf()
                else -> conditions.filter { it }.debounce(config.syncDelay.millis)
            }
        }.onEach { doPush() }.launchIn(scope)
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

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
import com.babytracker.core.settings.SettingsStore
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    private val settingsStore: SettingsStore,
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

    private var lastSyncedFamilyId: String? = prefs.getString("sync_last_family", null)

    companion object {
        private const val SYNC_WORK_NAME = "bg_sync"
    }

    fun start() {
        scope.launch {
            settingsStore.awaitReady()
            Timber.tag("Sync").d("SyncTrigger start")
            observeAuth()
            observeFamily()
            observeNetwork()
            observeAutoTrigger()
            observeBgInterval()
        }
    }

    private fun observeAuth() {
        scope.launch {
            combine(
                authService.observeAuthState().map { it?.id },
                authService.observeVerifiedAuthState().map { it?.id },
            ) { cachedId, verifiedId -> cachedId to verifiedId }
                .distinctUntilChanged()
                .collectLatest { (cachedId, verifiedId) ->
                    when {
                        cachedId == null -> familyService.clearSession()
                        cachedId == verifiedId -> {
                            if (familyService.sessionState.value.userId != cachedId) {
                                familyService.restoreCachedForUser(cachedId)
                            }
                            runCatching { familyService.refreshForUser(cachedId) }
                                .onFailure { Timber.tag("Family").e(it, "auth refresh failed") }
                        }
                        else -> familyService.restoreCachedForUser(cachedId)
                    }
                }
        }
    }

    fun onAppBackgrounded() {
        val config = settingsStore.settings.value.sync
        if (!config.syncOnExit) return
        if (!shouldSync(config)) return
        scope.launch {
            try {
                syncEngine.fullSync()
                _lastSyncResult.value = "同步完成"
            } catch (e: Exception) {
                Timber.tag("Sync").e(e, "onAppBackgrounded fullSync failed")
                _lastSyncResult.value = "同步失败"
            }
        }
    }

    private fun observeFamily() {
        scope.launch {
            Timber.tag("Sync").d("observeFamily start")
            familyService.sessionState.map { it.verifiedFamilyForSync?.id }
                .distinctUntilChanged().collect { newId ->
                Timber.tag("Sync").d("observeFamily family=%s", newId)
                syncEngine.currentFamilyId = newId
                if (newId != null) {
                    val isNewFamily = newId != lastSyncedFamilyId
                    if (isNewFamily) {
                        syncEngine.resetLastSync()
                        lastSyncedFamilyId = newId
                        prefs.edit().putString("sync_last_family", newId).apply()
                    }
                    val pendingKey = "sync_pending_marked_$newId"
                    if (!prefs.getBoolean(pendingKey, false)) {
                        // 有表标记失败时不置位一次性标记，下次家庭触发可重试
                        val failures = syncEngine.markExistingPending()
                        if (failures.isEmpty()) {
                            prefs.edit().putBoolean(pendingKey, true).apply()
                        } else {
                            Timber.tag("Sync").w("markExistingPending partial failures=%d, 保留重试机会", failures.size)
                        }
                    }
                    triggerSync()
                    authService.verifiedUserId()?.let {
                        realtimeManager.subscribeAll()
                    }
                } else {
                    realtimeManager.unsubscribe()
                }
            }
        }
    }

    private fun observeNetwork() {
        scope.launch {
            var first = true
            networkMonitor.isOnline.collect { online ->
                if (first) { first = false; return@collect }
                if (online) {
                    authService.verifiedUserId()?.let { userId ->
                        runCatching { familyService.refreshForUser(userId) }
                            .onFailure { Timber.tag("Family").e(it, "network refresh failed") }
                    }
                    if (authService.verifiedUserId() != null && syncEngine.currentFamilyId != null) triggerSync()
                }
            }
        }
    }

    private fun triggerSync() {
        scope.launch {
            try {
                syncEngine.fullSync()
                Timber.tag("Sync").d("auto trigger sync done")
            } catch (e: Exception) {
                Timber.tag("Sync").e(e, "trigger fullSync failed")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAutoTrigger() {
        Timber.tag("Sync").d("observeAutoTrigger start")
        scope.launch {
            PendingChangeNotifier.events.collect {
                val config = settingsStore.settings.value.sync
                val fid = syncEngine.currentFamilyId
                if (fid == null) return@collect
                if (!config.autoSync) { Timber.tag("Sync").d("autoTrigger skip: autoSync off"); return@collect }
                val loggedIn = authService.verifiedUserId() != null
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
            settingsStore.settings.map { it.sync }.distinctUntilChanged().collect { config ->
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
        val user = authService.verifiedUserId()
        if (user == null) return false
        if (!networkMonitor.isOnline.value) return false
        if (config.wifiOnly && !networkMonitor.isUnmetered.value) return false
        return true
    }

    private suspend fun doPush() {
        try {
            // 防抖/推送期间新增事件可能被缓冲溢出丢弃，推送后复查 pending，仍有余量则补推
            var attempts = 0
            while (attempts < 3) {
                attempts++
                syncEngine.push()
                val fid = syncEngine.currentFamilyId ?: return
                if (syncMeta.pendingCount(fid) <= 0) break
                Timber.tag("Sync").d("auto push 仍有 %d 条 pending，补推第 %d 次", syncMeta.pendingCount(fid), attempts)
                if (attempts < 3) kotlinx.coroutines.delay(1_000)
            }
            Timber.tag("Sync").d("auto push done")
        } catch (e: Exception) {
            Timber.tag("Sync").e(e, "auto push failed")
        }
    }
}

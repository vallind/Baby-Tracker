package com.babytracker.core.sync

import org.junit.Test
import java.io.File

/** 同步安全边界静态审计，防止家庭游标和本地消息隔离规则回退。 */
class SyncSafetyStaticAuditTest {
    private val moduleDir = File(System.getProperty("user.dir") ?: ".").let { workingDir ->
        if (File(workingDir, "src/main").isDirectory) workingDir else File(workingDir, "app")
    }
    private val syncEngine = File(moduleDir, "src/main/java/com/babytracker/core/sync/SyncEngine.kt").readText()
    private val realtime = File(moduleDir, "src/main/java/com/babytracker/core/sync/RealtimeManager.kt").readText()
    private val repositories = File(moduleDir, "src/main/java/com/babytracker/core/data/repository/Repositories.kt").readText()
    private val daos = File(moduleDir, "src/main/java/com/babytracker/core/database/dao/Daos.kt").readText()
    private val app = File(moduleDir, "src/main/java/com/babytracker/BabyTrackerApp.kt").readText()
    private val coordinator = File(moduleDir, "src/main/java/com/babytracker/core/sync/SyncCoordinator.kt").readText()
    private val settingsViewModel = File(moduleDir, "src/main/java/com/babytracker/feature/settings/SettingsViewModel.kt").readText()

    @Test
    fun `messages must remain outside cloud sync`() {
        val messageRepository = repositories.substringAfter("class MessageRepositoryImpl")
            .substringBefore("// —— 发育评估")
        assert(!messageRepository.contains("pendingChange"))
        assert(!syncEngine.substringAfter("syncedTables = listOf(").substringBefore(")").contains("messages"))
    }

    @Test
    fun `pull must use family table server cursor`() {
        assert(syncEngine.contains("syncCursor.get(fid, tableName)"))
        assert(syncEngine.contains("gt(\"sync_version\", pageCursor)"))
        assert(syncEngine.contains("SyncCursorEntity(fid, tableName, pageCursor)"))
    }

    @Test
    fun `realtime must filter current family`() {
        assert(realtime.contains("filter(\"family_id\", FilterOperator.EQ, familyId)"))
        assert(realtime.contains("familyId != syncEngine.currentFamilyId"))
    }

    @Test
    fun `conflicts must not be reset to pending in bulk`() {
        assert(!syncEngine.contains("syncStatus='pending' WHERE tableName='\$table' AND syncStatus='conflict'"))
    }

    @Test
    fun `未归属记录不得被当前家庭自动认领`() {
        assert(!syncEngine.contains("assignUnscopedToFamily"))
        assert(!syncEngine.contains("UPDATE babies SET familyId=? WHERE familyId IS NULL"))
        assert(!daos.contains("familyId = :familyId OR familyId IS NULL"))
    }

    @Test
    fun `同步生命周期必须在应用启动而不是设置页启动`() {
        assert(app.contains("get<SyncCoordinator>().start()"))
        assert(coordinator.contains("PeriodicWorkRequestBuilder<SyncWorker>"))
        assert(coordinator.contains("watchPendingCount()"))
        assert(!settingsViewModel.contains("registerNetworkCallback"))
        assert(!settingsViewModel.contains("tryAutoSync"))
    }
}

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
}

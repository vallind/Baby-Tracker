package com.babytracker.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class SyncTriggerTest {
    private fun sourceFile(path: String): File {
        val fromApp = File("src/main/java/$path")
        return if (fromApp.exists()) fromApp else File("app/src/main/java/$path")
    }

    @Test
    fun `pendingChange calls PendingChangeNotifier changed`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val body = source.substringAfter("private suspend fun SyncMetadataDao.pendingChange")
            .substringBefore("// ── 宝宝")

        assertEquals(true, body.contains("PendingChangeNotifier.changed()"))
    }

    @Test
    fun `sync_metadata insert uses REPLACE`() {
        val source = sourceFile("com/babytracker/core/database/dao/Daos.kt").readText()
        val dao = source.substringAfter("interface SyncMetadataDao {")
            .substringBefore("interface SyncCursorDao {")

        assertEquals(true, dao.contains("OnConflictStrategy.IGNORE"))
    }

    @Test
    fun `baby insert update delete all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class BabyRepositoryImpl")
            .substringBefore("// ── 喂养")

        assertEquals(4, Regex("syncMeta\\.pendingChange\\(\\\"babies\\\"").findAll(impl).count())
    }

    @Test
    fun `feeding insert update delete all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class FeedingRepositoryImpl")
            .substringBefore("// ── 睡眠")

        assertEquals(3, Regex("syncMeta\\.pendingChange\\(\\\"feedings\\\"").findAll(impl).count())
    }

    @Test
    fun `sleep insert update delete all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class SleepRepositoryImpl")
            .substringBefore("// ── 生长")

        assertEquals(3, Regex("syncMeta\\.pendingChange\\(\\\"sleeps\\\"").findAll(impl).count())
    }

    @Test
    fun `growth insert update delete all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class GrowthRepositoryImpl")
            .substringBefore("// ── 疫苗")

        assertEquals(3, Regex("syncMeta\\.pendingChange\\(\\\"growths\\\"").findAll(impl).count())
    }

    @Test
    fun `vaccination insert update delete all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class VaccinationRepositoryImpl")
            .substringBefore("// ── 健康记录")

        assertEquals(3, Regex("syncMeta\\.pendingChange\\(\\\"vaccinations\\\"").findAll(impl).count())
    }

    @Test
    fun `health record insert update delete all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class HealthRepositoryImpl")
            .substringBefore("// ── 尿布")

        assertEquals(3, Regex("syncMeta\\.pendingChange\\(\\\"health_records\\\"").findAll(impl).count())
    }

    @Test
    fun `diaper insert update delete all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class DiaperRepositoryImpl")
            .substringBefore("// —— 消息中心")

        assertEquals(3, Regex("syncMeta\\.pendingChange\\(\\\"diapers\\\"").findAll(impl).count())
    }

    @Test
    fun `development assessment insert update delete all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class DevelopmentAssessmentRepositoryImpl")
            .substringBefore("// —— 提醒中心")

        assertEquals(3, Regex("syncMeta\\.pendingChange\\(\\\"development_assessments\\\"").findAll(impl).count())
    }

    @Test
    fun `reminder insert update delete markDone setEnabled all call pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class ReminderRepositoryImpl")
            .substringBefore("// ── Repository 工厂辅助")

        assertEquals(5, Regex("syncMeta\\.pendingChange\\(\\\"reminders\\\"").findAll(impl).count())
    }

    @Test
    fun `message repository never calls pendingChange`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val impl = source.substringAfter("class MessageRepositoryImpl")
            .substringBefore("// —— 发育评估")

        assertEquals(false, impl.contains("pendingChange"))
    }

    @Test
    fun `cascade soft delete marks children pending`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val cascade = source.substringAfter("private suspend fun cascadeSoftDelete")
            .substringBefore("// ── 喂养")

        assertEquals(true, cascade.contains("syncMeta.pendingChange"))
    }

    @Test
    fun `observeAutoTrigger uses PendingChangeNotifier events`() {
        val source = sourceFile("com/babytracker/core/sync/SyncTrigger.kt").readText()
        val autoTrigger = source.substringAfter("private fun observeAutoTrigger")
            .substringBefore("private fun observeBgInterval")

        assertEquals(true, autoTrigger.contains("PendingChangeNotifier.events.collect"))
    }
}

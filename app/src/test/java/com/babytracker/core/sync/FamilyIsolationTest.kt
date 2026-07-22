package com.babytracker.core.sync

import com.babytracker.core.data.Family
import com.babytracker.core.data.FamilySessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.File

class FamilyIsolationTest {
    private val family = Family(id = "family-a", name = "A")

    private fun sourceFile(path: String): File {
        val fromApp = File("src/main/java/$path")
        return if (fromApp.exists()) fromApp else File("app/src/main/java/$path")
    }

    private fun repositoryFile(path: String): File {
        val fromRoot = File(path)
        return if (fromRoot.exists()) fromRoot else File("../$path")
    }

    @Test
    fun `cached family cannot drive cloud sync`() {
        val state = FamilySessionState(
            userId = "user-a",
            families = listOf(family),
            selectedFamily = family,
            sessionVerified = false,
        )

        assertSame(family, state.activeFamily)
        assertNull(state.verifiedFamilyForSync)
    }

    @Test
    fun `verified family can drive sync unless local mode is selected`() {
        val verified = FamilySessionState(
            userId = "user-a",
            families = listOf(family),
            selectedFamily = family,
            sessionVerified = true,
        )
        assertSame(family, verified.verifiedFamilyForSync)

        val local = verified.copy(isLocalMode = true)
        assertNull(local.activeFamily)
        assertNull(local.verifiedFamilyForSync)
    }

    @Test
    fun `baby repository never merges unscoped babies into a family`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val babyRepository = source.substringAfter("class BabyRepositoryImpl").substringBefore("// ── 喂养")

        assertEquals(false, babyRepository.contains("combine("))
        assertEquals(false, babyRepository.contains("scoped + unscoped"))
        assertEquals(true, babyRepository.contains("state.activeFamily?.id?.let(dao::watchByFamily) ?: dao.watchUnscoped()"))
    }

    @Test
    fun `automatic pending scan does not claim unscoped babies`() {
        val source = sourceFile("com/babytracker/core/sync/SyncEngine.kt").readText()
        val pendingScan = source.substringAfter("suspend fun markExistingPending")
            .substringBefore("suspend fun claimUnscopedData")

        assertEquals(false, pendingScan.contains("SET familyId = ? WHERE familyId IS NULL"))
    }

    @Test
    fun `messages never create sync metadata`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val messageRepository = source.substringAfter("class MessageRepositoryImpl")
            .substringBefore("// —— 发育评估")

        assertEquals(false, messageRepository.contains("pendingChange"))
    }

    @Test
    fun `reminder state changes create sync metadata`() {
        val source = sourceFile("com/babytracker/core/data/repository/Repositories.kt").readText()
        val stateChanges = source.substringAfter("override suspend fun markDone")
            .substringBefore("// ── Repository 工厂辅助")

        assertEquals(2, Regex("pendingChange\\(\\\"reminders\\\"").findAll(stateChanges).count())
        assertEquals(2, Regex("updatedAt = nowEpoch").findAll(stateChanges).count())
    }

    @Test
    fun `exit sync does not disable pending change push`() {
        val source = sourceFile("com/babytracker/core/sync/SyncTrigger.kt").readText()
        val autoTrigger = source.substringAfter("private fun observeAutoTrigger")
            .substringBefore("private fun observeBgInterval")

        assertEquals(false, autoTrigger.contains("if (config.syncOnExit) return@collect"))
    }

    @Test
    fun `app version matches latest changelog version`() {
        val build = repositoryFile("app/build.gradle.kts").readText()
        val changelog = repositoryFile("CHANGELOG.md").readText()
        val appVersion = Regex("""versionName\s*=\s*"([^"]+)"""").find(build)?.groupValues?.get(1)
        val changelogVersion = Regex("""### \[([^]]+)]""").find(changelog)?.groupValues?.get(1)

        assertEquals(changelogVersion, appVersion)
    }
}

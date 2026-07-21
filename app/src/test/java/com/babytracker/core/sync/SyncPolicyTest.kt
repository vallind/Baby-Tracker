package com.babytracker.core.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncPolicyTest {
    @Test
    fun `部分成功必须返回部分失败`() {
        val result = SyncRunResult(
            pushed = 2,
            pulled = 3,
            failures = listOf(SyncFailure("feedings", 7, "网络失败")),
        )

        assertEquals(SyncOutcome.PARTIAL_FAILURE, result.outcome)
        assertEquals(5, result.total)
    }

    @Test
    fun `全部失败不能伪装成同步成功`() {
        val result = SyncRunResult(failures = listOf(SyncFailure("sleeps", message = "无权限")))

        assertEquals(SyncOutcome.FAILURE, result.outcome)
    }

    @Test
    fun `页面未全部落库时游标不得推进`() {
        assertEquals(120L, committedSyncCursor(120L, 135L, allApplied = false))
        assertEquals(135L, committedSyncCursor(120L, 135L, allApplied = true))
    }

    @Test
    fun `重试退避增长并在第五档封顶`() {
        assertEquals(5_000L, syncRetryDelay(0))
        assertEquals(160_000L, syncRetryDelay(5))
        assertEquals(160_000L, syncRetryDelay(99))
    }

    @Test
    fun `已有同步记录不得因切换家庭被重新归属`() {
        assertEquals("family-a", resolveSyncFamilyId("family-a", "family-b"))
        assertEquals("family-b", resolveSyncFamilyId(null, "family-b"))
        assertEquals(null, resolveSyncFamilyId(null, null))
    }

    @Test
    fun `非法写入延迟配置回退到两秒`() {
        assertEquals(SyncDelay.TWO_SECONDS, SyncDelay.fromStored("unknown"))
        assertEquals(SyncDelay.ON_BACKGROUND, SyncDelay.fromStored("on_background"))
    }

    @Test
    fun `非法后台周期配置回退到三十分钟`() {
        assertEquals(
            BackgroundSyncInterval.THIRTY_MINUTES,
            BackgroundSyncInterval.fromStored("unknown"),
        )
        assertEquals(BackgroundSyncInterval.OFF, BackgroundSyncInterval.fromStored("off"))
    }
}

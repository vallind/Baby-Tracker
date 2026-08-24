package com.babytracker.core.sync

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SyncEngine 云端孤儿行处理守门。
 *
 * 背景：v9 起 Room 外键强制（AppDatabase_Impl 执行 PRAGMA foreign_keys = ON），
 * 云端孤儿记录（babyId uuid 在本机 babies 不存在 → resolveLocalBabyId 返回 0，
 * baby_id=0 插入违反外键）曾导致 pull 整页失败、feedings 同步永久卡死。
 * 修复：applyRemoteChange 对 SQLiteConstraintException 特判为「跳过 + 推进游标」。
 * 本测试防止该特判被回退（整页失败死循环回归）。
 */
class SyncEngineOrphanAuditTest {
    private fun sourceFile(path: String): File {
        val fromApp = File("src/main/java/$path")
        return if (fromApp.exists()) fromApp else File("app/src/main/java/$path")
    }

    private fun applyRemoteChangeBody(): String {
        val source = sourceFile("com/babytracker/core/sync/SyncEngine.kt").readText()
        val body = source.substringAfter("internal suspend fun applyRemoteChange")
            .substringBefore("private fun getUpdatedAt")
        // 防路径漂移导致的空断言假绿
        assertTrue("applyRemoteChange 源码段为空，路径可能已漂移", body.length > 100)
        return body
    }

    @Test
    fun `外键约束违反必须特判为跳过而非整页失败`() {
        val body = applyRemoteChangeBody()
        assertTrue("必须显式 catch SQLiteConstraintException", body.contains("catch (e: SQLiteConstraintException)"))
        assertTrue("必须优先于通用 Exception catch（特判才生效）", body.indexOf("catch (e: SQLiteConstraintException)") < body.indexOf("catch (e: Exception)"))
        assertTrue("必须记录 WARN 日志说明跳过孤儿行", body.contains("跳过云端孤儿行"))
        // 特判 catch 块内部必须 return true（游标推进），截取块体断言避免命中文件其他 return
        val skipBlock = body.substringAfter("catch (e: SQLiteConstraintException)")
            .substringBefore("catch (e: Exception)")
        assertTrue("特判块内必须 return true（推进游标，避免死循环）", skipBlock.contains("return true"))
    }
}

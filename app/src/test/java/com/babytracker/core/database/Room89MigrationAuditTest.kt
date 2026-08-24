package com.babytracker.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * MIGRATION_8_9 静态审计：孤儿清理策略守门。
 *
 * 背景：8→9 迁移曾对孤儿记录（baby 行已物理删除但记录仍在）直接抛
 * IllegalStateException，导致真机升级后启动即崩且无恢复入口。
 * 本测试防止两类回归：
 *  1. 孤儿处理回退为「抛异常」（必须保持清理 + 日志）
 *  2. 六表 12 步重建被意外简化（列清单 / 建表 SQL / 预演脚本缺失）
 */
class Room89MigrationAuditTest {
    private fun sourceFile(path: String): File {
        val fromApp = File("src/main/java/$path")
        return if (fromApp.exists()) fromApp else File("app/src/main/java/$path")
    }

    private fun migration89Body(): String {
        val source = sourceFile("com/babytracker/core/database/AppDatabase.kt").readText()
        val body = source.substringAfter("private val MIGRATION_8_9")
            .substringBefore("fun get(")
        // 防路径漂移导致的空断言假绿
        assertTrue("MIGRATION_8_9 源码段为空，路径可能已漂移", body.length > 200)
        return body
    }

    @Test
    fun `孤儿记录必须被清理而非抛异常`() {
        val body = migration89Body()
        assertTrue(
            "MIGRATION_8_9 必须包含孤儿物理清理（DELETE FROM \$table WHERE baby_id NOT IN ...）",
            body.contains("DELETE FROM \$table WHERE baby_id NOT IN (SELECT id FROM babies)"),
        )
        assertEquals(
            "迁移不得回退为抛异常（真机启动即崩的根因）",
            false,
            body.contains("throw IllegalStateException"),
        )
    }

    @Test
    fun `六表重建列清单与预演脚本同步存在`() {
        val body = migration89Body()
        val sixTables = listOf("feedings", "sleeps", "growths", "vaccinations", "health_records", "diapers")
        // 源码文件为 CRLF 行尾，先归一化再匹配跨行断言
        val bodyLf = body.replace("\r\n", "\n")
        for (table in sixTables) {
            assertTrue("迁移缺少 $table 重建入口", bodyLf.contains("rebuildWithBabyFk(\n                    db, table = \"$table\""))
            assertTrue("迁移缺少 $table 的 baby_id 索引", body.contains("index_\${table}_baby_id"))
        }
        // JVM 单测工作目录是 app 模块根（见 lessons #14），tools/ 在仓库根
        val previewCandidates = listOf(File("../tools/migrate-8to9-preview.sql"), File("tools/migrate-8to9-preview.sql"))
        val preview = previewCandidates.firstOrNull { it.exists() }
        assertTrue("预演脚本缺失", preview != null)
        val previewText = preview!!.readText()
        assertTrue(
            "预演脚本仍停留在「人工终止」策略，未同步清理方案",
            previewText.contains("DELETE FROM feedings WHERE baby_id NOT IN (SELECT id FROM babies)"),
        )
    }
}

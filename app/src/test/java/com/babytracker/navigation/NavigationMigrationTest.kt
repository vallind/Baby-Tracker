package com.babytracker.navigation

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 导航迁移回归门禁：应用源码禁止再引用 AndroidX Navigation Compose。
 *
 * 注意：JVM 单测工作目录为 app 模块根，路径以 src/ 开头（lessons #14）。
 */
class NavigationMigrationTest {

    @Test
    fun `源码不引用 androidx navigation`() {
        val root = File("src/main/java")
        assertTrue("扫描根目录不存在：$root", root.isDirectory)
        val hits = root.walkTopDown()
            .filter { it.extension == "kt" }
            .filter { it.readText().contains("androidx.navigation.") }
            .map { it.relativeTo(root) }
            .toList()
        assertTrue(
            "发现 AndroidX Navigation 残留引用：$hits",
            hits.isEmpty(),
        )
    }
}

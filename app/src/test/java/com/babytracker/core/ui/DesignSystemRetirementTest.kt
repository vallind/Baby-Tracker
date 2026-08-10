package com.babytracker.core.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 旧设计系统退役门禁：主源码禁止再出现 `com.babytracker.designsystem` 引用。
 *
 * 注意：JVM 单测工作目录为 app 模块根，路径以 src/ 开头（lessons #14）。
 */
class DesignSystemRetirementTest {

    @Test
    fun `主源码不再引用旧设计系统`() {
        val root = File("src/main/java")
        assertTrue("扫描根目录不存在：$root", root.isDirectory)
        val hits = root.walkTopDown()
            .filter { it.extension == "kt" }
            .filter { it.readText().contains("com.babytracker.designsystem") }
            .map { it.relativeTo(root) }
            .toList()
        assertTrue(
            "旧设计系统引用残留：$hits",
            hits.isEmpty(),
        )
    }
}

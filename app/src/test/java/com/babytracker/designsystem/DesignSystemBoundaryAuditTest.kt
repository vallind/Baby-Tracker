package com.babytracker.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * DesignSystem 边界静态审计（Batch 1 守门，架构十原则 #1/#2）：
 * designsystem 下所有 .kt 文件禁止 import core / feature / navigation / androidx.navigation / koin，
 * 保证 DS 是纯 UI 层 —— UI 长什么样它负责，业务/数据/导航/DI 一概不知。
 *
 * 豁免：无。Controller（ThemeController/DensityController）已迁出 designsystem
 * （core/settings），本目录不应再出现任何业务依赖。
 *
 * 路径约定（lessons #14）：JVM 单测工作目录是 app 模块根，相对路径不带 app/ 前缀；
 * 自带"扫描到文件数 > 0"断言，防止路径漂移后静默空转。
 */
class DesignSystemBoundaryAuditTest {

    private val dsDir = File("src/main/java/com/babytracker/designsystem")

    private val forbiddenImportPrefixes = listOf(
        "com.babytracker.core",
        "com.babytracker.feature",
        "com.babytracker.navigation",
        "androidx.navigation",
        "org.koin",
    )

    @Test
    fun `DesignSystem 禁止依赖 core_feature_navigation_koin`() {
        val files = dsDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()

        assertTrue("designsystem 目录未扫描到任何 .kt 文件（路径漂移？）", files.isNotEmpty())

        val violations = files.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("import ") && forbiddenImportPrefixes.any { trimmed.startsWith("import $it") }) {
                    "  ${file.name}:${index + 1}: $trimmed"
                } else {
                    null
                }
            }
        }

        assertTrue(
            "DesignSystem 出现反向依赖（禁止 import core/feature/navigation/koin）：\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
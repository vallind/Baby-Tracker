package com.babytracker.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * app/ui 层边界静态审计（五阶段收敛 Phase 4/5 守门）：
 * `ui/patterns`（业务形态模式层）与 `ui/framework`（app 层 UI 框架）禁止 import
 * core / feature / navigation / androidx.navigation / org.koin —— 与 DesignSystem 同边界
 * （AGENTS.md §3 红线 ⑥，taxonomy §〇.4）。
 *
 * ui → designsystem 是合法消费方向（主题令牌经公开组件与 *Defaults 工厂）；
 * 本测试只守"反向泄漏"（业务/数据/导航/DI 不得渗入 patterns/framework）。
 *
 * 路径约定（lessons #14）：JVM 单测工作目录是 app 模块根，相对路径不带 app/ 前缀；
 * 自带"扫描到文件数 > 0"断言，防止路径漂移后静默空转。
 */
class UiLayerBoundaryAuditTest {

    private val uiDirs = listOf(
        File("src/main/java/com/babytracker/ui"),
    )

    private val forbiddenImportPrefixes = listOf(
        "com.babytracker.core",
        "com.babytracker.feature",
        "com.babytracker.navigation",
        "androidx.navigation",
        "org.koin",
    )

    @Test
    fun `ui 层禁止依赖 core_feature_navigation_koin`() {
        val files = uiDirs.flatMap { dir ->
            dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }
        assertTrue("ui 目录未扫描到任何 .kt 文件（路径漂移？）", files.isNotEmpty())

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
            "app/ui 出现反向依赖（禁止 import core/feature/navigation/koin）：\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
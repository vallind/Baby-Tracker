package com.babytracker.core

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * core 层边界静态审计（依赖架构守门补强）：
 * core 禁止 import feature / navigation —— 依赖方向单向 `feature → core`。
 *
 * 唯一豁免：`core/di/Modules.kt`（Koin 组合根，装配全部 feature ViewModel；
 * 组合根是依赖汇聚点，行业通行的 inject 语义豁免）。
 *
 * 路径约定（lessons #14）：JVM 单测工作目录是 app 模块根，相对路径不带 app/ 前缀；
 * 自带"扫描到文件数 > 0"断言，防止路径漂移后静默空转。
 */
class CoreLayerBoundaryAuditTest {

    private val coreDir = File("src/main/java/com/babytracker/core")

    private val forbiddenImportPrefixes = listOf(
        "com.babytracker.feature",
        "com.babytracker.navigation",
    )

    // 组合根豁免（相对 core 目录的路径，invariantSeparatorsPath 比较）
    private val exemptionRelPaths = setOf("di/Modules.kt")

    @Test
    fun `core 禁止依赖 feature_navigation（组合根 Modules_kt 豁免）`() {
        val files = coreDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
        assertTrue("core 目录未扫描到任何 .kt 文件（路径漂移？）", files.isNotEmpty())

        val violations = files.flatMap { file ->
            if (file.relativeTo(coreDir).invariantSeparatorsPath in exemptionRelPaths) return@flatMap emptyList()
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
            "core 出现反向依赖（禁止 import feature/navigation，仅 di/Modules.kt 组合根豁免）：\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
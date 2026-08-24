package com.babytracker.feature

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Screen 边界静态审计（Batch 2 守门）：
 * feature 下所有 *Screen.kt 文件禁止 import navigation / koin / Repository / Controller，
 * 保证 Screen 只收 viewModel + state + 回调（Route 负责 DI 与导航）。
 *
 * 豁免：Route 文件（允许 koin/nav）、Page 文件（如 FamilyPage，后续批次收敛）、
 * ViewModel 与辅助文件不在扫描范围内（按文件名 *Screen.kt 精确匹配）。
 *
 * 路径约定（lessons #14）：JVM 单测工作目录是 app 模块根，相对路径不带 app/ 前缀；
 * 自带"扫描到文件数 > 0"断言，防止路径漂移后静默空转。
 */
class ScreenBoundaryAuditTest {

    private val featureDir = File("src/main/java/com/babytracker/feature")

    private val forbiddenImportPrefixes = listOf(
        "androidx.navigation",
        "org.koin",
        "com.babytracker.core.data.repository",
        "com.babytracker.core.util.BabyController",
        "com.babytracker.core.auth.AuthService",
        "com.babytracker.core.backup.BackupManager",
        "com.babytracker.core.settings.ThemeController",
        "com.babytracker.core.settings.DensityController",
    )

    @Test
    fun `Screen 文件禁止直接依赖 navigation_koin_repository_controller`() {
        val screens = featureDir.walkTopDown()
            .filter { it.isFile && it.name.endsWith("Screen.kt") }
            .toList()

        assertTrue("feature 目录未扫描到任何 *Screen.kt（路径漂移？）", screens.isNotEmpty())

        val violations = screens.flatMap { file ->
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
            "Screen 出现反向依赖（禁止 import navigation/koin/repository/controller）：\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
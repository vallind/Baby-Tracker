package com.babytracker.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AppButton 语义 API 审计（Batch 5 守门）：
 * designsystem 之外（feature / navigation / core）禁止给 AppButton 传裸 token 覆盖参数
 * （height/cornerRadius/fontSize/fontWeight/iconSize/containerColor/contentColor/disabled*），
 * 颜色/圆角/字号一律由 variant + size 从 ButtonTokens 派生 —— 业务代码不得绕过 Design Token。
 *
 * 实现说明：定位 `AppButton(` 调用，向后扫描其参数块（直到缩进 ≤ 调用缩进的收尾 `)`），
 * 检查其中是否出现被禁参数名。单行调用同样检查本行。
 *
 * 路径约定（lessons #14）：JVM 单测工作目录是 app 模块根，相对路径不带 app/ 前缀；
 * 自带"扫描到文件数 > 0"断言，防止路径漂移后静默空转。
 */
class AppButtonApiAuditTest {

    private val roots = listOf(
        File("src/main/java/com/babytracker/feature"),
        File("src/main/java/com/babytracker/navigation"),
        File("src/main/java/com/babytracker/core"),
    )

    private val bannedParams = listOf(
        "height =", "cornerRadius =", "fontSize =", "fontWeight =", "iconSize =",
        "containerColor =", "contentColor =",
        "disabledContainerColor =", "disabledContentColor =",
    )

    @Test
    fun `feature_navigation_core 禁止给 AppButton 传裸 token 参数`() {
        val files = roots.flatMap { root ->
            root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }
        assertTrue("扫描目录未命中任何 .kt 文件（路径漂移？）", files.isNotEmpty())

        val violations = mutableListOf<String>()
        files.forEach { file ->
            val lines = file.readLines()
            lines.forEachIndexed { index, line ->
                if ("AppButton(" in line) {
                    val callIndent = line.takeWhile { it == ' ' }.length
                    // 单行调用：参数与被禁名同处一行
                    bannedParams.forEach { if (line.contains(it)) violations += "  ${file.name}:${index + 1}: ${line.trim()}" }
                    // 多行调用：向后扫描参数块（缩进更深，直到收尾右括号）
                    var j = index + 1
                    var scanned = 0
                    while (j < lines.size && scanned < 25) {
                        val l = lines[j]
                        val indent = l.takeWhile { it == ' ' }.length
                        if (l.contains(")") && indent <= callIndent) break
                        if (indent > callIndent) {
                            bannedParams.forEach { p ->
                                if (l.contains(p)) violations += "  ${file.name}:${j + 1}: ${l.trim()}"
                            }
                        }
                        scanned++
                        j++
                    }
                }
            }
        }

        assertTrue(
            "业务代码绕过 ButtonTokens（AppButton 裸 token 参数）：\n" + violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
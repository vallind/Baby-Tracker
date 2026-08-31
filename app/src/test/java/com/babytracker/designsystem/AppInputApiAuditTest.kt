package com.babytracker.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AppInput 语义 API 审计（五阶段收敛 Phase 2 守门）：
 *
 * 1. 库自身：`AppInput` 声明签名禁止裸 token 几何参数
 *    （height/cornerRadius/fontSize/borderWidth/borderWidthFocus/iconSize），
 *    必须由 `size: AppInputSize` 语义轴经 InputDefaults → InputTokens 分档派生
 *    （四层 API 契约 taxonomy §六）。
 * 2. 库外：feature / navigation / core 禁止给 `AppInput(` 传裸 token 参数
 *    （与 AppButtonApiAuditTest 同模式）。
 *
 * 路径约定（lessons #14）：JVM 单测工作目录是 app 模块根，相对路径不带 app/ 前缀；
 * 自带"扫描到文件数 > 0"断言，防止路径漂移后静默空转。
 */
class AppInputApiAuditTest {

    // 声明侧（库自身）：参数名 + 类型
    private val bannedSignatureParams = listOf(
        "height: Dp", "cornerRadius: Dp", "fontSize: TextUnit",
        "borderWidth: Dp", "borderWidthFocus: Dp", "iconSize: Dp",
    )

    // 调用侧（库外）：命名参数写法
    private val bannedCallParams = listOf(
        "height =", "cornerRadius =", "fontSize =", "borderWidth =",
        "borderWidthFocus =", "iconSize =",
    )

    private val auditedRoots = listOf(
        File("src/main/java/com/babytracker/feature"),
        File("src/main/java/com/babytracker/navigation"),
        File("src/main/java/com/babytracker/core"),
    )

    private fun appInputSignature(): List<String> {
        val inputFile = File("src/main/java/com/babytracker/designsystem/components/input/Input.kt")
        assertTrue("Input.kt 未找到（路径漂移？）", inputFile.isFile)
        val lines = inputFile.readLines()
        val start = lines.indexOfFirst { "fun AppInput(" in it }
        assertTrue("Input.kt 未找到 AppInput 声明（路径漂移？）", start >= 0)
        val signature = mutableListOf<String>()
        for (k in start until lines.size) {
            signature += lines[k]
            if (lines[k].trim().endsWith(") {")) break
        }
        return signature
    }

    @Test
    fun `AppInput 声明签名禁止裸 token 几何参数且提供 size 轴`() {
        val signature = appInputSignature().joinToString("\n")

        val violations = bannedSignatureParams.filter { p ->
            signature.lines().any { it.contains(p) && !it.trim().startsWith("*") }
        }
        assertTrue(
            "AppInput 签名仍外露裸 token 几何参数（应收敛为 size 轴）：$violations\n$signature",
            violations.isEmpty(),
        )
        assertTrue(
            "AppInput 签名缺少 size: AppInputSize 语义轴（四层 API 契约）：\n$signature",
            signature.lines().any { it.contains("size: AppInputSize") },
        )
    }

    @Test
    fun `feature_navigation_core 禁止给 AppInput 传裸 token 参数`() {
        val files = auditedRoots.flatMap { root ->
            root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }
        assertTrue("扫描目录未命中任何 .kt 文件（路径漂移？）", files.isNotEmpty())

        val violations = mutableListOf<String>()
        files.forEach { file ->
            val lines = file.readLines()
            lines.forEachIndexed { index, line ->
                if ("AppInput(" in line) {
                    val callIndent = line.takeWhile { it == ' ' }.length
                    bannedCallParams.forEach { if (line.contains(it)) violations += "  ${file.name}:${index + 1}: ${line.trim()}" }
                    var j = index + 1
                    var scanned = 0
                    while (j < lines.size && scanned < 30) {
                        val l = lines[j]
                        val indent = l.takeWhile { it == ' ' }.length
                        if (l.contains(")") && indent <= callIndent) break
                        if (indent > callIndent) {
                            bannedCallParams.forEach { p ->
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
            "业务代码绕过 InputTokens（AppInput 裸 token 参数）：\n" + violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
package com.babytracker.core.util

import java.io.File
import kotlin.system.exitProcess

/** 令牌审计违规项 */
data class AuditViolation(val file: String, val rule: String, val detail: String)

/**
 * 令牌化静态审计检查器 — 纯 Kotlin 文本扫描，Gradle 任务与 JVM 单测双路复用。
 * 规则编号与 ThemeTokenizationStaticAuditTest 对应。
 *
 * @param kotlinRoot 源码根目录（如 app/src/main/java）
 * @param themeRelDir theme 包相对路径（如 com/babytracker/designsystem/theme，M3 桥接豁免）
 * @param componentsRelDir components 包相对路径（如 com/babytracker/designsystem/components，Defaults 扫描范围）
 * @param componentTokensFile AppComponentTokens.kt 文件（注册字段校验）
 */
object TokenAuditChecker {

    const val RULE_DEFAULTS_MISSING_TOKENS = "DefaultsMissingLocalAppComponentTokens"
    const val RULE_DEFAULTS_HARDCODED_COLOR = "DefaultsHardcodedColor"
    const val RULE_DEFAULTS_IMPORTS_COLORS = "DefaultsImportsLocalAppColors"
    const val RULE_COMPONENT_M3_TOKEN = "ComponentLayerM3Token"
    const val RULE_TOKENS_MISSING_REGISTRATION = "ComponentTokensMissingRegistration"

    private val hardcodedColorPatterns = listOf(
        "Color.Black" to "hardcoded Color.Black",
        "Color.White" to "hardcoded Color.White",
        "Color(0xFF" to "hardcoded Color",
    )

    private val m3TokenImports = listOf(
        "import androidx.compose.material3.Typography",
        "import androidx.compose.material3.ColorScheme",
        "import androidx.compose.material3.Shapes",
    )

    // 检查器自身源码含扫描模式串（如 "import androidx.compose.material3.Typography"），规则 4 须豁免本文件，否则自查必报。
    // 按相对 kotlinRoot 的路径匹配而非文件名，避免误豁免其他同名文件（lessons #14/#17）。
    private const val SELF_FILE_REL_PATH = "com/babytracker/core/util/TokenAuditChecker.kt"

    fun audit(
        kotlinRoot: File,
        themeRelDir: String,
        componentsRelDir: String,
        componentTokensFile: File,
    ): List<AuditViolation> {
        val violations = mutableListOf<AuditViolation>()
        val componentsDir = File(kotlinRoot, componentsRelDir.replace('.', '/'))
        val defaultsFiles = if (componentsDir.exists()) {
            componentsDir.walkTopDown().filter { it.name.endsWith("Defaults.kt") }.toList()
        } else emptyList()
        // 防呆（lessons #14）：扫描为空必须报错，禁止静默假绿
        if (defaultsFiles.isEmpty()) {
            violations.add(AuditViolation(componentsDir.path, "ScanEmpty", "Defaults 扫描为空，路径可能漂移"))
        }
        for (file in defaultsFiles) {
            val text = file.readText()
            if (!text.contains("LocalAppComponentTokens")) {
                violations.add(AuditViolation(file.path, RULE_DEFAULTS_MISSING_TOKENS, "Defaults 未走组件令牌"))
            }
            for ((pattern, label) in hardcodedColorPatterns) {
                if (text.contains(pattern)) {
                    violations.add(AuditViolation(file.path, RULE_DEFAULTS_HARDCODED_COLOR, "包含 $label"))
                }
            }
            if (text.contains(Regex("import.*LocalAppColors"))) {
                violations.add(AuditViolation(file.path, RULE_DEFAULTS_IMPORTS_COLORS, "直接 import LocalAppColors"))
            }
        }
        val themeRelPath = themeRelDir.replace('.', '/')
        val scanned = kotlinRoot.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".kt") }
            .filterNot { it.path.contains(themeRelPath) }
            .filterNot { it.path == File(kotlinRoot, SELF_FILE_REL_PATH).path }
            .toList()
        // 防呆（lessons #14）：规则 4 扫描为空必须报错，禁止静默假绿
        if (scanned.isEmpty()) {
            violations.add(AuditViolation(kotlinRoot.path, "ScanEmpty", "规则 4 扫描为空，路径可能漂移"))
        }
        for (file in scanned) {
            val text = file.readText()
            val hitM3Import = m3TokenImports.any { text.contains(it) }
            val hitMaterialTheme = text.contains("MaterialTheme.typography") ||
                text.contains("MaterialTheme.colorScheme") || text.contains("MaterialTheme.shapes")
            if (hitM3Import || hitMaterialTheme) {
                violations.add(AuditViolation(file.path, RULE_COMPONENT_M3_TOKEN, "组件层暴露 M3 令牌/主题类型"))
            }
        }
        val required = listOf("divider", "surface", "snackbarHost", "emptyState")
        if (componentTokensFile.exists()) {
            val text = componentTokensFile.readText()
            for (field in required) {
                if (!text.contains("val $field:")) {
                    violations.add(AuditViolation(componentTokensFile.path, RULE_TOKENS_MISSING_REGISTRATION, "缺少令牌字段 $field"))
                }
            }
        } else {
            // 防呆（lessons #14/#17）：文件缺失必须报错，禁止静默假绿（否则注册校验随路径漂移失效）
            violations.add(AuditViolation(componentTokensFile.path, "ScanEmpty", "AppComponentTokens.kt 不存在，注册校验已失效"))
        }
        return violations
    }
}

/**
 * Gradle JavaExec 入口（mainClass = TokenAuditCheckerKt），与 JUnit 测试共用 audit 逻辑。
 * 参数按序：kotlinRoot、themeRelDir、componentsRelDir、componentTokensFile。
 * 违规时逐条打印 `文件: 规则 — 详情` 并 exitProcess(1)；参数数量不符打印用法并 exit 2。
 */
fun main(args: Array<String>) {
    if (args.size != 4) {
        System.err.println(
            "用法: themeTokenAudit <kotlinRoot> <themeRelDir> <componentsRelDir> <componentTokensFile>\n" +
                "示例: ./gradlew themeTokenAudit",
        )
        exitProcess(2)
    }
    val violations = TokenAuditChecker.audit(
        kotlinRoot = File(args[0]),
        themeRelDir = args[1],
        componentsRelDir = args[2],
        componentTokensFile = File(args[3]),
    )
    if (violations.isEmpty()) {
        println("themeTokenAudit: 未发现违规")
        return
    }
    println("themeTokenAudit: 发现 ${violations.size} 处违规:")
    violations.forEach { println("${it.file}: ${it.rule} — ${it.detail}") }
    exitProcess(1)
}

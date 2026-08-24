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
    const val RULE_FEATURE_GENERIC_CARD = "FeatureLayerGenericCard"

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

    /**
     * 规则 6：feature 层禁止新定义通用卡片容器（*Card 命名的 Composable）。
     * 卡片能力统一由 designsystem 的 AppCard 基座承担，业务形态用 variant/slots 组合表达；
     * 私有 *Card 是"影子设计系统"的种子（AI 辅助开发下会按页面数累积）。
     *
     * 白名单为存量债（G 批收编后逐文件移除）：白名单按文件豁免，文件内新增同模式函数同样会被拦截。
     */
    private val featureCardDefRegex = Regex("""\bfun\s+\w*Card\w*\s*\(""")

    private val featureCardBaselineRelPaths = setOf(
        "com/babytracker/feature/ai/AiChatScreen.kt",
        "com/babytracker/feature/ai/AiSettingsScreen.kt",
        "com/babytracker/feature/development/DevelopmentAssessmentScreen.kt",
        "com/babytracker/feature/diaper/DiaperListScreen.kt",
        "com/babytracker/feature/health/HealthScreen.kt",
        "com/babytracker/feature/home/HomeScreen.kt",
        "com/babytracker/feature/message/MessageScreen.kt",
        "com/babytracker/feature/reminder/ReminderScreen.kt",
        "com/babytracker/feature/settings/SettingsComponents.kt",
        "com/babytracker/feature/sleep/SleepListScreen.kt",
        "com/babytracker/feature/stats/StatsScreen.kt",
        "com/babytracker/feature/vaccination/VaccinationListScreen.kt",
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
        // theme 豁免必须用 File 构造目录再前缀比较：File.path 按平台分隔符（Windows 反斜杠）生成，
        // 直接把正斜杠包路径与 it.path 做 contains 在 Windows 上永不命中，豁免静默失效（见 lessons #23）
        val themeDir = File(kotlinRoot, themeRelPath)
        val scanned = kotlinRoot.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".kt") }
            .filterNot { it.path.startsWith(themeDir.path) }
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
        // 规则 6：feature 层通用卡片容器守门。目录豁免用 File 前缀比较（lessons #24），
        // 相对路径统一 invariantSeparatorsPath（正斜杠），与白名单同源比较。
        val featureDir = File(kotlinRoot, "com/babytracker/feature")
        if (!featureDir.exists()) {
            violations.add(AuditViolation(featureDir.path, "ScanEmpty", "规则 6 扫描为空，feature 目录不存在或路径漂移"))
        } else {
            val featureFiles = featureDir.walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .toList()
            if (featureFiles.isEmpty()) {
                violations.add(AuditViolation(featureDir.path, "ScanEmpty", "规则 6 扫描为空，路径可能漂移"))
            }
            for (file in featureFiles) {
                if (file.extension != "kt") continue
                val relPath = file.relativeTo(kotlinRoot).invariantSeparatorsPath
                if (relPath in featureCardBaselineRelPaths) continue
                val text = file.readText()
                val matches = featureCardDefRegex.findAll(text).map { it.value }.toList()
                if (matches.isNotEmpty()) {
                    violations.add(
                        AuditViolation(file.path, RULE_FEATURE_GENERIC_CARD, "feature 层定义卡片容器: $matches；请改用 AppCard variant/slots 组合"),
                    )
                }
            }
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

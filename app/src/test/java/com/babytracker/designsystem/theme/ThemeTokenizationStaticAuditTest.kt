package com.babytracker.designsystem.theme

import org.junit.Test
import java.io.File

/**
 * 令牌化静态审计 — 守护组件层与令牌体系边界。
 *
 * 规则：
 * 1. 组件 Defaults 必须通过 LocalAppComponentTokens 取令牌，禁止直接读取本地色值；
 * 2. Defaults 不应直接引入 Color.Black / Color.White 或内联的 alpha 状态颜色；
 * 3. 新增组件 Defaults 必须注册进 AppComponentTokens 聚合容器；
 * 4. M3 令牌类型（Typography/ColorScheme/Shapes）只允许在 theme 层桥接，
 *    组件层不得直接导入或在 MaterialTheme 上读取。
 *
 * 注意：JVM 单测工作目录为 app 模块根（非仓库根），文件路径以 src/ 开头。
 */
class ThemeTokenizationStaticAuditTest {

    private val defaultsFiles: List<File> by lazy {
        val root = File("src/main/java/com/babytracker/designsystem/components")
        root.walkTopDown().filter { it.name.endsWith("Defaults.kt") }.toList()
    }

    // 防呆（lessons #14）：walkTopDown 对不存在的路径静默返回空流，必须断言扫描命中文件数 > 0，防止路径漂移后审计假绿
    private fun assertScanNonEmpty(files: List<File>, scanDesc: String) {
        assert(files.isNotEmpty()) { "静态审计扫描为空：$scanDesc 路径可能已漂移，审计已失效" }
    }

    @Test
    fun `all Defaults files should reference LocalAppComponentTokens`() {
        assertScanNonEmpty(defaultsFiles, "components Defaults 目录")
        val violations = defaultsFiles.filterNot { file ->
            file.readText().contains("LocalAppComponentTokens")
        }
        assert(violations.isEmpty()) {
            "Defaults files not routing through componentThemes:\n" +
                violations.joinToString("\n") { "  ${it.name}" }
        }
    }

    @Test
    fun `Defaults should not directly reference Color constants`() {
        assertScanNonEmpty(defaultsFiles, "components Defaults 目录")
        val suspicious = listOf(
            "Color.Black" to "hardcoded Color.Black",
            "Color.White" to "hardcoded Color.White",
            "Color(0xFF" to "hardcoded Color",
        )
        val violations = mutableListOf<String>()
        for (file in defaultsFiles) {
            val text = file.readText()
            for ((pattern, label) in suspicious) {
                if (text.contains(pattern, ignoreCase = false)) {
                    violations.add("${file.name} contains $label")
                }
            }
        }
        assert(violations.isEmpty()) {
            "Defaults should route colors through componentThemes, not hardcode:\n" +
                violations.joinToString("\n") { "  $it" }
        }
    }

    @Test
    fun `Defaults should not import LocalAppColors`() {
        assertScanNonEmpty(defaultsFiles, "components Defaults 目录")
        val violations = defaultsFiles.filter { file ->
            file.readText().contains("import.*LocalAppColors".toRegex())
        }
        assert(violations.isEmpty()) {
            "Defaults importing LocalAppColors directly:\n" +
                violations.joinToString("\n") { "  ${it.name}" }
        }
    }

    @Test
    fun `新组件 Defaults 应被 AppComponentTokens 覆盖`() {
        val componentTokensFile = File("src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt").readText()
        val required = listOf("divider", "surface", "snackbarHost", "emptyState")
        val missing = required.filterNot { componentTokensFile.contains("val $it:") }
        assert(missing.isEmpty()) { "AppComponentTokens 缺少组件令牌字段: $missing" }
    }

    @Test
    fun `组件层不应导入 M3 令牌与主题类型`() {
        // theme 层是唯一允许桥接 M3 的位置
        val root = File("src/main/java/com/babytracker")
        val m3TokenImports = listOf(
            "import androidx.compose.material3.Typography",
            "import androidx.compose.material3.ColorScheme",
            "import androidx.compose.material3.Shapes",
        )
        val scannedFiles = root.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".kt") }
            .filterNot { it.path.contains("/designsystem/theme/") }
            .toList()
        assertScanNonEmpty(scannedFiles, "组件层与 feature 层 .kt 文件")
        val violations = scannedFiles
            .filter { file ->
                val text = file.readText()
                m3TokenImports.any { text.contains(it) } ||
                    text.contains("MaterialTheme.typography") ||
                    text.contains("MaterialTheme.colorScheme") ||
                    text.contains("MaterialTheme.shapes")
            }
        assert(violations.isEmpty()) {
            "组件层暴露 M3 令牌/主题类型（token 只允许在 theme 层桥接 M3）:\n" +
                violations.joinToString("\n") { it.path }
        }
    }
}

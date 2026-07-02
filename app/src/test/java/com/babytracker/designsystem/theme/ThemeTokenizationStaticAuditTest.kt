package com.babytracker.designsystem.theme

import org.junit.Test
import java.io.File

/**
 * 令牌化静态审计 — 守护 Defaults 文件迁移边界。
 *
 * 规则：
 * 1. 组件 Defaults 必须通过 LocalAppComponentTokens 路由，不直接读 AppColors
 * 2. Defaults 不应硬编码 Color.Black / Color.White 或内联 Color(0xFF...)
 * 3. Defaults 不应直接 import LocalAppColors
 * 4. 组件令牌中的动画时长应从 AppMotion 派生，不硬编码
 * 5. 组件令牌中的圆角值应使用 shapes.scaled()，不硬编码 dp
 */
class ThemeTokenizationStaticAuditTest {

    private val defaultsFiles: List<File> by lazy {
        val root = File("app/src/main/java/com/babytracker/designsystem/components")
        root.walkTopDown().filter { it.name.endsWith("Defaults.kt") }.toList()
    }

    private val tokensFiles: List<File> by lazy {
        val root = File("app/src/main/java/com/babytracker/designsystem/theme")
        root.walkTopDown().filter { it.name == "AppComponentTokens.kt" }.toList()
    }

    @Test
    fun `all Defaults files should reference LocalAppComponentTokens`() {
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
        val violations = defaultsFiles.filter { file ->
            file.readText().contains("import.*LocalAppColors".toRegex())
        }
        assert(violations.isEmpty()) {
            "Defaults importing LocalAppColors directly:\n" +
                violations.joinToString("\n") { "  ${it.name}" }
        }
    }

    @Test
    fun `token animation durations should use AppMotion not hardcoded values`() {
        val tokensText = tokensFiles.joinToString("\n") { it.readText() }
        val hardcodedDurationPattern = """animationDurationMs\s*=\s*\d+""".toRegex()
        val matches = hardcodedDurationPattern.findAll(tokensText)
        val violations = matches.filter { !it.value.contains("motion.duration") }.toList()
        assert(violations.isEmpty()) {
            "Token animationDurationMs should derive from AppMotion, not hardcode:\n" +
                violations.joinToString("\n") { "  $it" }
        }
    }

    @Test
    fun `token cornerRadius should use shapes scaled not hardcoded dp`() {
        val tokensText = tokensFiles.joinToString("\n") { it.readText() }
        // Check for companion object default() functions that set cornerRadius without scaled()
        // 허용: shapes.scaled(...) 호출이 포함된 경우
        // 위반: cornerRadius = N.dp (직접 하드코딩)
        val hardcodedRadius = """cornerRadius\s*=\s*\d+\.dp""".toRegex()
        val matches = hardcodedRadius.findAll(tokensText)
        val violations = matches.filter { match ->
            // Check if this line or nearby uses scaled()
            val lineStart = maxOf(0, match.range.first - 60)
            val context = tokensText.substring(lineStart, match.range.last + 1)
            !context.contains("scaled(")
        }.toList()
        assert(violations.isEmpty()) {
            "Token cornerRadius should use shapes.scaled(), not hardcoded dp:\n" +
                violations.joinToString("\n") { "  ${it.value}" }
        }
    }
}

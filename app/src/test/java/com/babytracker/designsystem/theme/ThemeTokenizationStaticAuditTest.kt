package com.babytracker.designsystem.theme

import org.junit.Test
import java.io.File

/**
 * 令牌化静态审计 — 守护 Defaults 文件迁移边界。
 *
 * 规则：
 * 1. 组件 Defaults 不应直接读取 PaletteTheme.colors；
 *    主要视觉样式应通过 PaletteTheme.componentThemes 路由。
 * 2. Defaults 不应直接引入 Color.Black / Color.White 或内联的 alpha 状态颜色。
 */
class ThemeTokenizationStaticAuditTest {

    private val defaultsFiles: List<File> by lazy {
        val root = File("app/src/main/java/com/babytracker/designsystem/components")
        root.walkTopDown().filter { it.name.endsWith("Defaults.kt") }.toList()
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
}

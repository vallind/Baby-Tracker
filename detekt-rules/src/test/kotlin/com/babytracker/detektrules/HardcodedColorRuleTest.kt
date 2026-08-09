package com.babytracker.detektrules

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HardcodedColorRuleTest {

    private val rule = HardcodedColorRule(TestConfig())

    private fun lintWith(content: String) = rule.lint(content)

    @Test
    fun `组件层 Color 十六进制字面量应命中`() {
        val findings = lintWith(
            """
            import androidx.compose.ui.graphics.Color
            val c = Color(0xFF000000)
            """.trimIndent(),
        )
        assertThat(findings).hasSize(1)
        assertThat(findings.first().message).contains("0xFF")
    }

    @Test
    fun `Color 命名属性 Black 应命中`() {
        val findings = lintWith(
            """
            import androidx.compose.ui.graphics.Color
            val c = Color.Black
            """.trimIndent(),
        )
        assertThat(findings).hasSize(1)
        assertThat(findings.first().message).contains("Color.Black")
    }

    @Test
    fun `Color 命名属性 White 应命中`() {
        val findings = lintWith(
            """
            import androidx.compose.ui.graphics.Color
            val c = Color.White.copy(alpha = 0.25f)
            """.trimIndent(),
        )
        assertThat(findings).hasSize(1)
        assertThat(findings.first().message).contains("Color.White")
    }

    @Test
    fun `未 import Compose Color 不命中`() {
        val findings = lintWith(
            """
            val c = Color(0xFF000000)
            val d = Color.Black
            """.trimIndent(),
        )
        assertThat(findings).isEmpty()
    }

    @Test
    fun `import 其他 Color 类型不命中`() {
        val findings = lintWith(
            """
            import java.awt.Color
            val c = Color(0xFF000000)
            """.trimIndent(),
        )
        assertThat(findings).isEmpty()
    }

    @Test
    fun `theme 包白名单内硬编码颜色不命中`() {
        val file = compileContentForTest(
            """
            package com.babytracker.designsystem.theme

            import androidx.compose.ui.graphics.Color

            val darkBg = Color(0xFF12121F)
            val scrim = Color.Black.copy(alpha = 0.32f)
            """.trimIndent(),
            "com/babytracker/designsystem/theme/AppColors.kt",
        )
        assertThat(rule.lint(file)).isEmpty()
    }

    @Test
    fun `全限定 Color 引用应命中`() {
        val findings = lintWith(
            """
            import androidx.compose.ui.graphics.Color
            val c = androidx.compose.ui.graphics.Color.Black
            val d = androidx.compose.ui.graphics.Color(0xFF000000)
            """.trimIndent(),
        )
        assertThat(findings).hasSize(2)
    }

    @Test
    fun `非 Color 的十六进制字面量不命中`() {
        val findings = lintWith(
            """
            import androidx.compose.ui.graphics.Color
            val mask = 0xFF000000.toInt()
            val c = Color(1, 2, 3)
            """.trimIndent(),
        )
        assertThat(findings).isEmpty()
    }
}

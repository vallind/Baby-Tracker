package com.babytracker.detektrules

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TokenBypassRuleTest {

    private val rule = TokenBypassRule(TestConfig())

    // —— 规则 1：Defaults 直读核心令牌（components 包内 Defaults.kt import LocalAppColors）——

    @Test
    fun `components 包 Defaults 文件 import LocalAppColors 应命中`() {
        val file = compileContentForTest(
            """
            package com.babytracker.designsystem.components.chip

            import androidx.compose.ui.unit.Dp
            import androidx.compose.ui.unit.dp
            import com.babytracker.designsystem.theme.LocalAppColors

            object ChipDefaults {
                val x: Dp = 8.dp
            }
            """.trimIndent(),
            "com/babytracker/designsystem/components/chip/ChipDefaults.kt",
        )
        val findings = rule.lint(file)
        assertThat(findings).hasSize(1)
        assertThat(findings.first().message).contains("LocalAppColors")
    }

    @Test
    fun `components 子包 Defaults 文件 import LocalAppColors 应命中`() {
        val file = compileContentForTest(
            """
            package com.babytracker.designsystem.components.recordcard

            import com.babytracker.designsystem.theme.LocalAppColors

            object RecordCardDefaults
            """.trimIndent(),
            "com/babytracker/designsystem/components/recordcard/RecordCardDefaults.kt",
        )
        assertThat(rule.lint(file)).hasSize(1)
    }

    @Test
    fun `组件实现文件 import LocalAppColors 不命中`() {
        val file = compileContentForTest(
            """
            package com.babytracker.designsystem.components.chip

            import androidx.compose.material3.FilterChipDefaults
            import com.babytracker.designsystem.theme.LocalAppColors

            @Composable
            fun AppFilterChip() {
                val c = LocalAppColors.current
                val colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.primary)
            }
            """.trimIndent(),
            "com/babytracker/designsystem/components/chip/Chip.kt",
        )
        assertThat(rule.lint(file)).isEmpty()
    }

    @Test
    fun `Defaults 文件未 import LocalAppColors 不命中`() {
        val file = compileContentForTest(
            """
            package com.babytracker.designsystem.components.button

            import androidx.compose.ui.unit.dp

            object ButtonDefaults
            """.trimIndent(),
            "com/babytracker/designsystem/components/button/ButtonDefaults.kt",
        )
        assertThat(rule.lint(file)).isEmpty()
    }

    @Test
    fun `components 包外的 Defaults 文件 import LocalAppColors 不命中`() {
        val file = compileContentForTest(
            """
            package com.babytracker.feature.home

            import com.babytracker.designsystem.theme.LocalAppColors

            object HomeDefaults
            """.trimIndent(),
            "com/babytracker/feature/home/HomeDefaults.kt",
        )
        assertThat(rule.lint(file)).isEmpty()
    }

    @Test
    fun `import 其他同名类型不命中`() {
        val file = compileContentForTest(
            """
            package com.babytracker.designsystem.components.chip

            import com.babytracker.theme.LocalAppColors

            object ChipDefaults
            """.trimIndent(),
            "com/babytracker/designsystem/components/chip/ChipDefaults.kt",
        )
        assertThat(rule.lint(file)).isEmpty()
    }

    // —— 规则 2：M3 主题直用（MaterialTheme.colorScheme/typography/shapes，theme 包豁免）——

    @Test
    fun `MaterialTheme colorScheme 访问应命中`() {
        val findings = rule.lint(
            """
            val c = MaterialTheme.colorScheme.primary
            """.trimIndent(),
        )
        assertThat(findings).hasSize(1)
        assertThat(findings.first().message).contains("colorScheme")
    }

    @Test
    fun `MaterialTheme typography 访问应命中`() {
        val findings = rule.lint(
            """
            val style = MaterialTheme.typography.bodyLarge
            """.trimIndent(),
        )
        assertThat(findings).hasSize(1)
        assertThat(findings.first().message).contains("typography")
    }

    @Test
    fun `MaterialTheme shapes 访问应命中`() {
        val findings = rule.lint(
            """
            val shape = MaterialTheme.shapes.medium
            """.trimIndent(),
        )
        assertThat(findings).hasSize(1)
        assertThat(findings.first().message).contains("shapes")
    }

    @Test
    fun `全限定 MaterialTheme 引用应命中`() {
        val findings = rule.lint(
            """
            val c = androidx.compose.material3.MaterialTheme.colorScheme.background
            """.trimIndent(),
        )
        assertThat(findings).hasSize(1)
    }

    @Test
    fun `theme 包文件访问 MaterialTheme 不命中`() {
        val file = compileContentForTest(
            """
            package com.babytracker.designsystem.theme

            val fg = MaterialTheme.colorScheme.onSurface
            """.trimIndent(),
            "com/babytracker/designsystem/theme/AppTokens.kt",
        )
        assertThat(rule.lint(file)).isEmpty()
    }

    @Test
    fun `非 MaterialTheme 接收者不命中`() {
        val findings = rule.lint(
            """
            val c = CustomTheme.colorScheme.primary
            """.trimIndent(),
        )
        assertThat(findings).isEmpty()
    }
}

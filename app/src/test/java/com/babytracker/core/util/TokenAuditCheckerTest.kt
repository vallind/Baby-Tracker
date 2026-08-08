package com.babytracker.core.util

import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TokenAuditCheckerTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun write(relPath: String, content: String): File =
        File(tmp.root, relPath).apply { parentFile?.mkdirs(); writeText(content) }

    @Test
    fun `违规样本应被全部检出`() {
        write(
            "com/babytracker/designsystem/components/bad/BadDefaults.kt",
            "package com.babytracker.designsystem.components.bad\n" +
                "import com.babytracker.designsystem.theme.LocalAppColors\n" +
                "object BadDefaults { val c = Color(0xFF000000) }\n",  // 无 LocalAppComponentTokens + 硬编码 + 引 LocalAppColors
        )
        write(
            "com/babytracker/feature/bad/BadScreen.kt",
            "package com.babytracker.feature.bad\n" +
                "import androidx.compose.material3.Typography\n",  // 组件层导入 M3 令牌
        )
        val componentTokens = write(
            "com/babytracker/designsystem/theme/AppComponentTokens.kt",
            "object AppComponentTokens { /* 无 divider 字段 */ }\n",
        )
        val violations = TokenAuditChecker.audit(
            kotlinRoot = tmp.root,
            themeRelDir = "com/babytracker/designsystem/theme",
            componentsRelDir = "com/babytracker/designsystem/components",
            componentTokensFile = componentTokens,
        )
        val ruleNames = violations.map { it.rule }
        assertTrue("Defaults 缺 LocalAppComponentTokens 未检出", ruleNames.contains("DefaultsMissingLocalAppComponentTokens"))
        assertTrue("Defaults 硬编码颜色未检出", ruleNames.contains("DefaultsHardcodedColor"))
        assertTrue("Defaults 引 LocalAppColors 未检出", ruleNames.contains("DefaultsImportsLocalAppColors"))
        assertTrue("组件层 M3 令牌未检出", ruleNames.contains("ComponentLayerM3Token"))
        assertTrue("新组件令牌未注册未检出", ruleNames.contains("ComponentTokensMissingRegistration"))
    }

    @Test
    fun `合规样本应零违规且 theme 层豁免`() {
        write(
            "com/babytracker/designsystem/components/good/GoodDefaults.kt",
            "package com.babytracker.designsystem.components.good\n" +
                "import com.babytracker.designsystem.theme.LocalAppComponentTokens\n" +
                "object GoodDefaults { fun color() = LocalAppComponentTokens.current.button.containerColor }\n",
        )
        write(
            "com/babytracker/designsystem/theme/Theme.kt",
            "package com.babytracker.designsystem.theme\n" +
                "import androidx.compose.material3.Typography\n",  // theme 层豁免
        )
        val componentTokens = write(
            "com/babytracker/designsystem/theme/AppComponentTokens.kt",
            "object AppComponentTokens { val divider: DividerTokens; val surface: SurfaceTokens; val snackbarHost: SnackbarHostTokens; val emptyState: EmptyStateTokens }\n",
        )
        val violations = TokenAuditChecker.audit(
            kotlinRoot = tmp.root,
            themeRelDir = "com/babytracker/designsystem/theme",
            componentsRelDir = "com/babytracker/designsystem/components",
            componentTokensFile = componentTokens,
        )
        assertTrue("合规样本出现违规: ${violations.joinToString { "${it.file}:${it.rule}" }}", violations.isEmpty())
    }
}

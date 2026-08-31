package com.babytracker.designsystem.theme

import com.babytracker.core.util.AuditViolation
import com.babytracker.core.util.TokenAuditChecker
import org.junit.Test
import java.io.File

/**
 * 令牌化静态审计 — 守护组件层与令牌体系边界。
 *
 * 规则（与 TokenAuditChecker 常量一一对应）：
 * 1. `DefaultsMissingLocalAppComponentTokens` — 组件 Defaults 必须通过 LocalAppComponentTokens 取令牌；
 * 2. `DefaultsHardcodedColor` — Defaults 不应直接引入 Color.Black / Color.White / Color(0xFF...)；
 * 3. `DefaultsImportsLocalAppColors` — Defaults 不应直接 import LocalAppColors（跳过组件令牌消费层）；
 * 4. `ComponentLayerM3Token` — M3 令牌类型（Typography/ColorScheme/Shapes）只允许在 theme 层桥接，
 *    组件层不得直接导入或在 MaterialTheme 上读取；
 * 5. `ComponentTokensMissingRegistration` — 新增组件令牌必须注册进 AppComponentTokens 聚合容器。
 *
 * 扫描逻辑统一走 TokenAuditChecker（与 Gradle 任务 themeTokenAudit 双路复用，禁止各自实现）。
 * 防呆（lessons #14/#17）：检查器内置 ScanEmpty 违规，扫描范围为空或 AppComponentTokens.kt 缺失时
 * 以违规形式暴露，测试不会假绿。
 * 注意：JVM 单测工作目录为 app 模块根（非仓库根），文件路径以 src/ 开头。
 */
class ThemeTokenizationStaticAuditTest {

    private val violations: List<AuditViolation> by lazy {
        TokenAuditChecker.audit(
            kotlinRoot = File("src/main/java"),
            themeRelDir = "com/babytracker/designsystem/theme",
            componentsRelDir = "com/babytracker/designsystem/components",
            componentTokensFile = File("src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt"),
            // 四层架构 Patterns 层（app/ui/patterns）：Defaults 工厂与库内同规则守门
            extraDefaultsRelDirs = listOf("com/babytracker/ui/patterns"),
        )
    }

    private fun assertNoViolations(scopeDesc: String) {
        assert(violations.isEmpty()) {
            "$scopeDesc 发现违规:\n" +
                violations.joinToString("\n") { "  ${it.file}: ${it.rule} — ${it.detail}" }
        }
    }

    @Test
    fun `all Defaults files should reference LocalAppComponentTokens`() {
        assertNoViolations("Defaults 应通过 LocalAppComponentTokens 取令牌")
    }

    @Test
    fun `Defaults should not directly reference Color constants`() {
        assertNoViolations("Defaults 不应硬编码颜色")
    }

    @Test
    fun `Defaults should not import LocalAppColors`() {
        assertNoViolations("Defaults 不应直接 import LocalAppColors")
    }

    @Test
    fun `新组件 Defaults 应被 AppComponentTokens 覆盖`() {
        assertNoViolations("新组件 Defaults 应注册进 AppComponentTokens")
    }

    @Test
    fun `组件层不应导入 M3 令牌与主题类型`() {
        assertNoViolations("组件层不应导入 M3 令牌与主题类型")
    }
}

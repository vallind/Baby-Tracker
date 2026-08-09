package com.babytracker.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

/**
 * 拦截两处令牌绕过 —— 与 TokenAuditChecker 规则 3/4 同源双守的 AST 版：
 *
 * 命中形态 1（Defaults 直读核心令牌）：
 * - components 包内以 Defaults.kt 结尾的文件 import `com.babytracker.designsystem.theme.LocalAppColors`
 *   （Defaults 跳过组件令牌直读核心语义色，绕过 AppComponentTokens 消费层）
 * - 组件实现文件（非 Defaults）import LocalAppColors 属合法消费，不命中
 *
 * 命中形态 2（M3 主题直用）：
 * - `MaterialTheme.colorScheme|typography|shapes` 属性访问（KtDotQualifiedExpression），
 *   含全限定 `androidx.compose.material3.MaterialTheme.xxx` 写法
 *
 * 豁免：
 * - 路径含 `designsystem/theme` 的文件（主题桥接与令牌定义处合法使用 M3 主题）
 */
class TokenBypassRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "TokenBypass",
        severity = Severity.Warning,
        description = "拦截 Defaults 直读核心令牌与 M3 主题直用",
        debt = Debt.FIVE_MINS,
    )


    companion object {
        private const val LOCAL_APP_COLORS_FQ_NAME = "com.babytracker.designsystem.theme.LocalAppColors"
        private const val COMPONENTS_PACKAGE_SEGMENT = "components"
        private const val DEFAULTS_FILE_SUFFIX = "Defaults.kt"
        private const val THEME_PATH = "designsystem/theme"
        private val M3_THEME_PROPS = setOf("colorScheme", "typography", "shapes")
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        if (importDirective.importedFqName?.asString() != LOCAL_APP_COLORS_FQ_NAME) return
        val file = importDirective.containingKtFile
        // 仅命中 components 包的 Defaults 文件（与 TokenAuditChecker 扫描范围一致）
        if (!file.packageFqName.asString().contains(COMPONENTS_PACKAGE_SEGMENT)) return
        if (!file.virtualFilePath.endsWith(DEFAULTS_FILE_SUFFIX)) return
        report(
            CodeSmell(
                issue,
                Entity.from(importDirective),
                "Defaults 直读核心令牌：禁止 import LocalAppColors，请走 LocalAppComponentTokens",
            ),
        )
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val property = (expression.selectorExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (property !in M3_THEME_PROPS) return
        if (!isMaterialTheme(expression.receiverExpression)) return
        if (isThemeExempt(expression)) return
        report(
            CodeSmell(
                issue,
                Entity.from(expression),
                "组件层禁止直用 MaterialTheme.$property，请改用设计系统令牌",
            ),
        )
    }

    /** 接收者链最后一段是否为 MaterialTheme（兼容全限定写法） */
    private fun isMaterialTheme(receiver: KtExpression): Boolean {
        val name = when (receiver) {
            is KtNameReferenceExpression -> receiver.getReferencedName()
            is KtDotQualifiedExpression ->
                (receiver.selectorExpression as? KtNameReferenceExpression)?.getReferencedName()
            else -> null
        }
        return name == "MaterialTheme"
    }

    private fun isThemeExempt(element: KtElement): Boolean =
        element.containingKtFile.virtualFilePath.contains(THEME_PATH)
}

package com.babytracker.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

/**
 * 拦截组件层/feature 层的硬编码颜色，强制走设计系统令牌。
 *
 * 命中形态（以「绑定 import」判定层）：
 * - `Color(0xFF...)` 十六进制字面量调用（KtCallExpression，参数含 0xFF 常量）
 * - `Color.Black` / `Color.White` 属性引用（KtDotQualifiedExpression）
 * - 全限定写法 `androidx.compose.ui.graphics.Color(0xFF...)` / `...Color.Black` 同样命中
 *
 * 豁免：
 * - 文件未 import `androidx.compose.ui.graphics.Color` → 跳过（core 非 UI 层、其他同名 Color 类型自然豁免；
 *   通配 import `androidx.compose.ui.graphics.*` 不识别，属已知盲区）
 * - 文件路径含 `designsystem/theme` → 跳过（AppColors/derive 等令牌定义处的合法默认值白名单）
 */
class HardcodedColorRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "HardcodedColor",
        severity = Severity.Warning,
        description = "组件层禁止硬编码颜色，应使用设计系统令牌",
        debt = Debt.FIVE_MINS,
    )

    companion object {
        private const val COMPOSE_COLOR_FQ_NAME = "androidx.compose.ui.graphics.Color"
        private const val THEME_PATH = "designsystem/theme"
        private val NAMED_COLORS = setOf("Black", "White")
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text ?: return
        if (callee != "Color" && callee != COMPOSE_COLOR_FQ_NAME) return
        val hasHexArgument = expression.valueArguments.any { argument ->
            (argument.getArgumentExpression() as? KtConstantExpression)
                ?.text
                ?.startsWith("0xFF") == true
        }
        if (!hasHexArgument) return
        if (isExempt(expression)) return
        report(CodeSmell(issue, Entity.from(expression), "硬编码颜色 Color(0xFF...)，请改用设计令牌"))
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val property = (expression.selectorExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (property !in NAMED_COLORS) return
        val receiver = expression.receiverExpression.text
        if (receiver != "Color" && receiver != COMPOSE_COLOR_FQ_NAME) return
        if (isExempt(expression)) return
        report(CodeSmell(issue, Entity.from(expression), "硬编码颜色 Color.$property，请改用设计令牌"))
    }

    private fun isExempt(element: KtElement): Boolean {
        val file = element.containingKtFile
        return file.virtualFilePath.contains(THEME_PATH) || !importsComposeColor(file)
    }

    private fun importsComposeColor(file: KtFile): Boolean =
        file.importDirectives.any { it.importedFqName?.asString() == COMPOSE_COLOR_FQ_NAME }
}

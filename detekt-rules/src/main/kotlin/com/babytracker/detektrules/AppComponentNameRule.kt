package com.babytracker.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject

/**
 * POC 验证用最小规则：拦截类名不以 App 前缀开头的类。
 * 只用于验证 detekt 自定义规则 API 链路（ServiceLoader 加载 → PSI 访问 → 报告），
 * 具体拦截规则由后续 Task 4/5 设计。
 */
class AppComponentNameRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "AppComponentName",
        severity = Severity.Style,
        description = "类名必须以 App 前缀开头",
        debt = Debt.FIVE_MINS,
    )

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (classOrObject !is KtClass) return
        val name = classOrObject.name ?: return
        if (!name.startsWith("App")) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(classOrObject),
                    "类名 $name 未以 App 前缀开头",
                )
            )
        }
    }
}

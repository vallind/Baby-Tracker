package com.babytracker.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * 自定义规则集注册入口：detekt 通过 ServiceLoader 扫描
 * META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider 加载。
 */
class AppComponentRuleSetProvider : RuleSetProvider {

    override val ruleSetId: String = "baby-tracker-rules"

    override fun instance(config: Config): RuleSet =
        RuleSet(ruleSetId, listOf(AppComponentNameRule(config)))
}

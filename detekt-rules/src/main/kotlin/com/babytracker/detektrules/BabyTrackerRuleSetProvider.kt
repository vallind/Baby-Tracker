package com.babytracker.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * 自定义规则集注册入口：detekt 通过 ServiceLoader 扫描
 * META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider 加载。
 * 规则是否激活由 config/detekt/detekt.yml 逐条决定（1.23 起未列出即默认不激活）。
 */
class BabyTrackerRuleSetProvider : RuleSetProvider {

    override val ruleSetId: String = "baby-tracker-rules"

    override fun instance(config: Config): RuleSet =
        RuleSet(ruleSetId, listOf(HardcodedColorRule(config)))
}

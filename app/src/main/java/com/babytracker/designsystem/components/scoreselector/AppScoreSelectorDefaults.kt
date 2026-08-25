package com.babytracker.designsystem.components.scoreselector

import androidx.compose.runtime.Composable
import com.babytracker.designsystem.theme.LocalAppComponentTokens
import com.babytracker.designsystem.theme.ScoreSelectorTokens

/**
 * 评分选择器默认视觉 — 全部经 [LocalAppComponentTokens] 的 scoreSelector 令牌组取值。
 * 禁止直读核心颜色令牌或硬编码色值（审计规则 DefaultsHardcodedColor / DefaultsImportsLocalAppColors）。
 */
object AppScoreSelectorDefaults {

    /** 整组令牌访问入口（组件内部统一从这里取值） */
    @Composable
    fun tokens(): ScoreSelectorTokens = LocalAppComponentTokens.current.scoreSelector
}

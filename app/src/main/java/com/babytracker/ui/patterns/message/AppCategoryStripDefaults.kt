package com.babytracker.ui.patterns.message

import androidx.compose.runtime.Composable
import com.babytracker.designsystem.theme.CategoryStripTokens
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 分类统计条默认视觉 — 全部经 [LocalAppComponentTokens] 的 categoryStrip 令牌组取值。
 * 禁止直读核心颜色令牌或硬编码色值（审计规则 DefaultsHardcodedColor / DefaultsImportsLocalAppColors）。
 */
object AppCategoryStripDefaults {

    /** 整组令牌访问入口（组件内部统一从这里取值） */
    @Composable
    fun tokens(): CategoryStripTokens = LocalAppComponentTokens.current.categoryStrip
}
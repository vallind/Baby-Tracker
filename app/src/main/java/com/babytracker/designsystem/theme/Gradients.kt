package com.babytracker.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 渐变 Brush 工具集 — 所有 CTA 按钮 / FAB / 进度条 / 摘要卡 / 强调元素统一使用。
 *
 * 「柔和奶油 + 多彩分区」：主色渐变统一向白提亮一档（保持白字可读），
 * 分区渐变（feeding/sleep/diaper/growth/reminder）供各业务模块取用。
 *
 * 用法：
 *   Box(Modifier.background(Gradients.primary(c), shape)) { ... }
 */
object Gradients {

    /** 同色相向白提亮的渐变末档 */
    private fun Color.lighten(weight: Float): Color = Color(
        red * (1 - weight) + 1f * weight,
        green * (1 - weight) + 1f * weight,
        blue * (1 - weight) + 1f * weight,
        alpha = 1f,
    )

    fun primary(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.primary.lighten(0.28f)),
    )

    fun primaryVertical(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primary, c.secondary),
    )

    fun primarySoft(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primaryScale.shade100, c.pageBackground),   // 粉彩→奶油
    )

    fun sleepHeader(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.secondary, c.secondary.lighten(0.18f)),
    )

    fun progress(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.tertiary),
    )

    fun chartArea(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primary.copy(alpha = 0.22f), Color.Transparent),
    )

    // —— 宝宝追踪专属渐变 ——

    /** 首页顶部背景渐变（主色粉彩 → 奶油，柔和氛围） */
    fun pageHeader(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primaryContainer, c.pageBackground),
    )

    /** 今日概览卡片渐变（品牌蓝 → 提亮蓝） */
    fun overviewCard(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.primary.lighten(0.30f)),
    )

    /** 功能图标背景渐变（琥珀暖底） */
    fun iconBgWarm(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.warning.copy(alpha = 0.15f), c.warning),
    )

    /** 生长曲线渐变（绿 → 透明，健康生长意象） */
    fun growthChart(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.success.copy(alpha = 0.30f), Color.Transparent),
    )

    /** 提醒卡片渐变（琥珀 → 提亮琥珀） */
    fun reminderCard(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.warning, c.warning.lighten(0.20f)),
    )

    /** 时间轴竖线渐变（divider → 透明） */
    fun timelineLine(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.divider, Color.Transparent),
    )

    /** 尿布汇总大卡渐变（青 → 提亮青，干净清新意象） */
    fun diaperSummary(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.tertiary, c.tertiary.lighten(0.18f)),
    )

    // —— 多彩分区渐变（喂养/睡眠/尿布/生长/提醒/AI） ——

    /** 喂养珊瑚红渐变 */
    fun feeding(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.danger, c.danger.lighten(0.22f)),
    )

    /** 睡眠紫渐变 */
    fun sleep(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.secondary, c.secondary.lighten(0.22f)),
    )

    /** 尿布青渐变 */
    fun diaper(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.tertiary, c.tertiary.lighten(0.22f)),
    )

    /** 生长绿渐变 */
    fun growth(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.success, c.success.lighten(0.22f)),
    )

    /** 提醒琥珀渐变 */
    fun reminder(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.warning, c.warning.lighten(0.22f)),
    )

    /** AI 紫渐变 */
    fun ai(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.secondary, c.secondary.lighten(0.30f)),
    )
}
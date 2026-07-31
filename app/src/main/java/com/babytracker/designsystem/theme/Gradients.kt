package com.babytracker.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 渐变 Brush 工具集 — 所有 CTA 按钮 / FAB / 进度条 / 强调元素统一使用。
 *
 * 用法：
 *   Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
 *          modifier = Modifier.background(Gradients.primary(c), shape)) { ... }
 */
object Gradients {
    fun primary(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.primary.copy(alpha = 0.75f)),
    )

    fun primaryVertical(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primary, c.secondary),
    )

    fun primarySoft(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primaryContainer, c.pageBackground),
    )

    fun sleepHeader(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.secondary, c.secondary.copy(alpha = 0.8f)),
    )

    fun progress(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.tertiary),
    )

    fun chartArea(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primary.copy(alpha = 0.25f), Color.Transparent),
    )

    // —— 宝宝追踪专属渐变 ——

    /** 首页顶部背景渐变（主色极淡染 → 页面背景，克制的色彩氛围） */
    fun pageHeader(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.bgHover, c.pageBackground),
    )

    /** 今日概览卡片渐变（蓝 → 浅蓝，强调今日数据） */
    fun overviewCard(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.primary.copy(alpha = 0.85f)),
    )

    /** 功能图标背景渐变（温暖母婴风，橙色淡背景） */
    fun iconBgWarm(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.warning.copy(alpha = 0.15f), c.warning),
    )

    /** 生长曲线渐变（绿 → 透明，健康生长意象） */
    fun growthChart(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.success.copy(alpha = 0.3f), Color.Transparent),
    )

    /** 提醒卡片渐变（橙 → 深橙，温馨提示） */
    fun reminderCard(c: AppColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.warning.copy(alpha = 0.8f), c.warning),
    )

    /** 时间轴竖线渐变（divider → 透明） */
    fun timelineLine(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.divider, Color.Transparent),
    )

    /** 尿布汇总大卡渐变（青绿 → 深青，干净清新意象） */
    fun diaperSummary(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.tertiary, c.tertiary.copy(alpha = 0.75f)),
    )
}

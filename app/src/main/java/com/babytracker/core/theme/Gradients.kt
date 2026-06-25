package com.babytracker.core.theme

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
    fun primary(c: ThemeColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.primary.copy(alpha = 0.75f)),
    )

    fun primaryVertical(c: ThemeColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primary, c.purple),
    )

    fun primarySoft(c: ThemeColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primaryLight, c.bg),
    )

    fun sleepHeader(): Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81)),
    )

    fun progress(c: ThemeColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.cyan),
    )

    fun chartArea(c: ThemeColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primary.copy(alpha = 0.25f), Color.Transparent),
    )

    // —— 宝宝追踪专属渐变 ——

    /** 首页顶部背景渐变（浅蓝 → 更浅蓝，营造柔和氛围） */
    fun pageHeader(c: ThemeColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primaryLight, c.bg),
    )

    /** 今日概览卡片渐变（蓝 → 浅蓝，强调今日数据） */
    fun overviewCard(c: ThemeColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.blue.copy(alpha = 0.85f)),
    )

    /** 功能图标背景渐变（温暖母婴风，橙色淡背景） */
    fun iconBgWarm(c: ThemeColors): Brush = Brush.verticalGradient(
        colors = listOf(c.accent.copy(alpha = 0.15f), c.accentLight),
    )

    /** 生长曲线渐变（绿 → 透明，健康生长意象） */
    fun growthChart(c: ThemeColors): Brush = Brush.verticalGradient(
        colors = listOf(c.green.copy(alpha = 0.3f), Color.Transparent),
    )

    /** 提醒卡片渐变（橙 → 深橙，温馨提示） */
    fun reminderCard(c: ThemeColors): Brush = Brush.horizontalGradient(
        colors = listOf(c.accent.copy(alpha = 0.8f), c.accent),
    )

    /** 时间轴竖线渐变（divider → 透明） */
    fun timelineLine(c: ThemeColors): Brush = Brush.verticalGradient(
        colors = listOf(c.divider, Color.Transparent),
    )
}

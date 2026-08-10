package com.babytracker.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import io.elyon.kmp.theme.Colors

/**
 * 渐变 Brush 工具集 — 所有 CTA 按钮 / FAB / 进度条 / 强调元素统一使用。
 *
 * 用法：
 *   Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
 *          modifier = Modifier.background(Gradients.primary(c), shape)) { ... }
 */
object Gradients {
    fun primary(c: Colors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.primary.copy(alpha = 0.75f)),
    )

    fun primaryVertical(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.primary, c.secondaryContainer),
    )

    fun primarySoft(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.primaryContainer, c.background),
    )

    fun sleepHeader(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.secondaryContainer, c.secondaryContainer.copy(alpha = 0.8f)),
    )

    fun progress(c: Colors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.tertiaryContainer),
    )

    fun chartArea(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.primary.copy(alpha = 0.25f), Color.Transparent),
    )

    // —— 宝宝追踪专属渐变 ——

    /** 首页顶部背景渐变（浅蓝 → 更浅蓝，营造柔和氛围） */
    fun pageHeader(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.primaryContainer, c.background),
    )

    /** 今日概览卡片渐变（蓝 → 浅蓝，强调今日数据） */
    fun overviewCard(c: Colors): Brush = Brush.horizontalGradient(
        colors = listOf(c.primary, c.primary.copy(alpha = 0.85f)),
    )

    /** 功能图标背景渐变（温暖母婴风，橙色淡背景） */
    fun iconBgWarm(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.tertiaryContainer.copy(alpha = 0.15f), c.tertiaryContainer),
    )

    /** 生长曲线渐变（绿 → 透明，健康生长意象） */
    fun growthChart(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.tertiaryContainer.copy(alpha = 0.5f), Color.Transparent),
    )

    /** 提醒卡片渐变（橙 → 深橙，温馨提示） */
    fun reminderCard(c: Colors): Brush = Brush.horizontalGradient(
        colors = listOf(c.tertiaryContainer.copy(alpha = 0.8f), c.tertiaryContainer),
    )

    /** 时间轴竖线渐变（divider → 透明） */
    fun timelineLine(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.dividerLine, Color.Transparent),
    )

    /** 尿布汇总大卡渐变（青绿 → 深青，干净清新意象） */
    fun diaperSummary(c: Colors): Brush = Brush.verticalGradient(
        colors = listOf(c.tertiaryContainer, c.tertiaryContainer.copy(alpha = 0.75f)),
    )
}

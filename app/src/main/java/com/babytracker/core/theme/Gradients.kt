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
}

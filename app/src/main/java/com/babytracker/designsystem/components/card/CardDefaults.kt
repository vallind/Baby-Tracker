package com.babytracker.designsystem.components.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 卡片默认值 —— 全部从 AppComponentTokens.card 读取，禁止硬编码颜色（审计门禁）
 *
 * 优先级模型：显式参数 > CardDefaults（令牌） > 回退值
 */
object CardDefaults {

    /** 按变体解析颜色组（Palette 的 per-variant 颜色组模式） */
    @Composable
    fun colors(variant: CardVariant): CardColors {
        val t = LocalAppComponentTokens.current.card
        return when (variant) {
            CardVariant.Filled -> CardColors(
                containerColor = t.filledContainerColor,
                contentColor = t.filledContentColor,
            )
            CardVariant.Elevated -> CardColors(
                containerColor = t.elevatedContainerColor,
                contentColor = t.elevatedContentColor,
                elevation = t.elevation,
            )
            CardVariant.Outlined -> CardColors(
                containerColor = t.outlinedContainerColor,
                contentColor = t.outlinedContentColor,
                borderColor = t.outlinedBorderColor,
                borderWidth = t.outlinedBorderWidth,
            )
            CardVariant.Transparent -> CardColors(
                containerColor = Color.Transparent,
                contentColor = t.transparentContentColor,
            )
        }
    }

    @Composable
    fun cornerRadius(): Dp = LocalAppComponentTokens.current.card.cornerRadius

    /**
     * 尺寸档位 → 内容边距：Compact/Medium/Large 映射 spacing 三档。
     * 走 spacing 而非独立令牌字段，密度体系缩放自动生效。
     */
    @Composable
    fun contentPadding(size: CardSize): Dp {
        val s = LocalAppSpacing.current
        return when (size) {
            CardSize.Compact -> s.sm
            CardSize.Medium -> LocalAppComponentTokens.current.card.innerPadding
            CardSize.Large -> s.lg
        }
    }

    @Composable
    fun selectedBorderColor(): Color = LocalAppComponentTokens.current.card.selectedBorderColor

    @Composable
    fun selectedBorderWidth(): Dp = LocalAppComponentTokens.current.card.selectedBorderWidth

    @Composable
    fun disabledAlpha(): Float = LocalAppComponentTokens.current.card.disabledAlpha
}

/**
 * 卡片颜色覆盖组 —— 调用方整体逃生口（对标 Palette ButtonColors/CardColors）。
 * 未指定的字段回落到 variant 解析值；仅限真正需要偏离设计的场景使用。
 */
data class CardColors(
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val borderColor: Color = Color.Unspecified,
    val borderWidth: Dp = Dp.Unspecified,
    val elevation: Dp = Dp.Unspecified,
)

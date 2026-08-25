package com.babytracker.designsystem.components.iconbutton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 图标按钮默认值 — 全部从组件令牌读取（颜色一律经组件令牌层派生，审计门禁）。
 */
object IconButtonDefaults {
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.iconButton.iconSize
    @Composable fun tintColor(): Color = LocalAppComponentTokens.current.iconButton.tintColor

    // B 批 variant 轴颜色组
    @Composable fun filledContainerColor(): Color = LocalAppComponentTokens.current.iconButton.filledContainerColor
    @Composable fun filledContentColor(): Color = LocalAppComponentTokens.current.iconButton.filledContentColor
    @Composable fun tonalContainerColor(): Color = LocalAppComponentTokens.current.iconButton.tonalContainerColor
    @Composable fun tonalContentColor(): Color = LocalAppComponentTokens.current.iconButton.tonalContentColor
    @Composable fun outlinedContentColor(): Color = LocalAppComponentTokens.current.iconButton.outlinedContentColor
    @Composable fun outlinedBorderColor(): Color = LocalAppComponentTokens.current.iconButton.outlinedBorderColor

    /** 带底变体的容器尺寸：随图标尺寸派生（图标直径的 2 倍） */
    fun containerSize(iconSize: Dp): Dp = iconSize * 2f

    /** 各变体默认前景色（tint 参数的按变体回落值） */
    @Composable
    fun contentColorFor(variant: IconButtonVariant): Color = when (variant) {
        IconButtonVariant.Standard -> tintColor()
        IconButtonVariant.Filled -> filledContentColor()
        IconButtonVariant.Tonal -> tonalContentColor()
        IconButtonVariant.Outlined -> outlinedContentColor()
    }
}

package com.babytracker.designsystem.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens
import com.babytracker.designsystem.theme.LocalAppControl

object ButtonDefaults {
    /** 高度随尺寸档位：小/默认/大（默认档 = ButtonTokens 基准，其余映射 AppControlTokens） */
    @Composable fun height(size: ButtonSize): Dp = when (size) {
        ButtonSize.Small -> LocalAppControl.current.small.height
        ButtonSize.Medium -> LocalAppComponentTokens.current.button.height
        ButtonSize.Large -> LocalAppControl.current.large.height
    }

    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.button.cornerRadius

    @Composable fun fontSize(size: ButtonSize): TextUnit = when (size) {
        ButtonSize.Small -> LocalAppControl.current.small.fontSize
        ButtonSize.Medium -> LocalAppComponentTokens.current.button.fontSize
        ButtonSize.Large -> LocalAppControl.current.large.fontSize
    }

    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.button.fontWeight

    @Composable fun iconSize(size: ButtonSize): Dp = when (size) {
        ButtonSize.Small -> LocalAppControl.current.small.iconSize
        ButtonSize.Medium -> LocalAppComponentTokens.current.button.iconSize
        ButtonSize.Large -> LocalAppControl.current.large.iconSize
    }

    @Composable fun disabledAlpha(): Float = LocalAppComponentTokens.current.button.disabledAlpha
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.button.containerColor
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.button.contentColor
    @Composable fun disabledContainerColor(): Color = LocalAppComponentTokens.current.button.disabledContainerColor
    @Composable fun disabledContentColor(): Color = LocalAppComponentTokens.current.button.disabledContentColor
    @Composable fun tonalContainerColor(): Color = LocalAppComponentTokens.current.button.tonalContainerColor
    @Composable fun tonalContentColor(): Color = LocalAppComponentTokens.current.button.tonalContentColor
    @Composable fun secondaryContentColor(): Color = LocalAppComponentTokens.current.button.secondaryContentColor
    @Composable fun textContentColor(): Color = LocalAppComponentTokens.current.button.textContentColor
    @Composable fun dangerContainerColor(): Color = LocalAppComponentTokens.current.button.dangerContainerColor
    @Composable fun dangerContentColor(): Color = LocalAppComponentTokens.current.button.dangerContentColor

    /** loading 转圈描边宽（State/Motion 轴参照） */
    @Composable fun loadingStrokeWidth(): Dp = LocalAppComponentTokens.current.button.loadingStrokeWidth

    /** Outline 变体默认描边宽 */
    @Composable fun outlineBorderWidth(): Dp = LocalAppComponentTokens.current.button.outlineBorderWidth
}
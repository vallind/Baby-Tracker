package com.babytracker.designsystem.components.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 输入框几何/视觉预设（L3 Token 消费层）。
 *
 * 四层 API 契约（taxonomy §六）：消费者通过 `size: AppInputSize` 语义轴获取
 * Small/Medium/Large 三档几何；本工厂是唯一读令牌的地方，组件签名不再外露
 * height/cornerRadius/fontSize 等裸 token 参数。
 */
object InputDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.input.height
    @Composable fun height(size: AppInputSize): Dp = when (size) {
        AppInputSize.Small -> LocalAppComponentTokens.current.input.heightSmall
        AppInputSize.Medium -> LocalAppComponentTokens.current.input.height
        AppInputSize.Large -> LocalAppComponentTokens.current.input.heightLarge
    }
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.input.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.input.fontSize
    @Composable fun fontSize(size: AppInputSize): TextUnit = when (size) {
        AppInputSize.Small -> LocalAppComponentTokens.current.input.fontSizeSmall
        AppInputSize.Medium -> LocalAppComponentTokens.current.input.fontSize
        AppInputSize.Large -> LocalAppComponentTokens.current.input.fontSizeLarge
    }
    @Composable fun borderWidth(): Dp = LocalAppComponentTokens.current.input.borderWidth
    @Composable fun borderWidthFocus(): Dp = LocalAppComponentTokens.current.input.borderWidthFocus
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.input.iconSize
    @Composable fun iconSize(size: AppInputSize): Dp = when (size) {
        AppInputSize.Small -> LocalAppComponentTokens.current.input.iconSizeSmall
        AppInputSize.Medium -> LocalAppComponentTokens.current.input.iconSize
        AppInputSize.Large -> LocalAppComponentTokens.current.input.iconSizeLarge
    }
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.input.containerColor
    @Composable fun focusedBorderColor(): Color = LocalAppComponentTokens.current.input.focusedBorderColor
    @Composable fun unfocusedBorderColor(): Color = LocalAppComponentTokens.current.input.unfocusedBorderColor
    @Composable fun errorBorderColor(): Color = LocalAppComponentTokens.current.input.errorBorderColor
    @Composable fun placeholderColor(): Color = LocalAppComponentTokens.current.input.placeholderColor
    @Composable fun cursorColor(): Color = LocalAppComponentTokens.current.input.cursorColor
}
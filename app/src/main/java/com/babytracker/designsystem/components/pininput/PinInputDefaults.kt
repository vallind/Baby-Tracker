package com.babytracker.designsystem.components.pininput

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * AppPinInput 默认值 —— 几何与颜色全部复用 AppComponentTokens.input（TT-019），
 * 不另设令牌组：验证码格本质是"迷你输入框"，与输入框同族取色（参照组件收敛范式）。
 */
@Immutable
object PinInputDefaults {
    /** 单格边长 = 输入框高度基准 */
    @Composable fun cellSize(): Dp = LocalAppComponentTokens.current.input.height

    @Composable fun cellRadius(): Shape = RoundedCornerShape(LocalAppComponentTokens.current.input.cornerRadius)

    @Composable fun focusedBorderColor(): Color = LocalAppComponentTokens.current.input.focusedBorderColor

    @Composable fun unfocusedBorderColor(): Color = LocalAppComponentTokens.current.input.unfocusedBorderColor

    @Composable fun errorBorderColor(): Color = LocalAppComponentTokens.current.input.errorBorderColor

    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.input.containerColor

    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.input.cursorColor

    @Composable fun disabledContentColor(): Color = LocalAppComponentTokens.current.input.placeholderColor
}

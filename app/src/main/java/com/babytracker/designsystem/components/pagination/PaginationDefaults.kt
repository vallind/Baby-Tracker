package com.babytracker.designsystem.components.pagination

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * AppPagination 默认值 —— 全部读 AppComponentTokens.pagination（TT-027 令牌组，此前悬空，本组件首次消费）。
 */
@Immutable
object PaginationDefaults {
    @Composable fun itemSize(): Dp = LocalAppComponentTokens.current.pagination.itemSize
    @Composable fun itemSpacing(): Dp = LocalAppComponentTokens.current.pagination.itemSpacing
    @Composable fun activeColor(): Color = LocalAppComponentTokens.current.pagination.activeColor
    @Composable fun inactiveColor(): Color = LocalAppComponentTokens.current.pagination.inactiveColor
    @Composable fun cornerRadius(): Shape = RoundedCornerShape(LocalAppComponentTokens.current.pagination.cornerRadius)
}

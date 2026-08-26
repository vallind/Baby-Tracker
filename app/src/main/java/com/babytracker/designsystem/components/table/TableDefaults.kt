package com.babytracker.designsystem.components.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * AppDataTable 默认值 —— 全部读 AppComponentTokens.table（TT-023 令牌组，此前悬空，本组件首次消费）。
 */
@Immutable
object TableDefaults {
    @Composable fun headerHeight(): Dp = LocalAppComponentTokens.current.table.headerHeight
    @Composable fun rowHeight(): Dp = LocalAppComponentTokens.current.table.rowHeight
    @Composable fun cellHorizontalPadding(): Dp = LocalAppComponentTokens.current.table.cellHorizontalPadding
    @Composable fun cellVerticalPadding(): Dp = LocalAppComponentTokens.current.table.cellVerticalPadding

    /** 表头底色（浅 hover 灰） */
    @Composable fun headerBgColor(): Color = LocalAppComponentTokens.current.table.headerBgColor

    /** 行选中底色 */
    @Composable fun selectedBgColor(): Color = LocalAppComponentTokens.current.table.selectedBgColor

    /** 行分隔线粗细 */
    @Composable fun dividerThickness(): Dp = LocalAppComponentTokens.current.table.dividerThickness
}

package com.babytracker.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════
//  组件 Defaults — 对标 Palette 的 XxxDefaults 模式
//  每个函数通过 @Composable 从主题令牌读取默认值
//
//  优先级模型（对标 Palette 五级）：
//    显式参数 > Defaults 参数 > 组件令牌 > 语义令牌 > 回退值
// ═══════════════════════════════════════════════════════════

// —— 卡片 ——
object CardDefaults {
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.card.cornerRadius
    @Composable fun innerPadding(): Dp = LocalAppComponentTokens.current.card.innerPadding
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.card.elevation
}

// —— 导航栏 ——
object AppBarDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.appBar.height
    @Composable fun titleSize(): TextUnit = LocalAppComponentTokens.current.appBar.titleSize
    @Composable fun titleWeight(): FontWeight = LocalAppComponentTokens.current.appBar.titleWeight
    @Composable fun backIconSize(): Dp = LocalAppComponentTokens.current.appBar.backIconSize
}

// —— 按钮 ——
object ButtonDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.button.height
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.button.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.button.fontSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.button.fontWeight
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.button.iconSize
    @Composable fun disabledAlpha(): Float = LocalAppComponentTokens.current.button.disabledAlpha
}

// —— 输入框 ——
object InputDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.input.height
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.input.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.input.fontSize
    @Composable fun borderWidth(): Dp = LocalAppComponentTokens.current.input.borderWidth
    @Composable fun borderWidthFocus(): Dp = LocalAppComponentTokens.current.input.borderWidthFocus
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.input.iconSize
}

// —— 标签 ——
object ChipDefaults {
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.chip.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.chip.fontSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.chip.fontWeight
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.chip.horizontalPadding
    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.chip.verticalPadding
}

// —— FAB ——
object FabDefaults {
    @Composable fun size(): Dp = LocalAppComponentTokens.current.fab.size
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.fab.iconSize
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.fab.cornerRadius
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.fab.elevation
}

// —— 底部导航 ——
object BottomBarDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.bottomBar.height
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.bottomBar.iconSize
    @Composable fun labelSize(): TextUnit = LocalAppComponentTokens.current.bottomBar.labelSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.bottomBar.fontWeight
}

// —— 列表项 ——
object ListItemDefaults {
    @Composable fun minHeight(): Dp = LocalAppComponentTokens.current.listItem.minHeight
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.listItem.horizontalPadding
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.listItem.iconSize
    @Composable fun titleSize(): TextUnit = LocalAppComponentTokens.current.listItem.titleSize
    @Composable fun subtitleSize(): TextUnit = LocalAppComponentTokens.current.listItem.subtitleSize
    @Composable fun dividerAlpha(): Float = LocalAppComponentTokens.current.listItem.dividerAlpha
}

// —— 骨架屏 ——
object SkeletonDefaults {
    @Composable fun shimmerColor1(): Color = LocalAppComponentTokens.current.skeleton.shimmerColor1
    @Composable fun shimmerColor2(): Color = LocalAppComponentTokens.current.skeleton.shimmerColor2
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.skeleton.cornerRadius
    @Composable fun avatarSize(): Dp = LocalAppComponentTokens.current.skeleton.avatarSize
}

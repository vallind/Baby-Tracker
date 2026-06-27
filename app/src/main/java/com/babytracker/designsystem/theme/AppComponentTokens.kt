package com.babytracker.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════
//  组件令牌 — 对标 PaletteComponentThemes
//  每组对应一个组件的可主题化属性
// ═══════════════════════════════════════════════════════════

// —— 卡片 ——
data class CardTokens(
    val cornerRadius: Dp = 16.dp,
    val innerPadding: Dp = 16.dp,
    val elevation: Dp = 2.dp,
)

// —— 导航栏 ——
data class AppBarTokens(
    val height: Dp = 56.dp,
    val titleSize: TextUnit = 18.sp,
    val titleWeight: FontWeight = FontWeight.SemiBold,
    val backIconSize: Dp = 22.dp,
)

// —— 按钮 ——
data class ButtonTokens(
    val height: Dp = 48.dp,
    val cornerRadius: Dp = 24.dp,
    val fontSize: TextUnit = 15.sp,
    val fontWeight: FontWeight = FontWeight.SemiBold,
    val iconSize: Dp = 20.dp,
    val disabledAlpha: Float = 0.38f,
)

// —— 输入框 ——
data class InputTokens(
    val height: Dp = 48.dp,
    val cornerRadius: Dp = 12.dp,
    val fontSize: TextUnit = 15.sp,
    val borderWidth: Dp = 1.dp,
    val borderWidthFocus: Dp = 2.dp,
    val iconSize: Dp = 20.dp,
)

// —— 标签 ——
data class ChipTokens(
    val cornerRadius: Dp = 20.dp,
    val fontSize: TextUnit = 12.sp,
    val fontWeight: FontWeight = FontWeight.Medium,
    val horizontalPadding: Dp = 12.dp,
    val verticalPadding: Dp = 6.dp,
)

// —— FAB ——
data class FabTokens(
    val size: Dp = 56.dp,
    val iconSize: Dp = 24.dp,
    val cornerRadius: Dp = 28.dp,
    val elevation: Dp = 6.dp,
)

// —— 底部导航 ——
data class BottomBarTokens(
    val height: Dp = 64.dp,
    val iconSize: Dp = 22.dp,
    val labelSize: TextUnit = 11.sp,
    val fontWeight: FontWeight = FontWeight.Medium,
)

// —— 列表项 ——
data class ListItemTokens(
    val minHeight: Dp = 56.dp,
    val horizontalPadding: Dp = 16.dp,
    val iconSize: Dp = 24.dp,
    val titleSize: TextUnit = 15.sp,
    val subtitleSize: TextUnit = 13.sp,
    val dividerAlpha: Float = 0.12f,
)

// —— 骨架屏 ——
data class SkeletonTokens(
    val shimmerColor1: Color = Color(0xFFE0E0E0),
    val shimmerColor2: Color = Color(0xFFF5F5F5),
    val cornerRadius: Dp = 4.dp,
    val avatarSize: Dp = 40.dp,
)

// ═══════════════════════════════════════════════════════════
//  AppComponentTokens 聚合
// ═══════════════════════════════════════════════════════════

data class AppComponentTokens(
    val card: CardTokens = CardTokens(),
    val appBar: AppBarTokens = AppBarTokens(),
    val button: ButtonTokens = ButtonTokens(),
    val input: InputTokens = InputTokens(),
    val chip: ChipTokens = ChipTokens(),
    val fab: FabTokens = FabTokens(),
    val bottomBar: BottomBarTokens = BottomBarTokens(),
    val listItem: ListItemTokens = ListItemTokens(),
    val skeleton: SkeletonTokens = SkeletonTokens(),
) {
    companion object {
        /** 默认组件令牌（可后续扩展暗色变体） */
        fun default(): AppComponentTokens = AppComponentTokens()
    }
}

val LocalAppComponentTokens = staticCompositionLocalOf { AppComponentTokens() }

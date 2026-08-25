package com.babytracker.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════
//  组件令牌 — TT-017 ~ TT-036
//  参照 PaletteComponentThemes 架构：
//  - 每个组件令牌有独立 default() 工厂，接收基础令牌参数
//  - AppComponentTokens.default() 统一接收 colors/spacing/shapes/typography/
//    opacity/motion/elevation/control，分发到各组件
//  - TODO: 支持 derive {} 部分覆盖（TT-032）
// ═══════════════════════════════════════════════════════════

// —— TT-017 按钮（B 批：type 轴补全 Tonal，Text→Ghost、Secondary→Outline 改名对齐完整语义）——
@Immutable
data class ButtonTokens(
    val height: Dp,
    val cornerRadius: Dp,
    val fontSize: TextUnit,
    val fontWeight: FontWeight,
    val iconSize: Dp,
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledAlpha: Float,
    val tonalContainerColor: Color,     // Tonal 变体容器色（次级容器底）
    val tonalContentColor: Color,       // Tonal 变体内容色
    val secondaryContentColor: Color,   // Outline 变体内容色（描边色）
    val textContentColor: Color,        // Ghost 变体内容色
    val dangerContainerColor: Color,    // Danger 变体容器色（破坏性操作，如删除）
    val dangerContentColor: Color,      // Danger 变体内容色
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            typography: AppTypography,
            control: AppControlTokens,
            opacity: AppOpacity,
        ): ButtonTokens = ButtonTokens(
            height = control.medium.height,
            cornerRadius = shapes.scaled(shapes.full),   // 现代胶囊按钮
            fontSize = typography.bodyLarge.fontSize,
            fontWeight = FontWeight.SemiBold,
            iconSize = control.medium.iconSize,
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.bgDisabled,
            disabledContentColor = colors.textDisabled,
            disabledAlpha = opacity.disabled,
            tonalContainerColor = colors.secondaryScale.shade100,
            tonalContentColor = colors.secondaryScale.shade600,
            secondaryContentColor = colors.primary,
            textContentColor = colors.primary,
            dangerContainerColor = colors.danger,
            dangerContentColor = colors.onError,
        )
    }
}

// —— TT-018 卡片（A 批升级为全功能基座：variant 颜色组参照 Palette 的 per-variant 字段模式）——
@Immutable
data class CardTokens(
    // 共享几何：三种变体共用一套圆角与圆角缩放；innerPadding 为 Medium 档，Compact/Large 由 spacing 派生
    val cornerRadius: Dp,
    val innerPadding: Dp,
    val elevation: Dp,                           // 仅 Elevated 变体消费的暖阴影高度
    // Filled：平面色差卡
    val filledContainerColor: Color,
    val filledContentColor: Color,
    // Elevated：surface + 暖阴影（历史全站卡片观感）
    val elevatedContainerColor: Color,
    val elevatedContentColor: Color,
    // Outlined：描边卡
    val outlinedContainerColor: Color,
    val outlinedContentColor: Color,
    val outlinedBorderColor: Color,
    val outlinedBorderWidth: Dp,
    // Transparent：透明容器（仅前景色有意义的场景）
    val transparentContentColor: Color,
    // 选中态：主色描边高亮；禁用态：整体降透明
    val selectedBorderColor: Color,
    val selectedBorderWidth: Dp,
    val disabledAlpha: Float,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            spacing: AppSpacing,
            elevation: AppElevation,
        ): CardTokens = CardTokens(
            cornerRadius = shapes.scaled(shapes.largeIncreased), // 24dp 大圆角卡片
            innerPadding = spacing.md,
            elevation = elevation.level2,                // 轻量暖阴影，替代旧发丝描边
            filledContainerColor = colors.surfaceMuted,  // 奶油浅底平面卡
            filledContentColor = colors.onSurface,
            elevatedContainerColor = colors.surface,     // 历史默认观感归入 Elevated
            elevatedContentColor = colors.onSurface,
            outlinedContainerColor = colors.surface,
            outlinedContentColor = colors.onSurface,
            outlinedBorderColor = colors.outline,
            outlinedBorderWidth = 1.dp,
            transparentContentColor = colors.onSurface,
            selectedBorderColor = colors.primary,
            selectedBorderWidth = 2.dp,
            disabledAlpha = 0.38f,
        )
    }
}

// —— TT-019 输入框 ——
@Immutable
data class InputTokens(
    val height: Dp,
    val cornerRadius: Dp,
    val fontSize: TextUnit,
    val borderWidth: Dp,
    val borderWidthFocus: Dp,
    val iconSize: Dp,
    val containerColor: Color,
    val unfocusedBorderColor: Color,
    val focusedBorderColor: Color,
    val errorBorderColor: Color,
    val placeholderColor: Color,
    val cursorColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            typography: AppTypography,
            control: AppControlTokens,
        ): InputTokens = InputTokens(
            height = 56.dp,
            cornerRadius = shapes.scaled(shapes.medium),
            fontSize = typography.bodyLarge.fontSize,
            borderWidth = 0.dp,
            borderWidthFocus = 2.dp,
            iconSize = control.medium.iconSize,
            containerColor = colors.surfaceMuted,        // 填充式输入框：奶油浅底无描边
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = colors.borderFocus,
            errorBorderColor = colors.error,
            placeholderColor = colors.textTertiary,
            cursorColor = colors.primary,
        )
    }
}

// —— TT-020 下拉菜单 ——
@Immutable
data class SelectTokens(
    val menuItemHeight: Dp,
    val maxHeight: Dp,
    val itemHorizontalPadding: Dp,
    val itemVerticalPadding: Dp,
    val selectedBgColor: Color,
    val hoverBgColor: Color,
    val dividerColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            spacing: AppSpacing,
        ): SelectTokens = SelectTokens(
            menuItemHeight = 44.dp,
            maxHeight = 300.dp,
            itemHorizontalPadding = spacing.md,
            itemVerticalPadding = 10.dp,
            selectedBgColor = colors.bgSelected,
            hoverBgColor = colors.bgHover,
            dividerColor = colors.divider,
        )
    }
}

// —— TT-021 选择控件（复选框/单选） ——
@Immutable
data class SelectionControlTokens(
    val size: Dp,
    val strokeWidth: Dp,
    val checkedColor: Color,
    val uncheckedColor: Color,
    val disabledColor: Color,
    val animationDurationMs: Int,
) {
    companion object {
        fun default(
            colors: AppColors,
            opacity: AppOpacity,
        ): SelectionControlTokens = SelectionControlTokens(
            size = 20.dp,
            strokeWidth = 2.dp,
            checkedColor = colors.primary,
            uncheckedColor = colors.outline,
            disabledColor = colors.textDisabled,
            animationDurationMs = 200,
        )
    }
}

// —— TT-022 开关 ——
@Immutable
data class SwitchTokens(
    val trackWidth: Dp,
    val trackHeight: Dp,
    val thumbSize: Dp,
    val trackCornerRadius: Dp,
    val checkedColor: Color,
    val uncheckedColor: Color,
    val thumbElevation: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            elevation: AppElevation,
        ): SwitchTokens = SwitchTokens(
            trackWidth = 40.dp,
            trackHeight = 24.dp,
            thumbSize = 20.dp,
            trackCornerRadius = 12.dp,
            checkedColor = colors.primary,
            uncheckedColor = colors.outline,
            thumbElevation = elevation.level1,
        )
    }
}

// —— TT-023 表格 ——
@Immutable
data class TableTokens(
    val headerHeight: Dp,
    val rowHeight: Dp,
    val cellHorizontalPadding: Dp,
    val cellVerticalPadding: Dp,
    val dividerThickness: Dp,
    val headerBgColor: Color,
    val hoverBgColor: Color,
    val selectedBgColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            spacing: AppSpacing,
        ): TableTokens = TableTokens(
            headerHeight = 48.dp,
            rowHeight = 44.dp,
            cellHorizontalPadding = spacing.md,
            cellVerticalPadding = spacing.sm,
            dividerThickness = 0.5.dp,
            headerBgColor = colors.bgHover,
            hoverBgColor = colors.bgHover,
            selectedBgColor = colors.bgSelected,
        )
    }
}

// —— TT-024 对话框/遮罩 ——
@Immutable
data class DialogTokens(
    val scrimColor: Color,
    val scrimOpacity: Float,
    val containerColor: Color,
    val contentColor: Color,
    val cornerRadius: Dp,
    val contentHorizontalPadding: Dp,
    val contentVerticalPadding: Dp,
    val dividerColor: Color,
    val elevation: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            spacing: AppSpacing,
            elevation: AppElevation,
            opacity: AppOpacity,
        ): DialogTokens = DialogTokens(
            scrimColor = colors.scrim,
            scrimOpacity = opacity.scrim,
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            cornerRadius = shapes.scaled(shapes.large),   // shadcn 风格：对话框 = rounded-lg
            contentHorizontalPadding = spacing.lg,
            contentVerticalPadding = 20.dp,
            dividerColor = colors.divider,
            elevation = elevation.level4,
        )
    }
}

// —— TT-025 菜单 ——
@Immutable
data class MenuTokens(
    val itemHeight: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val minWidth: Dp,
    val cornerRadius: Dp,
    val hoverBgColor: Color,
    val selectedBgColor: Color,
    val elevation: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            spacing: AppSpacing,
            elevation: AppElevation,
        ): MenuTokens = MenuTokens(
            itemHeight = 44.dp,
            horizontalPadding = spacing.md,
            verticalPadding = spacing.sm,
            minWidth = 140.dp,
            cornerRadius = shapes.scaled(shapes.medium),
            hoverBgColor = colors.bgHover,
            selectedBgColor = colors.bgSelected,
            elevation = elevation.level3,
        )
    }
}

// —— TT-026 标签语义变体 ——
@Immutable
data class TagVariantColors(
    val backgroundColor: Color,
    val textColor: Color,
)

@Immutable
data class TagTokens(
    val primary: TagVariantColors,
    val success: TagVariantColors,
    val warning: TagVariantColors,
    val danger: TagVariantColors,
    val default: TagVariantColors,
    val cornerRadius: Dp,
    val fontSize: TextUnit,
    val fontWeight: FontWeight,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            typography: AppTypography,
            spacing: AppSpacing,
            opacity: AppOpacity,
        ): TagTokens = TagTokens(
            primary = TagVariantColors(
                backgroundColor = colors.primaryScale.tintContainer(colors),
                textColor = colors.primaryScale.accentContent(colors),
            ),
            success = TagVariantColors(
                backgroundColor = colors.successScale.tintContainer(colors),
                textColor = colors.successScale.accentContent(colors),
            ),
            warning = TagVariantColors(
                backgroundColor = colors.warningScale.tintContainer(colors),
                textColor = colors.warningScale.accentContent(colors),
            ),
            danger = TagVariantColors(
                backgroundColor = colors.dangerScale.tintContainer(colors),
                textColor = colors.dangerScale.accentContent(colors),
            ),
            default = TagVariantColors(
                backgroundColor = colors.surfaceMuted,
                textColor = colors.textSecondary,
            ),
            cornerRadius = shapes.scaled(shapes.full),     // 胶囊标签
            fontSize = typography.labelMedium.fontSize,
            fontWeight = FontWeight.Medium,
            horizontalPadding = 12.dp,
            verticalPadding = 6.dp,
        )
    }
}

// —— TT-033 分割线 ——
@Immutable
data class DividerTokens(
    val color: Color,
    val thickness: Dp,
) {
    companion object {
        fun default(colors: AppColors): DividerTokens = DividerTokens(
            color = colors.divider,
            thickness = 0.5.dp,
        )
    }
}

// —— TT-034 表面容器 ——
@Immutable
data class SurfaceTokens(
    val color: Color,
    val shape: Shape,
) {
    companion object {
        fun default(colors: AppColors, shapes: AppShapes): SurfaceTokens = SurfaceTokens(
            color = colors.surface,
            shape = RoundedCornerShape(shapes.scaled(shapes.medium)),   // 卡片级圆角
        )
    }
}

// —— TT-035 全局提示宿主 ——
@Immutable
data class SnackbarHostTokens(
    val containerColor: Color,
    val contentColor: Color,
    val cornerRadius: Dp,
    val elevation: Dp,
) {
    companion object {
        fun default(colors: AppColors, shapes: AppShapes, elevation: AppElevation): SnackbarHostTokens =
            SnackbarHostTokens(
                containerColor = colors.inverseSurface,
                contentColor = colors.inverseOnSurface,
                cornerRadius = shapes.scaled(shapes.medium),
                elevation = elevation.level3,   // 中档阴影；M3 1.4.0 Snackbar 无 tonalElevation 参数，令牌预留
            )
    }
}

// —— TT-036 空状态 ——
@Immutable
data class EmptyStateTokens(
    val emojiSize: TextUnit,
    val titleColor: Color,
    val subtitleColor: Color,
    val actionSpacing: Dp,
) {
    companion object {
        fun default(colors: AppColors, spacing: AppSpacing): EmptyStateTokens = EmptyStateTokens(
            emojiSize = 64.sp,
            titleColor = colors.onSurface,
            subtitleColor = colors.textSecondary,
            actionSpacing = spacing.lg,
        )
    }
}

// —— TT-027 进度/骨架屏 ——
@Immutable
data class ProgressTokens(
    val height: Dp,
    val circularSize: Dp,
    val strokeWidth: Dp,
    val trackColor: Color,
    val indicatorColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
        ): ProgressTokens = ProgressTokens(
            height = 8.dp,
            circularSize = 32.dp,
            strokeWidth = 4.dp,
            trackColor = colors.surfaceMuted,
            indicatorColor = colors.primary,
        )
    }
}

@Immutable
data class SkeletonTokens(
    val shimmerColor1: Color,
    val shimmerColor2: Color,
    val cornerRadius: Dp,
    val avatarSize: Dp,
    val shimmerDurationMs: Int,
) {
    companion object {
        fun default(
            shapes: AppShapes,
            darkTheme: Boolean = false,
        ): SkeletonTokens = SkeletonTokens(
            shimmerColor1 = if (darkTheme) Color(0xFF383430) else Color(0xFFEAE6E0),
            shimmerColor2 = if (darkTheme) Color(0xFF4A453F) else Color(0xFFF5F2EC),
            cornerRadius = shapes.scaled(shapes.extraSmall),
            avatarSize = 40.dp,
            shimmerDurationMs = 1000,
        )
    }
}

// —— TT-028 步骤/分页 ——
@Immutable
data class StepsTokens(
    val dotSize: Dp,
    val connectorThickness: Dp,
    val activeColor: Color,
    val inactiveColor: Color,
    val completedColor: Color,
    val labelStyle: TextStyle,
) {
    companion object {
        fun default(
            colors: AppColors,
        ): StepsTokens = StepsTokens(
            dotSize = 10.dp,
            connectorThickness = 2.dp,
            activeColor = colors.primary,
            inactiveColor = colors.textDisabled,
            completedColor = colors.success,
            labelStyle = TextStyle.Default,
        )
    }
}

@Immutable
data class PaginationTokens(
    val itemSize: Dp,
    val itemSpacing: Dp,
    val activeColor: Color,
    val inactiveColor: Color,
    val cornerRadius: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
        ): PaginationTokens = PaginationTokens(
            itemSize = 32.dp,
            itemSpacing = 4.dp,
            activeColor = colors.primary,
            inactiveColor = colors.textDisabled,
            cornerRadius = shapes.scaled(shapes.large),
        )
    }
}

// —— TT-029 滑块/评分 ——
@Immutable
data class SliderTokens(
    val trackHeight: Dp,
    val thumbSize: Dp,
    val activeColor: Color,
    val inactiveColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
        ): SliderTokens = SliderTokens(
            trackHeight = 6.dp,
            thumbSize = 22.dp,
            activeColor = colors.primary,
            inactiveColor = colors.borderDisabled,
        )
    }
}

@Immutable
data class RateTokens(
    val starSize: Dp,
    val starSpacing: Dp,
    val selectedColor: Color,
    val unselectedColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
        ): RateTokens = RateTokens(
            starSize = 24.dp,
            starSpacing = 4.dp,
            selectedColor = colors.warning,
            unselectedColor = colors.textTertiary,
        )
    }
}

// —— 导航栏 ——
@Immutable
data class AppBarTokens(
    val height: Dp,
    val titleSize: TextUnit,
    val titleWeight: FontWeight,
    val backIconSize: Dp,
    val containerColor: Color,
    val titleColor: Color,
    val iconColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            typography: AppTypography,
            control: AppControlTokens,
            darkTheme: Boolean = false,
        ): AppBarTokens = AppBarTokens(
            height = 56.dp,
            titleSize = typography.titleLarge.fontSize,
            titleWeight = FontWeight.SemiBold,
            backIconSize = 22.dp,
            containerColor = colors.pageBackground,      // 顶栏融入页面底，去掉旧式彩色大色块
            titleColor = colors.textPrimary,
            iconColor = colors.textPrimary,
        )
    }
}

// —— 标签 ——
@Immutable
data class ChipTokens(
    val cornerRadius: Dp,
    val fontSize: TextUnit,
    val fontWeight: FontWeight,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val backgroundColor: Color,
    val textColor: Color,
    val selectedContainerColor: Color,   // B 批交互胶囊：选中底
    val selectedTextColor: Color,        // B 批交互胶囊：选中前景
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            typography: AppTypography,
        ): ChipTokens = ChipTokens(
            cornerRadius = shapes.scaled(shapes.full),     // shadcn 风格：Chip = rounded-full
            fontSize = typography.labelMedium.fontSize,
            fontWeight = FontWeight.Medium,
            horizontalPadding = 12.dp,
            verticalPadding = 6.dp,
            backgroundColor = colors.surfaceElevated,
            textColor = colors.primary,
            selectedContainerColor = colors.primary,
            selectedTextColor = colors.onPrimary,
        )
    }
}

// —— FAB ——
@Immutable
data class FabTokens(
    val size: Dp,
    val iconSize: Dp,
    val cornerRadius: Dp,
    val elevation: Dp,
    val containerColor: Color,
    val contentColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            control: AppControlTokens,
            elevation: AppElevation,
        ): FabTokens = FabTokens(
            size = 56.dp,
            iconSize = control.large.iconSize,
            cornerRadius = 28.dp,                        // 胶囊 FAB
            elevation = elevation.level3,
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
        )
    }
}

// —— 底部导航 ——
@Immutable
data class BottomBarTokens(
    val height: Dp,
    val iconSize: Dp,
    val labelSize: TextUnit,
    val fontWeight: FontWeight,
    val containerColor: Color,
    val contentColor: Color,
    val selectedColor: Color,
    val unselectedColor: Color,
    val indicatorColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            typography: AppTypography,
        ): BottomBarTokens = BottomBarTokens(
            height = 72.dp,
            iconSize = 24.dp,
            labelSize = 11.sp,
            fontWeight = FontWeight.Medium,
            containerColor = colors.surface,
            contentColor = colors.textPrimary,
            selectedColor = colors.primary,
            unselectedColor = colors.textSecondary,
            indicatorColor = colors.primaryContainer,
        )
    }
}

// —— 列表项 ——
@Immutable
data class ListItemTokens(
    val minHeight: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val titleSize: TextUnit,
    val subtitleSize: TextUnit,
    val dividerAlpha: Float,
    val titleColor: Color,
    val subtitleColor: Color,
    val dividerColor: Color,
    val actionColor: Color,
    // D 批状态轴：选中底 / 禁用透明
    val selectedContainerColor: Color,
    val disabledAlpha: Float,
) {
    companion object {
        fun default(
            colors: AppColors,
            spacing: AppSpacing,
            typography: AppTypography,
            opacity: AppOpacity,
        ): ListItemTokens = ListItemTokens(
            minHeight = 64.dp,
            horizontalPadding = spacing.md,
            iconSize = 24.dp,
            titleSize = typography.bodyLarge.fontSize,
            subtitleSize = typography.bodyMedium.fontSize,
            dividerAlpha = opacity.divider,
            titleColor = colors.textPrimary,
            subtitleColor = colors.textSecondary,
            dividerColor = colors.divider,
            actionColor = colors.primary,
            selectedContainerColor = colors.bgSelected,
            disabledAlpha = opacity.disabled,
        )
    }
}

// —— 图标按钮 ——
@Immutable
data class IconButtonTokens(
    val iconSize: Dp,
    val tintColor: Color,
    // B 批 variant 轴：Filled/Tonal/Outlined 三种带底形态的颜色组（Standard 无底沿用 tintColor）
    val filledContainerColor: Color,
    val filledContentColor: Color,
    val tonalContainerColor: Color,
    val tonalContentColor: Color,
    val outlinedContentColor: Color,
    val outlinedBorderColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            control: AppControlTokens,
        ): IconButtonTokens = IconButtonTokens(
            iconSize = control.medium.iconSize,
            tintColor = colors.primary,
            filledContainerColor = colors.primary,
            filledContentColor = colors.onPrimary,
            tonalContainerColor = colors.secondaryScale.shade100,
            tonalContentColor = colors.secondaryScale.shade600,
            outlinedContentColor = colors.textSecondary,
            outlinedBorderColor = colors.outline,
        )
    }
}

// —— 脚手架 ——
@Immutable
data class ScaffoldTokens(
    val containerColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
        ): ScaffoldTokens = ScaffoldTokens(
            containerColor = colors.pageBackground,
        )
    }
}

// —— 边框容器 ——
@Immutable
data class BorderContainerTokens(
    val borderColor: Color,
    val cornerRadius: Dp,
    val borderWidth: Dp,
    val padding: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            spacing: AppSpacing,
        ): BorderContainerTokens = BorderContainerTokens(
            borderColor = colors.outline,
            cornerRadius = shapes.scaled(shapes.largeIncreased),
            borderWidth = 0.dp,
            padding = spacing.md,
        )
    }
}

// —— 时间选择器 ——
//  TDesign 风格：滚轮式时/分选择，选中项有品牌色高亮背景
@Immutable
data class TimePickerTokens(
    val hourColor: Color,
    val minuteColor: Color,
    val separatorColor: Color,
    val labelColor: Color,
    val arrowColor: Color,
    val backgroundColor: Color,
    val cornerRadius: Dp,           // shapes.large（16dp）
    // TDesign 新增：滚轮选中态
    val selectedBackgroundColor: Color,     // primary.copy(alpha=0.12)
    val selectedTextColor: Color,           // primary
    val unselectedTextColor: Color,         // textTertiary
    val dividerColor: Color,                // 分隔线
    val itemHeight: Dp,                     // 滚轮单项高度
    val visibleItems: Int,                  // 可见项数
    val toolbarHeight: Dp,                  // 顶部工具栏高度
    val toolbarTextColor: Color,            // 工具栏文字色
    val toolbarDividerColor: Color,         // 工具栏底部分隔线
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
        ): TimePickerTokens = TimePickerTokens(
            hourColor = colors.textPrimary,
            minuteColor = colors.textPrimary,
            separatorColor = colors.textPrimary,
            labelColor = colors.textSecondary,
            arrowColor = colors.primary,
            backgroundColor = colors.surface,
            cornerRadius = shapes.scaled(shapes.large),
            selectedBackgroundColor = colors.primaryScale.tintContainer(colors), // 粉彩选中底
            selectedTextColor = colors.primaryScale.accentContent(colors),
            unselectedTextColor = colors.textTertiary,
            dividerColor = colors.divider,
            itemHeight = 44.dp,
            visibleItems = 5,
            toolbarHeight = 48.dp,
            toolbarTextColor = colors.primary,
            toolbarDividerColor = colors.divider,
        )
    }
}

// —— 日期选择器 ——
//  TDesign 风格：日历面板，圆形选中标记，月/年头部分
@Immutable
data class DatePickerTokens(
    val selectedDayColor: Color,
    val selectedDayContentColor: Color,
    val todayColor: Color,
    val headlineColor: Color,
    val backgroundColor: Color,
    val cornerRadius: Dp,               // shapes.large（16dp）
    // TDesign 新增：日历面板
    val toolbarHeight: Dp,              // 顶部工具栏高度
    val toolbarTextColor: Color,        // 工具栏文字色
    val toolbarDividerColor: Color,     // 工具栏底部分隔线
    val weekHeaderColor: Color,         // 星期标题色
    val daySize: Dp,                    // 日期单元格大小
    val dayTextColor: Color,            // 普通日期文字色
    val dayDisabledTextColor: Color,    // 不可选日期文字色
    val selectedShapeRadius: Dp,        // 选中圆形半径
    val monthYearTextColor: Color,      // 年月标题色
    val navArrowColor: Color,           // 左右导航箭头色
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
        ): DatePickerTokens = DatePickerTokens(
            selectedDayColor = colors.primary,
            selectedDayContentColor = colors.onPrimary,
            todayColor = colors.primary,
            headlineColor = colors.textPrimary,
            backgroundColor = colors.surface,
            cornerRadius = shapes.scaled(shapes.large),
            toolbarHeight = 48.dp,
            toolbarTextColor = colors.primary,
            toolbarDividerColor = colors.divider,
            weekHeaderColor = colors.textTertiary,
            daySize = 40.dp,
            dayTextColor = colors.textPrimary,
            dayDisabledTextColor = colors.textDisabled,
            selectedShapeRadius = 20.dp,
            monthYearTextColor = colors.textPrimary,
            navArrowColor = colors.primary,
        )
    }
}

// —— 级联日期时间选择器 ——
@Immutable
data class DateTimeCascadeTokens(
    val backgroundColor: Color,
    val cornerRadius: Dp,       // shapes.large（16dp）
    val datePicker: DatePickerTokens,
    val timePicker: TimePickerTokens,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
        ): DateTimeCascadeTokens = DateTimeCascadeTokens(
            backgroundColor = colors.surface,
            cornerRadius = shapes.scaled(shapes.large),
            datePicker = DatePickerTokens.default(colors, shapes),
            timePicker = TimePickerTokens.default(colors, shapes),
        )
    }
}

// —— 底部弹层 ——
//  参照 shadcn Sheet：右上角关闭按钮 + 背景遮罩
@Immutable
data class SheetTokens(
    val containerColor: Color,
    val contentColor: Color,
    val scrimColor: Color,
    val cornerRadius: Dp,           // shapes.large（顶部圆角）
    val dragHandleColor: Color,
    val dragHandleWidth: Dp,
    val dragHandleHeight: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
        ): SheetTokens = SheetTokens(
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            scrimColor = colors.scrim,
            cornerRadius = shapes.scaled(shapes.extraLarge), // 32dp 大顶部圆角
            dragHandleColor = colors.divider,
            dragHandleWidth = 32.dp,
            dragHandleHeight = 4.dp,
        )
    }
}

// —— 分段选择器 ——
//  参照 shadcn Toggle Group / iOS 分段控件
@Immutable
data class SegmentedControlTokens(
    val containerColor: Color,
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val cornerRadius: Dp,           // shapes.large（外层）
    val innerCornerRadius: Dp,      // shapes.small（选中项）
    val borderWidth: Dp,
    val fontSize: TextUnit,
    val fontWeight: FontWeight,
    val selectedFontWeight: FontWeight,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            typography: AppTypography,
        ): SegmentedControlTokens = SegmentedControlTokens(
            containerColor = colors.surfaceMuted,       // 中性轨道槽（对标 iOS/HeroUI 分段控件）
            selectedContainerColor = colors.surface,    // 白色滑块
            selectedContentColor = colors.primary,
            unselectedContentColor = colors.textSecondary,
            cornerRadius = shapes.scaled(shapes.large),
            innerCornerRadius = shapes.scaled(shapes.small),
            borderWidth = 2.dp,
            fontSize = typography.labelMedium.fontSize,
            fontWeight = FontWeight.Normal,
            selectedFontWeight = FontWeight.SemiBold,
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  TT-030 顶层聚合容器
//  参照 PaletteComponentThemes.default()：统一接收所有基础令牌，分发到各组件
// ═══════════════════════════════════════════════════════════

// —— 统计格（今日概览/尿布汇总等数值格子，全站统一规格） ——
@Immutable
data class StatCellTokens(
    val valueColor: Color,
    val unitColor: Color,
    val labelColor: Color,
    val valueFontSize: TextUnit,
    val valueFontWeight: FontWeight,
    val unitFontSize: TextUnit,
    val labelFontSize: TextUnit,
    val emojiFontSize: TextUnit,
    val innerSpacing: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            typography: AppTypography,
            spacing: AppSpacing,
        ): StatCellTokens = StatCellTokens(
            valueColor = colors.primary,                  // 数值用强调色（数据突出）
            unitColor = colors.textSecondary,
            labelColor = colors.textTertiary,
            valueFontSize = typography.headlineSmall.fontSize, // 统一 21sp
            valueFontWeight = FontWeight.Bold,
            unitFontSize = typography.labelMedium.fontSize,
            labelFontSize = typography.labelMedium.fontSize,
            emojiFontSize = typography.titleLarge.fontSize,
            innerSpacing = spacing.xs,
        )
    }
}

// —— 底部主操作条（列表页固定底栏全宽按钮，替代三页复制粘贴） ——
@Immutable
data class ActionBarTokens(
    val containerColor: Color,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            spacing: AppSpacing,
        ): ActionBarTokens = ActionBarTokens(
            containerColor = colors.surface,
            horizontalPadding = spacing.md,
            verticalPadding = 12.dp,   // 存量事实标准收编（8dp 网格外值，仅此一处）
        )
    }
}

// —— 图标徽章（列表行 40dp emoji 徽章，全站统一规格） ——
@Immutable
data class BadgeTokens(
    val size: Dp,
    val cornerRadius: Dp,
    val fontSize: TextUnit,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            typography: AppTypography,
        ): BadgeTokens = BadgeTokens(
            size = 40.dp,                                // 全站列表行徽章统一尺寸（存量事实标准收编）
            cornerRadius = shapes.scaled(shapes.medium), // shapes.medium
            fontSize = typography.titleLarge.fontSize,   // 字号/容器 ≈ 0.45，与宫格比例一致
        )
    }
}

// —— 渐变摘要卡（夜间睡眠 / 今日尿布等大数字摘要） ——
@Immutable
data class SummaryCardTokens(
    val cornerRadius: Dp,
    val innerPadding: Dp,
    val contentColor: Color,
    val iconContainerAlpha: Float,
    val titleAlpha: Float,
    val subtitleAlpha: Float,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            spacing: AppSpacing,
        ): SummaryCardTokens = SummaryCardTokens(
            cornerRadius = shapes.scaled(shapes.largeIncreased),
            innerPadding = spacing.lg,
            contentColor = colors.onPrimary,
            iconContainerAlpha = 0.25f,
            titleAlpha = 0.90f,
            subtitleAlpha = 0.75f,
        )
    }
}

// —— 日期导航胶囊（记录四页 + 统计页共用，收敛 80 行×4 的复制） ——
@Immutable
data class DateNavCapsuleTokens(
    val capsuleColor: Color,
    val textColor: Color,
    val iconColor: Color,
    val todayContainerColor: Color,
    val todayContentColor: Color,
    val cornerRadius: Dp,           // shapes.full（胶囊）
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
        ): DateNavCapsuleTokens = DateNavCapsuleTokens(
            capsuleColor = colors.surfaceMuted,                 // 柔底胶囊
            textColor = colors.textPrimary,
            iconColor = colors.textTertiary,
            todayContainerColor = colors.primaryScale.tintContainer(colors),
            todayContentColor = colors.primaryScale.accentContent(colors),
            cornerRadius = shapes.scaled(shapes.full),
            horizontalPadding = 18.dp,
            verticalPadding = 10.dp,
        )
    }
}

// —— 统计胶囊（渐变卡上的白字统计格：今日概览/尿布汇总等，全站统一） ——
@Immutable
data class QuickStatPillTokens(
    val valueFontSize: TextUnit,
    val valueFontWeight: FontWeight,
    val unitFontSize: TextUnit,
    val labelFontSize: TextUnit,
    val valueAlpha: Float,
    val unitAlpha: Float,
    val labelAlpha: Float,
    val innerSpacing: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            typography: AppTypography,
            spacing: AppSpacing,
        ): QuickStatPillTokens = QuickStatPillTokens(
            valueFontSize = typography.headlineSmall.fontSize,
            valueFontWeight = FontWeight.Bold,
            unitFontSize = typography.labelMedium.fontSize,
            labelFontSize = typography.labelMedium.fontSize,
            valueAlpha = 1f,
            unitAlpha = 0.78f,
            labelAlpha = 0.70f,
            innerSpacing = spacing.xs,
        )
    }
}

// —— 记录详情弹层（全站记录卡「单击=详情」契约的统一承载） ——
@Immutable
data class RecordDetailSheetTokens(
    val titleSize: TextUnit,
    val titleWeight: FontWeight,
    val labelColor: Color,
    val valueColor: Color,
    val labelWidth: Dp,
    val rowSpacing: Dp,
    val deleteColor: Color,
    val actionSpacing: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            typography: AppTypography,
            spacing: AppSpacing,
        ): RecordDetailSheetTokens = RecordDetailSheetTokens(
            titleSize = typography.titleLarge.fontSize,
            titleWeight = FontWeight.SemiBold,
            labelColor = colors.textSecondary,
            valueColor = colors.textPrimary,
            labelWidth = 92.dp,
            rowSpacing = spacing.md,
            deleteColor = colors.danger,
            actionSpacing = spacing.md,
        )
    }
}

// —— 迷你图表（指标卡内嵌 sparkline：柱状/折线两形态，自 StatsScreen 收编） ——
@Immutable
data class MiniChartTokens(
    val barGapRatio: Float,         // 柱间距 / 柱宽
    val barDimmedAlpha: Float,      // 非峰值柱透明度（峰值柱为实色）
    val lineStrokeWidth: Dp,
    val pointRadius: Dp,
    val areaFillAlpha: Float,       // 折线下方渐变面积顶部透明度
    val lineColor: Color,           // 折线默认色（可用参数覆盖）
) {
    companion object {
        fun default(colors: AppColors): MiniChartTokens = MiniChartTokens(
            barGapRatio = 0.35f,
            barDimmedAlpha = 0.6f,
            lineStrokeWidth = 2.dp,
            pointRadius = 3.dp,
            areaFillAlpha = 0.25f,
            lineColor = colors.primary,
        )
    }
}

// —— 设置行（emoji 徽章 + 标题/副标题 + 尾部插槽的设置项家族，自 feature/settings 收编） ——
@Immutable
data class SettingItemTokens(
    val badgeSize: Dp,
    val badgeCornerRadius: Dp,
    val badgeFontSize: TextUnit,
    val badgeContainerColor: Color,
    val titleFontSize: TextUnit,
    val titleColor: Color,
    val subtitleFontSize: TextUnit,
    val subtitleColor: Color,
    val trailingIconSize: Dp,
    val chevronTint: Color,
    // 胶囊单选行（AppSettingChoiceItem）标题/副标题
    val choiceLabelFontSize: TextUnit,
    val choiceSubtitleFontSize: TextUnit,
    val choiceSubtitleColor: Color,
    // 分组标题（AppSettingGroupTitle）
    val groupTitleFontSize: TextUnit,
    val groupTitleColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            typography: AppTypography,
        ): SettingItemTokens = SettingItemTokens(
            badgeSize = 40.dp,                            // 全站设置行徽章统一尺寸（存量事实标准收编）
            badgeCornerRadius = shapes.scaled(shapes.large),
            badgeFontSize = typography.titleMedium.fontSize,
            badgeContainerColor = colors.primaryContainer,
            titleFontSize = typography.bodyMedium.fontSize,
            titleColor = colors.textPrimary,
            subtitleFontSize = typography.bodySmall.fontSize,
            subtitleColor = colors.textTertiary,
            trailingIconSize = 18.dp,
            chevronTint = colors.textTertiary,
            choiceLabelFontSize = typography.bodyLarge.fontSize,
            choiceSubtitleFontSize = typography.bodyMedium.fontSize,
            choiceSubtitleColor = colors.textSecondary,
            groupTitleFontSize = typography.labelMedium.fontSize,
            groupTitleColor = colors.textSecondary,
        )
    }
}

// —— 键值行（信息展示行：label 左 / value 右 + 可选 caption 副行，自 feature/settings/BabyProfileScreen 收编） ——
@Immutable
data class KeyValueRowTokens(
    val rowHeight: Dp,
    val labelTextStyle: TextStyle,
    val labelColor: Color,
    val valueTextStyle: TextStyle,
    val valueColor: Color,              // 无副行形态（源 InfoRow：值用次要色）
    val valueEmphasizedColor: Color,    // 带副行形态（源 GrowthValueRow：值用主色强调）
    val captionTextStyle: TextStyle,
    val captionColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            typography: AppTypography,
        ): KeyValueRowTokens = KeyValueRowTokens(
            // 以源码实测为准（BabyProfileScreen.InfoRow/GrowthValueRow）：
            // label=textPrimary、value=无副行 textSecondary / 有副行 textPrimary、caption=textTertiary；
            // 行高 44dp；字号 label/value=bodyLarge、caption=labelSmall。
            rowHeight = 44.dp,
            labelTextStyle = typography.bodyLarge,
            labelColor = colors.textPrimary,
            valueTextStyle = typography.bodyLarge,
            valueColor = colors.textSecondary,
            valueEmphasizedColor = colors.textPrimary,
            captionTextStyle = typography.labelSmall,
            captionColor = colors.textTertiary,
        )
    }
}

data class AppComponentTokens(
    val button: ButtonTokens,
    val card: CardTokens,
    val input: InputTokens,
    val select: SelectTokens,
    val selectionControl: SelectionControlTokens,
    val switch: SwitchTokens,
    val table: TableTokens,
    val dialog: DialogTokens,
    val menu: MenuTokens,
    val tag: TagTokens,
    val divider: DividerTokens,
    val surface: SurfaceTokens,
    val snackbarHost: SnackbarHostTokens,
    val progress: ProgressTokens,
    val skeleton: SkeletonTokens,
    val steps: StepsTokens,
    val pagination: PaginationTokens,
    val slider: SliderTokens,
    val rate: RateTokens,
    val appBar: AppBarTokens,
    val chip: ChipTokens,
    val fab: FabTokens,
    val bottomBar: BottomBarTokens,
    val listItem: ListItemTokens,
    val iconButton: IconButtonTokens,
    val scaffold: ScaffoldTokens,
    val borderContainer: BorderContainerTokens,
    val timePicker: TimePickerTokens,
    val datePicker: DatePickerTokens,
    val dateTimeCascade: DateTimeCascadeTokens,
    val sheet: SheetTokens,
    val segmentedControl: SegmentedControlTokens,
    val summaryCard: SummaryCardTokens,
    val emptyState: EmptyStateTokens,
    val badge: BadgeTokens,
    val statCell: StatCellTokens,
    val actionBar: ActionBarTokens,
    val dateNavCapsule: DateNavCapsuleTokens,
    val quickStatPill: QuickStatPillTokens,
    val recordDetailSheet: RecordDetailSheetTokens,
    val miniChart: MiniChartTokens,
    val settingItem: SettingItemTokens,
    val keyValueRow: KeyValueRowTokens,
) {
    companion object {
        fun default(
            colors: AppColors = AppColors.light(),
            spacing: AppSpacing = AppSpacing(),
            shapes: AppShapes = AppShapes(),
            typography: AppTypography = AppTypography(),
            opacity: AppOpacity = AppOpacity(),
            motion: AppMotion = AppMotion(),
            elevation: AppElevation = AppElevation(),
            control: AppControlTokens = AppControlTokens(),
            darkTheme: Boolean = false,
        ): AppComponentTokens = AppComponentTokens(
            button = ButtonTokens.default(colors, shapes, typography, control, opacity),
            card = CardTokens.default(colors, shapes, spacing, elevation),
            input = InputTokens.default(colors, shapes, typography, control),
            select = SelectTokens.default(colors, spacing),
            selectionControl = SelectionControlTokens.default(colors, opacity),
            switch = SwitchTokens.default(colors, elevation),
            table = TableTokens.default(colors, spacing),
            dialog = DialogTokens.default(colors, shapes, spacing, elevation, opacity),
            menu = MenuTokens.default(colors, shapes, spacing, elevation),
            tag = TagTokens.default(colors, shapes, typography, spacing, opacity),
            divider = DividerTokens.default(colors),
            surface = SurfaceTokens.default(colors, shapes),
            snackbarHost = SnackbarHostTokens.default(colors, shapes, elevation),
            progress = ProgressTokens.default(colors),
            skeleton = SkeletonTokens.default(shapes, darkTheme),
            steps = StepsTokens.default(colors),
            pagination = PaginationTokens.default(colors, shapes),
            slider = SliderTokens.default(colors),
            rate = RateTokens.default(colors),
            appBar = AppBarTokens.default(colors, typography, control, darkTheme),
            chip = ChipTokens.default(colors, shapes, typography),
            fab = FabTokens.default(colors, control, elevation),
            bottomBar = BottomBarTokens.default(colors, typography),
            listItem = ListItemTokens.default(colors, spacing, typography, opacity),
            iconButton = IconButtonTokens.default(colors, control),
            scaffold = ScaffoldTokens.default(colors),
            borderContainer = BorderContainerTokens.default(colors, shapes, spacing),
            timePicker = TimePickerTokens.default(colors, shapes),
            datePicker = DatePickerTokens.default(colors, shapes),
            dateTimeCascade = DateTimeCascadeTokens.default(colors, shapes),
            sheet = SheetTokens.default(colors, shapes),
            segmentedControl = SegmentedControlTokens.default(colors, shapes, typography),
            summaryCard = SummaryCardTokens.default(colors, shapes, spacing),
            emptyState = EmptyStateTokens.default(colors, spacing),
            badge = BadgeTokens.default(colors, shapes, typography),
            statCell = StatCellTokens.default(colors, typography, spacing),
            actionBar = ActionBarTokens.default(colors, spacing),
            dateNavCapsule = DateNavCapsuleTokens.default(colors, shapes),
            quickStatPill = QuickStatPillTokens.default(colors, typography, spacing),
            recordDetailSheet = RecordDetailSheetTokens.default(colors, typography, spacing),
            miniChart = MiniChartTokens.default(colors),
            settingItem = SettingItemTokens.default(colors, shapes, typography),
            keyValueRow = KeyValueRowTokens.default(colors, typography),
        )
    }
}

val LocalAppComponentTokens = compositionLocalOf { AppComponentTokens.default() }

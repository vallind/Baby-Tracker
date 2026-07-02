package com.babytracker.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════
//  组件令牌 — TT-017 ~ TT-029
//  参照 PaletteComponentThemes 架构：
//  - 每个组件令牌有独立 default() 工厂，接收基础令牌参数
//  - AppComponentTokens.default() 统一接收 colors/spacing/shapes/typography/
//    opacity/motion/elevation/control，分发到各组件
//  - 支持 derive {} 部分覆盖（TT-032）
// ═══════════════════════════════════════════════════════════

// —— TT-017 按钮 ——
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
            cornerRadius = shapes.scaled(shapes.medium),   // shadcn 风格：按钮 = --radius
            fontSize = typography.bodyLarge.fontSize,
            fontWeight = FontWeight.SemiBold,
            iconSize = control.medium.iconSize,
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.bgDisabled,
            disabledContentColor = colors.textDisabled,
            disabledAlpha = opacity.disabled,
        )
    }
}

// —— TT-018 卡片 ——
@Immutable
data class CardTokens(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val borderWidth: Dp,
    val cornerRadius: Dp,
    val innerPadding: Dp,
    val elevation: Dp,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            spacing: AppSpacing,
            elevation: AppElevation,
        ): CardTokens = CardTokens(
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            borderColor = colors.outline,
            borderWidth = 1.dp,                          // shadcn 风格：卡片带描边
            cornerRadius = shapes.scaled(shapes.medium), // shadcn 风格：卡片 = --radius
            innerPadding = spacing.md,
            elevation = elevation.level2,
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
            height = control.medium.height,
            cornerRadius = shapes.scaled(shapes.medium),
            fontSize = typography.bodyLarge.fontSize,
            borderWidth = 1.dp,
            borderWidthFocus = 2.dp,
            iconSize = control.medium.iconSize,
            containerColor = colors.surface,
            unfocusedBorderColor = colors.outline,
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
            motion: AppMotion,
        ): SelectionControlTokens = SelectionControlTokens(
            size = 20.dp,
            strokeWidth = 2.dp,
            checkedColor = colors.primary,
            uncheckedColor = colors.outline,
            disabledColor = colors.textDisabled,
            animationDurationMs = motion.duration.fast,
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
                backgroundColor = colors.primary.copy(alpha = opacity.subtle),
                textColor = colors.primary,
            ),
            success = TagVariantColors(
                backgroundColor = colors.success.copy(alpha = opacity.subtle),
                textColor = colors.success,
            ),
            warning = TagVariantColors(
                backgroundColor = colors.warning.copy(alpha = opacity.subtle),
                textColor = colors.warning,
            ),
            danger = TagVariantColors(
                backgroundColor = colors.danger.copy(alpha = opacity.subtle),
                textColor = colors.danger,
            ),
            default = TagVariantColors(
                backgroundColor = colors.bgHover,
                textColor = colors.textSecondary,
            ),
            cornerRadius = shapes.scaled(shapes.full),     // shadcn 风格：标签 = rounded-full
            fontSize = typography.label.fontSize,
            fontWeight = FontWeight.Medium,
            horizontalPadding = 12.dp,
            verticalPadding = 6.dp,
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
            height = 6.dp,
            circularSize = 32.dp,
            strokeWidth = 4.dp,
            trackColor = colors.bgHover,
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
            shimmerColor1 = if (darkTheme) Color(0xFF3A3A3A) else Color(0xFFE0E0E0),
            shimmerColor2 = if (darkTheme) Color(0xFF4A4A4A) else Color(0xFFF5F5F5),
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
            trackHeight = 4.dp,
            thumbSize = 20.dp,
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
        ): AppBarTokens = AppBarTokens(
            height = 56.dp,
            titleSize = typography.titleLarge.fontSize,
            titleWeight = FontWeight.SemiBold,
            backIconSize = 22.dp,
            containerColor = colors.primaryContainer,
            titleColor = colors.textPrimary,
            iconColor = colors.primary,
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
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
            typography: AppTypography,
        ): ChipTokens = ChipTokens(
            cornerRadius = shapes.scaled(shapes.full),     // shadcn 风格：Chip = rounded-full
            fontSize = typography.label.fontSize,
            fontWeight = FontWeight.Medium,
            horizontalPadding = 12.dp,
            verticalPadding = 6.dp,
            backgroundColor = colors.surfaceElevated,
            textColor = colors.primary,
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
            cornerRadius = 28.dp,
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
            height = 80.dp,
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
) {
    companion object {
        fun default(
            colors: AppColors,
            spacing: AppSpacing,
            typography: AppTypography,
            opacity: AppOpacity,
        ): ListItemTokens = ListItemTokens(
            minHeight = 56.dp,
            horizontalPadding = spacing.md,
            iconSize = 24.dp,
            titleSize = typography.bodyLarge.fontSize,
            subtitleSize = typography.bodyMedium.fontSize,
            dividerAlpha = opacity.divider,
            titleColor = colors.textPrimary,
            subtitleColor = colors.textSecondary,
            dividerColor = colors.divider,
            actionColor = colors.primary,
        )
    }
}

// —— 图标按钮 ——
@Immutable
data class IconButtonTokens(
    val iconSize: Dp,
    val tintColor: Color,
) {
    companion object {
        fun default(
            colors: AppColors,
            control: AppControlTokens,
        ): IconButtonTokens = IconButtonTokens(
            iconSize = control.medium.iconSize,
            tintColor = colors.primary,
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
            cornerRadius = shapes.scaled(shapes.medium),
            borderWidth = 1.dp,
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
    val cornerRadius: Dp,           // shapes.medium * 2
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
            cornerRadius = shapes.scaled(shapes.medium * 2),
            selectedBackgroundColor = colors.primary.copy(alpha = 0.12f),
            selectedTextColor = colors.primary,
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
    val cornerRadius: Dp,               // shapes.medium * 2
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
            cornerRadius = shapes.scaled(shapes.medium * 2),
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
    val cornerRadius: Dp,       // shapes.medium * 2
    val datePicker: DatePickerTokens,
    val timePicker: TimePickerTokens,
) {
    companion object {
        fun default(
            colors: AppColors,
            shapes: AppShapes,
        ): DateTimeCascadeTokens = DateTimeCascadeTokens(
            backgroundColor = colors.surface,
            cornerRadius = shapes.scaled(shapes.medium * 2),
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
            cornerRadius = shapes.scaled(shapes.large),
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
            containerColor = colors.primaryContainer.copy(alpha = 0.25f),
            selectedContainerColor = colors.surface,
            selectedContentColor = colors.primary,
            unselectedContentColor = colors.textSecondary,
            cornerRadius = shapes.scaled(shapes.large),
            innerCornerRadius = shapes.scaled(shapes.small),
            borderWidth = 2.dp,
            fontSize = typography.label.fontSize,
            fontWeight = FontWeight.Normal,
            selectedFontWeight = FontWeight.SemiBold,
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  TT-030 顶层聚合容器
//  参照 PaletteComponentThemes.default()：统一接收所有基础令牌，分发到各组件
// ═══════════════════════════════════════════════════════════

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
            selectionControl = SelectionControlTokens.default(colors, opacity, motion),
            switch = SwitchTokens.default(colors, elevation),
            table = TableTokens.default(colors, spacing),
            dialog = DialogTokens.default(colors, shapes, spacing, elevation, opacity),
            menu = MenuTokens.default(colors, shapes, spacing, elevation),
            tag = TagTokens.default(colors, shapes, typography, spacing, opacity),
            progress = ProgressTokens.default(colors),
            skeleton = SkeletonTokens.default(shapes, darkTheme),
            steps = StepsTokens.default(colors),
            pagination = PaginationTokens.default(colors, shapes),
            slider = SliderTokens.default(colors),
            rate = RateTokens.default(colors),
            appBar = AppBarTokens.default(colors, typography, control),
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
        )
    }
}

val LocalAppComponentTokens = compositionLocalOf { AppComponentTokens.default() }

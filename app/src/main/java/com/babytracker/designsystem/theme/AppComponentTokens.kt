package com.babytracker.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════
//  组件令牌 — TT-017 ~ TT-029
//  优先级模型（TT-031）: 显式参数 > XxxDefaults > AppTheme > 兜底
//  每个令牌支持 derive {} 部分覆盖（TT-032）
// ═══════════════════════════════════════════════════════════

// —— TT-017 按钮 ——
@Immutable
data class ButtonTokens(
    val height: Dp = 48.dp,
    val cornerRadius: Dp = 24.dp,
    val fontSize: TextUnit = 15.sp,
    val fontWeight: FontWeight = FontWeight.SemiBold,
    val iconSize: Dp = 20.dp,
    val contentColor: Color = Color.Unspecified,
    val disabledAlpha: Float = 0.38f,
)

// —— TT-018 卡片 ——
@Immutable
data class CardTokens(
    val containerColor: Color = Color.Unspecified,
    val borderColor: Color = Color.Unspecified,
    val borderWidth: Dp = 0.dp,
    val cornerRadius: Dp = 16.dp,
    val innerPadding: Dp = 16.dp,
    val elevation: Dp = 2.dp,
)

// —— TT-019 输入框（原 InputTokens） ——
@Immutable
data class InputTokens(
    val height: Dp = 48.dp,
    val cornerRadius: Dp = 12.dp,
    val fontSize: TextUnit = 15.sp,
    val borderWidth: Dp = 1.dp,
    val borderWidthFocus: Dp = 2.dp,
    val iconSize: Dp = 20.dp,
    val containerColor: Color = Color.Unspecified,
    val unfocusedBorderColor: Color = Color.Unspecified,
    val focusedBorderColor: Color = Color.Unspecified,
    val errorBorderColor: Color = Color.Unspecified,
    val placeholderColor: Color = Color.Unspecified,
    val cursorColor: Color = Color.Unspecified,
)

// —— TT-020 下拉菜单 ——
@Immutable
data class SelectTokens(
    val menuItemHeight: Dp = 44.dp,
    val maxHeight: Dp = 300.dp,
    val itemHorizontalPadding: Dp = 16.dp,
    val itemVerticalPadding: Dp = 10.dp,
    val selectedBgColor: Color = Color.Unspecified,
    val hoverBgColor: Color = Color.Unspecified,
    val dividerColor: Color = Color.Unspecified,
)

// —— TT-021 选择控件（复选框/单选） ——
@Immutable
data class SelectionControlTokens(
    val size: Dp = 20.dp,
    val strokeWidth: Dp = 2.dp,
    val checkedColor: Color = Color.Unspecified,
    val uncheckedColor: Color = Color.Unspecified,
    val disabledColor: Color = Color.Unspecified,
    val animationDurationMs: Int = 200,
)

// —— TT-022 开关 ——
@Immutable
data class SwitchTokens(
    val trackWidth: Dp = 40.dp,
    val trackHeight: Dp = 24.dp,
    val thumbSize: Dp = 20.dp,
    val trackCornerRadius: Dp = 12.dp,
    val checkedColor: Color = Color.Unspecified,
    val uncheckedColor: Color = Color.Unspecified,
    val thumbElevation: Dp = 2.dp,
)

// —— TT-023 表格 ——
@Immutable
data class TableTokens(
    val headerHeight: Dp = 48.dp,
    val rowHeight: Dp = 44.dp,
    val cellHorizontalPadding: Dp = 16.dp,
    val cellVerticalPadding: Dp = 8.dp,
    val dividerThickness: Dp = 0.5.dp,
    val headerBgColor: Color = Color.Unspecified,
    val hoverBgColor: Color = Color.Unspecified,
    val selectedBgColor: Color = Color.Unspecified,
)

// —— TT-024 对话框/遮罩 ——
@Immutable
data class DialogTokens(
    val scrimColor: Color = Color.Unspecified,
    val scrimOpacity: Float = 0.40f,
    val cornerRadius: Dp = 24.dp,
    val contentHorizontalPadding: Dp = 24.dp,
    val contentVerticalPadding: Dp = 20.dp,
    val dividerColor: Color = Color.Unspecified,
    val elevation: Dp = 8.dp,
)

// —— TT-025 菜单 ——
@Immutable
data class MenuTokens(
    val itemHeight: Dp = 44.dp,
    val horizontalPadding: Dp = 16.dp,
    val verticalPadding: Dp = 8.dp,
    val minWidth: Dp = 140.dp,
    val cornerRadius: Dp = 12.dp,
    val hoverBgColor: Color = Color.Unspecified,
    val selectedBgColor: Color = Color.Unspecified,
    val elevation: Dp = 4.dp,
)

// —— TT-026 标签语义变体 ——
@Immutable
data class TagVariantColors(
    val backgroundColor: Color,
    val textColor: Color,
)

@Immutable
data class TagTokens(
    val primary: TagVariantColors = TagVariantColors(
        backgroundColor = Color.Unspecified,
        textColor = Color.Unspecified,
    ),
    val success: TagVariantColors = TagVariantColors(
        backgroundColor = Color.Unspecified,
        textColor = Color.Unspecified,
    ),
    val warning: TagVariantColors = TagVariantColors(
        backgroundColor = Color.Unspecified,
        textColor = Color.Unspecified,
    ),
    val danger: TagVariantColors = TagVariantColors(
        backgroundColor = Color.Unspecified,
        textColor = Color.Unspecified,
    ),
    val default: TagVariantColors = TagVariantColors(
        backgroundColor = Color.Unspecified,
        textColor = Color.Unspecified,
    ),
    val cornerRadius: Dp = 20.dp,
    val fontSize: TextUnit = 12.sp,
    val fontWeight: FontWeight = FontWeight.Medium,
    val horizontalPadding: Dp = 12.dp,
    val verticalPadding: Dp = 6.dp,
)

// —— TT-027 进度/骨架屏 ——
@Immutable
data class ProgressTokens(
    val height: Dp = 6.dp,
    val circularSize: Dp = 32.dp,
    val strokeWidth: Dp = 4.dp,
    val trackColor: Color = Color.Unspecified,
    val indicatorColor: Color = Color.Unspecified,
)

@Immutable
data class SkeletonTokens(
    val shimmerColor1: Color = Color(0xFFE0E0E0),
    val shimmerColor2: Color = Color(0xFFF5F5F5),
    val cornerRadius: Dp = 4.dp,
    val avatarSize: Dp = 40.dp,
    val shimmerDurationMs: Int = 1000,
)

// —— TT-028 步骤/分页 ——
@Immutable
data class StepsTokens(
    val dotSize: Dp = 10.dp,
    val connectorThickness: Dp = 2.dp,
    val activeColor: Color = Color.Unspecified,
    val inactiveColor: Color = Color.Unspecified,
    val completedColor: Color = Color.Unspecified,
    val labelStyle: TextStyle = TextStyle.Default,
)

@Immutable
data class PaginationTokens(
    val itemSize: Dp = 32.dp,
    val itemSpacing: Dp = 4.dp,
    val activeColor: Color = Color.Unspecified,
    val inactiveColor: Color = Color.Unspecified,
    val cornerRadius: Dp = 16.dp,
)

// —— TT-029 滑块/评分 ——
@Immutable
data class SliderTokens(
    val trackHeight: Dp = 4.dp,
    val thumbSize: Dp = 20.dp,
    val activeColor: Color = Color.Unspecified,
    val inactiveColor: Color = Color.Unspecified,
)

@Immutable
data class RateTokens(
    val starSize: Dp = 24.dp,
    val starSpacing: Dp = 4.dp,
    val selectedColor: Color = Color.Unspecified,
    val unselectedColor: Color = Color.Unspecified,
)

// —— 导航栏（已有） ——
@Immutable
data class AppBarTokens(
    val height: Dp = 56.dp,
    val titleSize: TextUnit = 18.sp,
    val titleWeight: FontWeight = FontWeight.SemiBold,
    val backIconSize: Dp = 22.dp,
)

// —— 标签（已有） ——
@Immutable
data class ChipTokens(
    val cornerRadius: Dp = 20.dp,
    val fontSize: TextUnit = 12.sp,
    val fontWeight: FontWeight = FontWeight.Medium,
    val horizontalPadding: Dp = 12.dp,
    val verticalPadding: Dp = 6.dp,
)

// —— FAB（已有） ——
@Immutable
data class FabTokens(
    val size: Dp = 56.dp,
    val iconSize: Dp = 24.dp,
    val cornerRadius: Dp = 28.dp,
    val elevation: Dp = 6.dp,
)

// —— 底部导航（已有） ——
@Immutable
data class BottomBarTokens(
    val height: Dp = 64.dp,
    val iconSize: Dp = 22.dp,
    val labelSize: TextUnit = 11.sp,
    val fontWeight: FontWeight = FontWeight.Medium,
)

// —— 列表项（已有） ——
@Immutable
data class ListItemTokens(
    val minHeight: Dp = 56.dp,
    val horizontalPadding: Dp = 16.dp,
    val iconSize: Dp = 24.dp,
    val titleSize: TextUnit = 15.sp,
    val subtitleSize: TextUnit = 13.sp,
    val dividerAlpha: Float = 0.12f,
)

// ═══════════════════════════════════════════════════════════
//  TT-030 顶层聚合容器
// ═══════════════════════════════════════════════════════════

data class AppComponentTokens(
    val button: ButtonTokens = ButtonTokens(),
    val card: CardTokens = CardTokens(),
    val input: InputTokens = InputTokens(),
    val select: SelectTokens = SelectTokens(),
    val selectionControl: SelectionControlTokens = SelectionControlTokens(),
    val switch: SwitchTokens = SwitchTokens(),
    val table: TableTokens = TableTokens(),
    val dialog: DialogTokens = DialogTokens(),
    val menu: MenuTokens = MenuTokens(),
    val tag: TagTokens = TagTokens(),
    val progress: ProgressTokens = ProgressTokens(),
    val skeleton: SkeletonTokens = SkeletonTokens(),
    val steps: StepsTokens = StepsTokens(),
    val pagination: PaginationTokens = PaginationTokens(),
    val slider: SliderTokens = SliderTokens(),
    val rate: RateTokens = RateTokens(),
    val appBar: AppBarTokens = AppBarTokens(),
    val chip: ChipTokens = ChipTokens(),
    val fab: FabTokens = FabTokens(),
    val bottomBar: BottomBarTokens = BottomBarTokens(),
    val listItem: ListItemTokens = ListItemTokens(),
) {
    companion object {
        fun default(colors: AppColors? = null): AppComponentTokens {
            val c = colors
            if (c == null) return AppComponentTokens()
            return AppComponentTokens(
                button = ButtonTokens(
                    contentColor = Color.Transparent,
                ),
                selectionControl = SelectionControlTokens(
                    checkedColor = c.primary,
                    uncheckedColor = c.outline,
                    disabledColor = c.textDisabled,
                ),
                switch = SwitchTokens(
                    checkedColor = c.primary,
                    uncheckedColor = c.outline,
                ),
                table = TableTokens(
                    headerBgColor = c.bgHover,
                    hoverBgColor = c.bgHover,
                    selectedBgColor = c.bgSelected,
                ),
                dialog = DialogTokens(
                    scrimColor = c.scrim,
                    dividerColor = c.divider,
                ),
                menu = MenuTokens(
                    hoverBgColor = c.bgHover,
                    selectedBgColor = c.bgSelected,
                ),
                tag = TagTokens(
                    primary = TagVariantColors(backgroundColor = c.primary.copy(alpha = 0.12f), textColor = c.primary),
                    success = TagVariantColors(backgroundColor = c.success.copy(alpha = 0.12f), textColor = c.success),
                    warning = TagVariantColors(backgroundColor = c.warning.copy(alpha = 0.12f), textColor = c.warning),
                    danger = TagVariantColors(backgroundColor = c.danger.copy(alpha = 0.12f), textColor = c.danger),
                    default = TagVariantColors(backgroundColor = c.bgHover, textColor = c.textSecondary),
                ),
                progress = ProgressTokens(
                    trackColor = c.bgHover,
                    indicatorColor = c.primary,
                ),
                slider = SliderTokens(
                    activeColor = c.primary,
                    inactiveColor = c.borderDisabled,
                ),
                rate = RateTokens(
                    selectedColor = c.warning,
                    unselectedColor = c.textTertiary,
                ),
            )
        }

        fun dark(): AppComponentTokens = AppComponentTokens(
            skeleton = SkeletonTokens(
                shimmerColor1 = Color(0xFF3A3A3A),
                shimmerColor2 = Color(0xFF4A4A4A),
            ),
            listItem = ListItemTokens(dividerAlpha = 0.2f),
            dialog = DialogTokens(scrimOpacity = 0.50f),
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  TT-032 部分覆盖扩展
//  用法: theme.button.derive { height = 56.dp }
// ═══════════════════════════════════════════════════════════

inline fun ButtonTokens.derive(block: ButtonTokens.() -> Unit) = copy().apply(block)
inline fun CardTokens.derive(block: CardTokens.() -> Unit) = copy().apply(block)
inline fun InputTokens.derive(block: InputTokens.() -> Unit) = copy().apply(block)
inline fun SelectTokens.derive(block: SelectTokens.() -> Unit) = copy().apply(block)
inline fun SelectionControlTokens.derive(block: SelectionControlTokens.() -> Unit) = copy().apply(block)
inline fun SwitchTokens.derive(block: SwitchTokens.() -> Unit) = copy().apply(block)
inline fun TableTokens.derive(block: TableTokens.() -> Unit) = copy().apply(block)
inline fun DialogTokens.derive(block: DialogTokens.() -> Unit) = copy().apply(block)
inline fun MenuTokens.derive(block: MenuTokens.() -> Unit) = copy().apply(block)
inline fun TagTokens.derive(block: TagTokens.() -> Unit) = copy().apply(block)
inline fun ProgressTokens.derive(block: ProgressTokens.() -> Unit) = copy().apply(block)
inline fun SkeletonTokens.derive(block: SkeletonTokens.() -> Unit) = copy().apply(block)
inline fun StepsTokens.derive(block: StepsTokens.() -> Unit) = copy().apply(block)
inline fun PaginationTokens.derive(block: PaginationTokens.() -> Unit) = copy().apply(block)
inline fun SliderTokens.derive(block: SliderTokens.() -> Unit) = copy().apply(block)
inline fun RateTokens.derive(block: RateTokens.() -> Unit) = copy().apply(block)
inline fun AppBarTokens.derive(block: AppBarTokens.() -> Unit) = copy().apply(block)
inline fun ChipTokens.derive(block: ChipTokens.() -> Unit) = copy().apply(block)
inline fun FabTokens.derive(block: FabTokens.() -> Unit) = copy().apply(block)
inline fun BottomBarTokens.derive(block: BottomBarTokens.() -> Unit) = copy().apply(block)
inline fun ListItemTokens.derive(block: ListItemTokens.() -> Unit) = copy().apply(block)
inline fun AppComponentTokens.derive(block: AppComponentTokens.() -> Unit) = copy().apply(block)

val LocalAppComponentTokens = staticCompositionLocalOf { AppComponentTokens() }

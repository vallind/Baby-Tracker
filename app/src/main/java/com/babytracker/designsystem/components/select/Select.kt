package com.babytracker.designsystem.components.select

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.divider.DividerDefaults
import com.babytracker.designsystem.components.input.InputDefaults
import com.babytracker.designsystem.components.select.SelectDefaults as AppSelectDefaults
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppElevation
import com.babytracker.designsystem.theme.LocalAppMotion
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 下拉选择器 —— 消费 AppComponentTokens.select（P0 五件套之一，参照组件范式）。
 *
 * Focus 轴：展开即聚焦——触发框边框切换 focused 色（与 AppInput 同语义）；
 * State 轴：选中项底色 selectedBgColor + 选中朗读（AppStrings.selected）；
 * Motion 轴：尾箭头 180° 旋转走 LocalAppMotion；选项列表超 maxHeight 出滚动。
 *
 * 收敛定位：底部弹层单选用 AppOptionPickerSheet，本组件负责"锚定下拉"形态；
 * options 沿用仓库 key→label Pair 风格（与 AppOptionChipRow 一致）。
 *
 * 用法：
 *   var gender by remember { mutableStateOf<String?>(null) }
 *   AppSelect(
 *       options = listOf("male" to AppStrings.male, "female" to AppStrings.female),
 *       selectedKey = gender,
 *       onOptionSelected = { gender = it },
 *       label = AppStrings.gender,
 *   )
 */
@Composable
fun <T> AppSelect(
    options: List<Pair<T, String>>,
    selectedKey: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    placeholder: String = AppStrings.pleaseSelect,
) {
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    val motion = LocalAppMotion.current
    var expanded by remember { mutableStateOf(false) }

    // Motion 参照：箭头随展开态旋转，时长/缓动走令牌
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = motion.duration.fast, easing = motion.easing.standard),
        label = "appSelectChevron",
    )

    val borderColor =
        if (expanded) InputDefaults.focusedBorderColor() else InputDefaults.unfocusedBorderColor()
    val shape = RoundedCornerShape(InputDefaults.cornerRadius())

    Box(modifier) {
        // 触发框：几何读 InputTokens（与输入框同族），状态色表达展开/收起
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(InputDefaults.height())
                .clip(shape)
                .background(InputDefaults.containerColor())
                .border(width = InputDefaults.borderWidth(), color = borderColor, shape = shape)
                .clickable(enabled = enabled, role = Role.DropdownList) { expanded = true }
                .padding(horizontal = LocalAppSpacing.current.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                if (label != null) {
                    Text(
                        label,
                        style = typography.bodySmall,
                        color = if (expanded) InputDefaults.focusedBorderColor() else colors.textSecondary,
                    )
                }
                Text(
                    text = options.firstOrNull { it.first == selectedKey }?.second ?: placeholder,
                    style = typography.bodyLarge,
                    color = if (selectedKey != null) colors.textPrimary else colors.textDisabled,
                    maxLines = 1,
                )
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(InputDefaults.iconSize())
                    .graphicsLayer { rotationZ = chevronRotation },
            )
        }

        // 选项浮层：高度上限/行几何/分隔线颜色全部读 SelectTokens
        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            shape = shape,
            tonalElevation = LocalAppElevation.current.level3,
        ) {
            Column(
                Modifier
                    .heightIn(max = AppSelectDefaults.maxHeight())
                    .verticalScroll(rememberScrollState()),
            ) {
                options.forEachIndexed { i, (key, optionLabel) ->
                    OptionRow(
                        label = optionLabel,
                        isSelected = key == selectedKey,
                        onClick = {
                            expanded = false
                            onOptionSelected(key)
                        },
                    )
                    if (i < options.lastIndex) {
                        HorizontalDivider(
                            color = AppSelectDefaults.dividerColor(),
                            thickness = DividerDefaults.thickness(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppSelectDefaults.menuItemHeight())
            .background(if (isSelected) AppSelectDefaults.selectedBgColor() else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSelectDefaults.itemHorizontalPadding())
            .semantics {
                this.selected = isSelected
                if (isSelected) stateDescription = AppStrings.selected
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) colors.textPrimary else colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Text("✓", color = colors.primary, style = typography.bodyMedium)
        }
    }
}

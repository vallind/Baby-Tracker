package com.babytracker.designsystem.components.colordots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 颜色圆点组 — 一排圆形色点，选中项带 primary 描边圆环。
 *
 * 自 feature/settings 的 ThemeDots 收编：颜色由调用方构造传入（如主题 seed 色），
 * 组件不再持有任何业务映射；描边色默认取主题 primary，可按需覆盖。
 *
 * 用法：
 *   AppColorDots(colors = themeColors, selectedIndex = currentIndex)
 */
@Composable
fun AppColorDots(
    colors: List<Color>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    selectedBorderColor: Color = LocalAppColors.current.primary,
) {
    val spacing = LocalAppSpacing.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colors.forEachIndexed { index, color ->
            Box(
                Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (index == selectedIndex) {
                            Modifier.border(2.dp, selectedBorderColor, CircleShape)
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}

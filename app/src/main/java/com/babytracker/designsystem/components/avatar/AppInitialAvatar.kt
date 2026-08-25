package com.babytracker.designsystem.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 首字头像 — 名字首字圆形徽章。
 *
 * 收敛来源：BabyManagementScreen 在册宝宝卡（44dp）与已删除宝宝行（36dp）
 * 两处内联实现；两处仅尺寸不同，排版以在册宝宝卡为主形态统一
 * （titleMedium + SemiBold，36dp 处由原默认样式归一为主形态排版）。
 * 首字提取照搬原实现：name.take(1)（中文名即取第一个字）。
 *
 * background 为 null 时用主题默认底色（primaryContainer，内容色 primary）；
 * 传入自定义 Brush 时内容色自动切 onPrimary 以保证对比度。
 * 颜色全部读 LocalAppColors，零硬编码色值。
 */
@Composable
fun AppInitialAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    background: Brush? = null,
) {
    val colors = LocalAppColors.current
    val contentColor = if (background == null) colors.primary else colors.onPrimary
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(background ?: SolidColor(colors.primaryContainer)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.take(1),
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            style = LocalAppTypography.current.titleMedium,
        )
    }
}

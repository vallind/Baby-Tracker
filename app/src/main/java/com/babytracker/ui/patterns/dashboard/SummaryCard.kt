package com.babytracker.ui.patterns.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.card.CardVariant
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 统一渐变摘要卡 — 夜间睡眠 / 今日尿布等大数字摘要。
 *
 * 渐变色与内容色由调用方按类别传入（如 secondary 渐变配 onSecondary），
 * 圆角/内边距/透明度统一消费 SummaryCardTokens，避免各页自行拼装。
 */
@Composable
fun AppSummaryCard(
    emoji: String,
    title: String,
    value: String,
    subtitle: String,
    gradient: Brush,
    contentColor: Color = SummaryCardDefaults.contentColor(),
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val cornerRadius = SummaryCardDefaults.cornerRadius()

    AppCard(
        variant = CardVariant.Transparent,
        modifier = modifier,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(gradient, RoundedCornerShape(cornerRadius)),
        ) {
            Column(Modifier.padding(SummaryCardDefaults.innerPadding())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(spacing.xl)
                            .clip(RoundedCornerShape(shapes.medium))
                            .background(contentColor.copy(alpha = SummaryCardDefaults.iconContainerAlpha())),
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, style = LocalAppTypography.current.titleMedium) }
                    Spacer(Modifier.width(spacing.sm))
                    Text(
                        title,
                        style = LocalAppTypography.current.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = contentColor.copy(alpha = SummaryCardDefaults.titleAlpha()),
                    )
                }
                Spacer(Modifier.height(spacing.lg))
                Text(
                    value,
                    style = LocalAppTypography.current.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    subtitle,
                    style = LocalAppTypography.current.bodyMedium,
                    color = contentColor.copy(alpha = SummaryCardDefaults.subtitleAlpha()),
                )
            }
        }
    }
}
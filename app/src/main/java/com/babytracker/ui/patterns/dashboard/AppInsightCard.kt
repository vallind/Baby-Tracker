package com.babytracker.ui.patterns.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 洞察/建议卡 —— AI 分析结论类内容的通用形态（G 批）：
 * 标题行（emoji + 标题）+ 正文 + 可选操作按钮。
 *
 * 领域无关：喂养建议、睡眠洞察、健康提醒都只是不同的入参。
 *
 * 用法：
 *   AppInsightCard(
 *       title = "睡眠洞察",
 *       message = analysis,
 *       actionLabel = "查看详情", onAction = onOpen,
 *   )
 */
@Composable
fun AppInsightCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    emoji: String? = "💡",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(emoji, style = typography.titleMedium)
                Spacer(Modifier.width(spacing.sm))
            }
            Text(title, style = typography.titleMedium)
        }
        Spacer(Modifier.height(spacing.sm))
        Text(
            message,
            style = typography.bodyMedium,
            modifier = Modifier.padding(bottom = spacing.xs),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(spacing.xs))
            AppButton(label = actionLabel, onClick = onAction, variant = ButtonVariant.Ghost)
        }
    }
}
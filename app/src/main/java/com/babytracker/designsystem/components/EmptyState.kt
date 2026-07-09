package com.babytracker.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.button.PrimaryButton

/**
 * 统一空状态组件。所有列表页为空时调用此组件，避免散落各处的 "无数据" 文案。
 *
 * @param emoji 主题 emoji（如 "🍼"）
 * @param title 主标题
 * @param subtitle 副标题（更详细的说明）
 * @param actionText 可选行动按钮文案
 * @param onAction 可选行动按钮回调
 */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = LocalAppTypography.current.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = LocalAppTypography.current.bodySmall,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            PrimaryButton(onClick = onAction, label = actionText, modifier = Modifier.fillMaxWidth())
        }
    }
}

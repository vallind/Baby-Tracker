package com.babytracker.designsystem.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.theme.LocalAppTypography

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
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 主题 emoji 纯装饰，对读屏静默；title/subtitle 保持可朗读
        Text(emoji, fontSize = EmptyStateDefaults.emojiSize(), modifier = Modifier.clearAndSetSemantics {})
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = LocalAppTypography.current.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = EmptyStateDefaults.titleColor(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = LocalAppTypography.current.bodySmall,
            color = EmptyStateDefaults.subtitleColor(),
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            AppButton(label = actionText, onClick = onAction)
        }
    }
}

// AnimatedListItem / animateNumber 已迁移至 Animations.kt

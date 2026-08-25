package com.babytracker.designsystem.components.typingindicator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 打字指示器（AI 回答生成中）— 圆角胶囊内「圆形进度 + 提示文案」。
 *
 * 来源：自 feature/ai 收编（原 feature/ai/AiChatScreen.kt 私有 [AiTypingIndicator]→本组件），
 * 视觉与动效与原实现一致：surfaceMuted 胶囊、primary 转圈、textSecondary 正文；
 * 转圈动画由 [AppCircularProgress] 内部承担（时长走 LocalAppMotion），本组件不含字面量毫秒。
 *
 * @param label 提示文案，默认沿用既有 [AppStrings.aiAnswering]
 */
@Composable
fun AppTypingIndicator(
    modifier: Modifier = Modifier,
    label: String = AppStrings.aiAnswering,
) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    Row(
        modifier.padding(vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceMuted)
                .padding(horizontal = spacing.md, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppCircularProgress(indicatorColor = colors.primary)
                Spacer(Modifier.width(spacing.sm))
                Text(
                    text = label,
                    style = typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

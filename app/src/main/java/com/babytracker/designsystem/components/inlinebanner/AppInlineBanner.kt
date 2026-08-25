package com.babytracker.designsystem.components.inlinebanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/** 内联横幅严重级：映射对应语义色阶（Info=primaryScale / Warning=warningScale / Error=dangerScale） */
enum class AppBannerSeverity { Info, Warning, Error }

/**
 * 内联横幅 —「浅底圆角强调色」提示条家族的统一承载。
 *
 * 视觉规格（取自存量横幅众数，几何/颜色全部走 InlineBannerTokens）：
 * - 容器：severity 对应语义色阶的 tintContainer 浅底 + 16dp 圆角；
 * - 内容：accentContent 强调色文字（bodyMedium），emoji 可选前置（对读屏静默）；
 * - 动作：[actionLabel] 与 [onAction] 同时非空时渲染尾部 Ghost 文字按钮
 *   （对齐存量 AiErrorBanner「重试」/AiAnalysisContextBar「移除」形态）。
 *
 * 组件自身撑满可用宽度；外边距由调用方经 [modifier] 追加，
 * 与迁移前调用点的 `Modifier.fillMaxWidth().padding(horizontal = md)` 链保持一致。
 *
 * @param message 提示文案
 * @param severity 语义档位
 * @param emoji 可选前置装饰 emoji
 * @param actionLabel 尾部动作文案；与 [onAction] 同时非空才渲染
 * @param onAction 尾部动作回调
 */
@Composable
fun AppInlineBanner(
    message: String,
    modifier: Modifier = Modifier,
    severity: AppBannerSeverity = AppBannerSeverity.Info,
    emoji: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val contentColor = InlineBannerDefaults.contentColor(severity)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(InlineBannerDefaults.cornerRadius()))
            .background(InlineBannerDefaults.containerColor(severity))
            .padding(
                horizontal = InlineBannerDefaults.horizontalPadding(),
                vertical = InlineBannerDefaults.verticalPadding(),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) {
            // 装饰性 emoji 对读屏静默（同 EmptyState 约定）
            Text(
                text = emoji,
                style = LocalAppTypography.current.bodyMedium,
                color = contentColor,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Spacer(Modifier.width(LocalAppSpacing.current.xs))
        }
        Text(
            text = message,
            style = LocalAppTypography.current.bodyMedium,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            AppButton(
                label = actionLabel,
                onClick = onAction,
                variant = ButtonVariant.Ghost,
            )
        }
    }
}

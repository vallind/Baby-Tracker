package com.babytracker.feature.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.cardgroup.AppCardGroup
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.components.scaffold.SubPageScaffold
import com.babytracker.ui.patterns.settings.AppSettingItem
import com.babytracker.designsystem.components.switchcontrol.AppSwitch
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.ui.i18n.AppStringsProduct
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 帮助与关于 — 纯 UI 渲染层（Batch 4 自 SettingsMenuScreen 拆出）。
 * 日志抓取开关状态与版本号由 Route 收集传入；开关动作经回调回 Route → SettingsViewModel。
 */
@Composable
fun SupportSettingsScreen(
    logCaptureEnabled: Boolean,
    versionName: String,
    onBack: () -> Unit,
    onToggleLogCapture: (Boolean) -> Unit,
    onOpenLogViewer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current

    SubPageScaffold(
        title = "帮助与关于",
        onBack = onBack,
    ) {
        AppCardGroup {
            AppSettingItem(
                emoji = "❓",
                label = "帮助与反馈",
                subtitle = "使用问题与意见反馈",
                trailing = { AppChip(label = AppStringsProduct.comingSoon) },
            )
            AppDivider(horizontalInset = spacing.md)
            AppSettingItem(
                emoji = "📋",
                label = "运行日志",
                subtitle = if (logCaptureEnabled) "日志抓取中，点击查看" else "已关闭",
                trailing = {
                    AppSwitch(
                        checked = logCaptureEnabled,
                        onCheckedChange = onToggleLogCapture,
                    )
                },
                onClick = onOpenLogViewer,
            )
            AppDivider(horizontalInset = spacing.md)
            AppSettingItem(
                emoji = "ℹ️",
                label = "关于 Baby Tracker",
                subtitle = "版本 $versionName",
            )
        }
        Spacer(Modifier.height(spacing.md))
    }
}
package com.babytracker.feature.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.cardgroup.AppCardGroup
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 数据与同步 — 纯 UI 渲染层（Batch 4 自 SettingsMenuScreen 拆出）。
 * 展示字段由 Route 从 SettingsViewModel 收集传入。
 */
@Composable
fun DataSettingsScreen(
    autoSync: Boolean,
    syncDelayLabel: String,
    onBack: () -> Unit,
    onOpenSyncSettings: () -> Unit,
    onOpenBackup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current

    SettingsMenuScaffold(
        title = "数据与同步",
        onBack = onBack,
    ) {
        AppCardGroup {
            SettingsRow(
                emoji = "🔄",
                label = "同步设置",
                subtitle = if (autoSync) {
                    "已开启 / 延迟 ${syncDelayLabel}"
                } else {
                    "已关闭"
                },
                onClick = onOpenSyncSettings,
            )
            SettingsDivider()
            SettingsRow(
                emoji = "📦",
                label = "备份与恢复",
                subtitle = "本地备份、WebDAV 与数据恢复",
                onClick = onOpenBackup,
            )
            SettingsDivider()
            SettingsRow(
                emoji = "🔐",
                label = "隐私设置",
                subtitle = "管理数据与隐私选项",
                trailing = { AppChip(label = AppStrings.comingSoon) },
            )
        }
        Spacer(Modifier.height(spacing.md))
    }
}
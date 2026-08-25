package com.babytracker.designsystem.components.settingitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.babytracker.designsystem.components.section.AppListItem
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 设置行 — emoji 徽章 + 标题/副标题 + 尾部插槽的设置项家族主行。
 *
 * 自 feature/settings 的 SettingsRow 收编：40dp 圆角 emoji 徽章（primaryContainer 底）、
 * 标题/副标题、默认尾部 ChevronRight；视觉与原 SettingsRow 完全一致。
 * 颜色/字号/徽章规格全部走 AppComponentTokens.settingItem（见 SettingItemDefaults）。
 *
 * 用法：
 *   AppSettingItem(emoji = "🔄", label = "同步设置", subtitle = "已开启", onClick = { ... })
 *   AppSettingItem(emoji = "🔐", label = "隐私设置", showChevron = false)
 */
@Composable
fun AppSettingItem(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showChevron: Boolean = true,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val resolvedTrailing: (@Composable () -> Unit)? = trailing ?: if (showChevron) {
        {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SettingItemDefaults.chevronTint(),
                modifier = Modifier.size(SettingItemDefaults.trailingIconSize()),
            )
        }
    } else {
        null
    }
    AppListItem(
        leadingContent = {
            Box(
                Modifier
                    .size(SettingItemDefaults.badgeSize())
                    .clip(RoundedCornerShape(SettingItemDefaults.badgeCornerRadius()))
                    .background(SettingItemDefaults.badgeContainerColor()),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    emoji,
                    style = LocalAppTypography.current.titleMedium.copy(
                        fontSize = SettingItemDefaults.badgeFontSize(),
                    ),
                )
            }
        },
        headlineContent = {
            Text(
                label,
                style = LocalAppTypography.current.bodyMedium.copy(fontSize = SettingItemDefaults.titleFontSize()),
                color = SettingItemDefaults.titleColor(),
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    it,
                    style = LocalAppTypography.current.bodySmall.copy(fontSize = SettingItemDefaults.subtitleFontSize()),
                    color = SettingItemDefaults.subtitleColor(),
                )
            }
        },
        trailingContent = resolvedTrailing,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    )
}

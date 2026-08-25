package com.babytracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.section.AppListItem
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

// ═══════════════════════════════════════════════════════════
//  Settings 共享 UI 组件（自 SettingsScreen.kt 拆出，Batch 4）
//  纯 UI 切片，不承载任何业务逻辑
//  G3 收编：通用卡组容器删除，调用点直接改用 designsystem 的 AppCardGroup
// ═══════════════════════════════════════════════════════════

@Composable
fun SettingsDivider() {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    AppDivider(
        color = c.divider,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = spacing.md),
    )
}

@Composable
fun SettingsRow(emoji: String, label: String, subtitle: String? = null, trailing: @Composable (() -> Unit)? = null, onClick: () -> Unit = {}) {
    val c = LocalAppColors.current
    val shapes = LocalAppShapes.current
    AppListItem(
        leadingContent = {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, style = LocalAppTypography.current.titleMedium)
            }
        },
        headlineContent = {
            Text(label, style = LocalAppTypography.current.bodyMedium, color = c.textPrimary)
        },
        supportingContent = subtitle?.let {
            {
                Text(it, style = LocalAppTypography.current.bodySmall, color = c.textTertiary)
            }
        },
        trailingContent = trailing ?: {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        },
        onClick = onClick,
    )
}

@Composable
fun SettingsSectionTitle(title: String) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    Text(
        title,
        style = LocalAppTypography.current.labelMedium,
        color = c.textSecondary,
        modifier = Modifier.padding(bottom = spacing.sm),
    )
}

/** 设置子页通用 Scaffold（标题 + 返回 + 纵向滚动） */
@Composable
fun SettingsMenuScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    AppScaffold(
        topBar = {
            AppTopBar(
                title = title,
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md, vertical = spacing.md),
        ) {
            content()
        }
    }
}

@Composable
fun ThemeDots(currentTheme: String, onClick: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val themesColors = mapOf(
        "pure" to 0xFF3B6FE0, "aurora" to 0xFF8B7BF0, "warm" to 0xFFEF7967,
        "sunny" to 0xFFF0A43B, "night" to 0xFF8FA7F9, "morandi" to 0xFF9AAE8F,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        themesColors.forEach { (name, colorInt) ->
            Box(
                Modifier.size(20.dp).clip(CircleShape).background(Color(colorInt))
                    .then(if (name == currentTheme) Modifier.border(2.dp, c.primary, CircleShape) else Modifier),
            )
        }
    }
}
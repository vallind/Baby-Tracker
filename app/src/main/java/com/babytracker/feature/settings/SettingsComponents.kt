package com.babytracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.cardgroup.AppCardGroup
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.section.AppListItem
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppElevation
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

// ═══════════════════════════════════════════════════════════
//  Settings 共享 UI 组件（自 SettingsScreen.kt 拆出，Batch 4）
//  纯 UI 切片，不承载任何业务逻辑
// ═══════════════════════════════════════════════════════════

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    AppCardGroup(content = content)
}

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
fun UserInfoCard(
    babyName: String,
    displayAccount: String?,
    nickname: String?,
    isLoggedIn: Boolean,
    onClick: (() -> Unit)? = null,
    onEditNickname: (() -> Unit)? = null,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val elev = LocalAppElevation.current
    val displayName = nickname ?: displayAccount ?: babyName

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Gradients.primary(c)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    displayName.take(1).ifEmpty { "?" },
                    style = LocalAppTypography.current.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.onPrimary,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = LocalAppTypography.current.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                    )
                    if (isLoggedIn && onEditNickname != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "修改昵称",
                            tint = c.textTertiary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = onEditNickname),
                        )
                    }
                }
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = if (isLoggedIn && displayAccount != null) {
                        if (nickname != null) "账号: ${displayAccount.take(8)}…" else "ID: ${displayAccount.take(8)}…"
                    } else "点击登录账号",
                    style = LocalAppTypography.current.bodySmall,
                    color = c.textTertiary,
                    maxLines = 1,
                )
            }

            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = c.textTertiary,
                    modifier = Modifier.size(20.dp),
                )
            }
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
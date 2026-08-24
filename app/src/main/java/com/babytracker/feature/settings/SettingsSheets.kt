package com.babytracker.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.babytracker.core.settings.DensityController
import com.babytracker.core.settings.ThemeController
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.AppDensity
import com.babytracker.designsystem.theme.AppTheme
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppElevation
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

// ═══════════════════════════════════════════════════════════
//  主题 / 密度选择弹层
//  从 SettingsScreen.kt 拆出：避免 *Screen.kt 文件直接依赖 Controller
//  （Screen 边界审计：Screen 只收 state + 回调，控制器由调用方注入）
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(themeCtrl: ThemeController, onDismiss: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val elev = LocalAppElevation.current
    val names = mapOf("pure" to "纯净蓝", "aurora" to "极光紫", "warm" to "暖阳粉", "sunny" to "阳光黄", "night" to "暗夜深", "morandi" to "莫兰迪")
    AppBottomSheet(
        show = true,
        onDismiss = onDismiss,
    ) {
        Column(Modifier.padding(spacing.md)) {
            Text("选择主题", style = LocalAppTypography.current.titleLarge, modifier = Modifier.padding(bottom = 20.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                AppTheme.all.forEach { theme ->
                    val selected = themeCtrl.currentTheme.name == theme.name
                    AppCard(
                        modifier = Modifier
                            .width(120.dp)
                            .height(96.dp)
                            .border(
                                if (selected) BorderStroke(2.dp, c.primary) else BorderStroke(1.dp, c.outline),
                                RoundedCornerShape(shapes.large),
                            )
                            .clickable { themeCtrl.switchTheme(theme.name) },
                        elevation = elev.level1,
                        containerColor = theme.colors.surface,
                    ) {
                        Box(Modifier.fillMaxSize().padding(spacing.md)) {
                            Column {
                                // 2.1：主题色条预览（主→次→第三色渐变条），替代单色方块
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(shapes.large))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(theme.colors.primary, theme.colors.secondary, theme.colors.tertiary),
                                            ),
                                        ),
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(names[theme.name] ?: theme.name, style = LocalAppTypography.current.bodySmall, color = theme.colors.textPrimary)
                            }
                            if (selected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = c.primary, modifier = Modifier.align(Alignment.TopEnd).size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DensityPickerSheet(ctrl: DensityController, onDismiss: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val elev = LocalAppElevation.current
    AppBottomSheet(
        show = true,
        onDismiss = onDismiss,
    ) {
        Column(Modifier.padding(spacing.md)) {
            Text(AppStrings.densityLabel, style = LocalAppTypography.current.titleLarge, modifier = Modifier.padding(bottom = 20.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                AppDensity.entries.forEach { density ->
                    val selected = ctrl.currentDensity == density
                    AppCard(
                        modifier = Modifier
                            .width(120.dp)
                            .height(96.dp)
                            .border(
                                if (selected) BorderStroke(2.dp, c.primary) else BorderStroke(1.dp, c.outline),
                                RoundedCornerShape(shapes.large),
                            )
                            .clickable { ctrl.switchDensity(density) },
                        elevation = elev.level1,
                    ) {
                        Box(Modifier.fillMaxSize().padding(spacing.md)) {
                            Column {
                                Box(
                                    Modifier.size(36.dp)
                                        .clip(RoundedCornerShape(shapes.large))
                                        .background(if (selected) c.primary else c.surfaceElevated)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(density.label, style = LocalAppTypography.current.bodySmall, color = c.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
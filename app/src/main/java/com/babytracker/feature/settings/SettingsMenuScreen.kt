package com.babytracker.feature.settings
import com.babytracker.core.ui.AppSpacing

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Sync
import com.babytracker.navigation.Navigator
import com.babytracker.core.ui.components.scaffold.AppScaffold
import com.babytracker.core.ui.components.switchcontrol.AppSwitch
import com.babytracker.core.ui.components.topbar.AppTopBar
import com.babytracker.i18n.AppStrings
import com.babytracker.core.ui.DensityController
import com.babytracker.core.ui.ThemeController
import com.babytracker.navigation.Route
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PreferenceSettingsScreen(navigator: Navigator) {
    val spacing = com.babytracker.core.ui.AppSpacing
    val themeCtrl: ThemeController = koinInject()
    val densityCtrl: DensityController = koinInject()
    var showThemePicker by remember { mutableStateOf(false) }
    var showDensityPicker by remember { mutableStateOf(false) }

    SettingsMenuScaffold(
        title = "使用偏好",
        navigator = navigator,
    ) {
        SettingsCard {
            SettingsRow(
                icon = Icons.Filled.Palette,
                label = "主题模式",
                subtitle = themeNameLabel(themeCtrl.currentThemeName),
                onClick = { showThemePicker = true },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Filled.Straighten,
                label = AppStrings.densityLabel,
                subtitle = densityCtrl.currentDensity.label,
                onClick = { showDensityPicker = true },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Filled.Star,
                label = AppStrings.aiSettings,
                subtitle = "模型、宝宝数据与回答偏好",
                onClick = { navigator.navigate(Route.AiSettings) },
            )
        }
        Spacer(Modifier.height(spacing.md))
    }

    if (showThemePicker) {
        ThemePickerSheet(themeCtrl = themeCtrl, onDismiss = { showThemePicker = false })
    }

    if (showDensityPicker) {
        DensityPickerSheet(ctrl = densityCtrl, onDismiss = { showDensityPicker = false })
    }
}

@Composable
fun DataSettingsScreen(navigator: Navigator) {
    val context = LocalContext.current
    val settingsVM: SettingsViewModel = koinViewModel()
    val settings by settingsVM.settings.collectAsState()
    val syncConfig = settings.sync

    SettingsMenuScaffold(
        title = "数据与同步",
        navigator = navigator,
    ) {
        SettingsCard {
            SettingsRow(
                icon = Icons.Filled.Sync,
                label = "同步设置",
                subtitle = if (syncConfig.autoSync) {
                    "已开启 / 延迟 ${syncConfig.syncDelay.label}"
                } else {
                    "已关闭"
                },
                onClick = { navigator.navigate(Route.SyncSettings) },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Filled.Archive,
                label = "备份与恢复",
                subtitle = "本地备份、WebDAV 与数据恢复",
                onClick = { navigator.navigate(Route.Backup) },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Filled.Lock,
                label = "隐私设置",
                subtitle = "管理数据与隐私选项",
                onClick = {
                    Toast.makeText(context, "即将上线", Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}

@Composable
fun SupportSettingsScreen(navigator: Navigator) {
    val context = LocalContext.current
    val settingsVM: SettingsViewModel = koinViewModel()
    val settings by settingsVM.settings.collectAsState()
    val logCaptureEnabled = settings.diagnostics.logCaptureEnabled
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    SettingsMenuScaffold(
        title = "帮助与关于",
        navigator = navigator,
    ) {
        SettingsCard {
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Help,
                label = "帮助与反馈",
                subtitle = "使用问题与意见反馈",
                onClick = {
                    Toast.makeText(context, "即将上线", Toast.LENGTH_SHORT).show()
                },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Filled.Description,
                label = "运行日志",
                subtitle = if (logCaptureEnabled) "日志抓取中，点击查看" else "已关闭",
                trailing = {
                    AppSwitch(
                        checked = logCaptureEnabled,
                        onCheckedChange = { enabled ->
                            settingsVM.updateSettings { current ->
                                current.copy(
                                    diagnostics = current.diagnostics.copy(
                                        logCaptureEnabled = enabled,
                                    ),
                                )
                            }
                            val app = context.applicationContext as com.babytracker.BabyTrackerApp
                            app.appLogTree.enabled = enabled
                        },
                    )
                },
                onClick = { navigator.navigate(Route.LogViewer) },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Filled.Info,
                label = "关于 Baby Tracker",
                subtitle = "版本 $versionName",
            )
        }
    }
}

@Composable
private fun SettingsMenuScaffold(
    title: String,
    navigator: Navigator,
    content: @Composable () -> Unit,
) {
    val spacing = com.babytracker.core.ui.AppSpacing
    AppScaffold(
        topBar = {
            AppTopBar(
                title = title,
                onBack = { navigator.pop() },
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

private fun themeNameLabel(name: String): String = when (name) {
    "pure" -> "纯净蓝"
    "aurora" -> "极光紫"
    "warm" -> "暖阳粉"
    "sunny" -> "阳光黄"
    "night" -> "暗夜深"
    "morandi" -> "莫兰迪"
    else -> "跟随系统"
}

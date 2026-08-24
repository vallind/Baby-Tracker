package com.babytracker.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.i18n.AppStrings

/**
 * 我的（设置主页）— 纯 UI 渲染层（Batch 4）。
 * 登录态/当前宝宝/昵称在 SettingsViewModel；弹层显隐留 Screen。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    bottomBar: @Composable () -> Unit = {},
    onOpenUserAccount: () -> Unit,
    onOpenBabyManagement: () -> Unit,
    onOpenReminder: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenLogViewer: () -> Unit,
    onOpenPreference: () -> Unit,
    onOpenData: () -> Unit,
    onOpenSupport: () -> Unit,
    onLogout: () -> Unit,
    onSaveNickname: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val isLoggedIn = state.isLoggedIn
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "我的",
                showBack = false,
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
        ) {
            Spacer(Modifier.height(spacing.md))

            UserInfoCard(
                babyName = state.baby?.name ?: "未设置",
                displayAccount = state.displayAccount,
                nickname = state.nickname,
                isLoggedIn = isLoggedIn,
                onClick = onOpenUserAccount,
                onEditNickname = if (isLoggedIn) {
                    { showNicknameDialog = true }
                } else null,
            )

            Spacer(Modifier.height(spacing.md))

            SettingsSectionTitle("宝宝与家庭")
            SettingsCard {
                SettingsRow(
                    emoji = "👶",
                    label = "宝宝管理",
                    subtitle = "资料、成长信息与宝宝切换",
                    onClick = onOpenBabyManagement,
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "👨‍👩‍👧",
                    label = "家庭与账号",
                    subtitle = if (isLoggedIn) "成员管理与账号信息" else "登录后与家人共享记录",
                    onClick = onOpenUserAccount,
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "🔔",
                    label = "提醒设置",
                    subtitle = "喂养、睡眠与护理提醒",
                    onClick = onOpenReminder,
                )
            }

            Spacer(Modifier.height(spacing.md))

            // 快捷工具组（2.1 H7：高频工具从深层上提，原深层路由保留）
            SettingsSectionTitle("快捷工具")
            SettingsCard {
                SettingsRow(
                    emoji = "📦",
                    label = AppStrings.backupManage,
                    subtitle = AppStrings.backupManageSubtitle,
                    onClick = onOpenBackup,
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "📋",
                    label = AppStrings.logViewer,
                    subtitle = AppStrings.logViewerSubtitle,
                    onClick = onOpenLogViewer,
                )
            }

            Spacer(Modifier.height(spacing.md))

            SettingsSectionTitle("更多设置")
            SettingsCard {
                SettingsRow(
                    emoji = "🎨",
                    label = "使用偏好",
                    subtitle = "主题与 AI 助手",
                    onClick = onOpenPreference,
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "🔒",
                    label = "数据与同步",
                    subtitle = "云同步、备份与隐私",
                    onClick = onOpenData,
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "❓",
                    label = "帮助与关于",
                    subtitle = "问题反馈、运行日志与版本信息",
                    onClick = onOpenSupport,
                )
            }

            Spacer(Modifier.height(spacing.lg))

            if (isLoggedIn) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = spacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    AppButton(
                        variant = ButtonVariant.Danger,
                        onClick = { showLogoutConfirm = true },
                        label = "退出登录",
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))
        }
    }

    AppConfirmDialog(
        show = showLogoutConfirm,
        title = "退出登录",
        message = "退出后数据保留在本地，云同步将停止。确定退出？",
        confirmText = "退出",
        onConfirm = {
            onLogout()
            showLogoutConfirm = false
        },
        onDismiss = { showLogoutConfirm = false },
    )

    if (showNicknameDialog) {
        NicknameEditDialog(
            currentNickname = state.nickname ?: "",
            onDismiss = { showNicknameDialog = false },
            onSave = { newNickname ->
                onSaveNickname(newNickname)
                showNicknameDialog = false
            },
        )
    }
}
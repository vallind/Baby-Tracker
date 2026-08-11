package com.babytracker.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.input.AppInput
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val vm: LoginViewModel = koinInject()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val c = LocalAppColors.current
    val typography = LocalAppTypography.current
    val spacing = LocalAppSpacing.current


    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) navController.popBackStack()
    }

    AppScaffold(
        topBar = {
            AppTopBar(title = "账户", onBack = { navController.popBackStack() })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                text = if (uiState.isRegisterMode) "创建账户" else "登录账户",
                style = typography.headlineLarge, color = c.textPrimary,
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = if (uiState.isRegisterMode) "注册后可开启云同步和家庭共享" else "登录后同步数据到云端",
                style = typography.bodyLarge, color = c.textSecondary,
            )
            Spacer(Modifier.height(spacing.xl))

            AppInput(
                value = uiState.account, onValueChange = vm::onAccountChange,
                label = "账户名",
                placeholder = "请输入账户名",
                keyboardType = KeyboardType.Text,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
            )
            Spacer(Modifier.height(spacing.md))

            AppInput(
                value = uiState.password, onValueChange = vm::onPasswordChange,
                label = "密码",
                placeholder = "请输入密码（至少 6 位）",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
            )
            Spacer(Modifier.height(spacing.sm))

            uiState.errorMessage?.let {
                Text(it, color = c.error, style = typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(vertical = spacing.xs))
            }
            Spacer(Modifier.height(spacing.lg))

            AppButton(
                onClick = vm::submit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                label = if (uiState.isLoading) "..." else if (uiState.isRegisterMode) "注册" else "登录",
            )
            Spacer(Modifier.height(spacing.md))

            AppButton(
                variant = ButtonVariant.Text,
                onClick = vm::toggleMode,
                label = if (uiState.isRegisterMode) "已有账户？去登录" else "没有账户？去注册",
            )
            Spacer(Modifier.height(spacing.xl))
            Text("登录为可选操作，不登录不影响本地使用", style = typography.labelSmall, color = c.textTertiary)
            Spacer(Modifier.height(spacing.md))
        }
    }
}

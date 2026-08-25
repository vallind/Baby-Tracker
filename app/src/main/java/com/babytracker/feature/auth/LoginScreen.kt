package com.babytracker.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.i18n.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginViewModel.UiState,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val typography = LocalAppTypography.current
    val spacing = LocalAppSpacing.current

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(title = AppStrings.accountPage, onBack = onBack)
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
            Spacer(Modifier.height(24.dp))

            // —— 品牌 hero：渐变光环 + 大标题 ——
            Box(
                Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(listOf(c.primary, c.secondary))),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(c.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🍼", style = typography.displayLarge)
                }
            }
            Spacer(Modifier.height(spacing.lg))

            Text(
                text = if (state.isRegisterMode) AppStrings.registerTitle else AppStrings.loginTitle,
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = if (state.isRegisterMode) AppStrings.registerSubtitle else AppStrings.loginSubtitle,
                style = typography.bodyLarge,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.xl))

            // —— 表单卡片 ——
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(spacing.lg)) {
                    AppInput(
                        value = state.account, onValueChange = onAccountChange,
                        label = AppStrings.accountLabel,
                        placeholder = AppStrings.accountPlaceholder,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                    )
                    Spacer(Modifier.height(spacing.md))

                    AppInput(
                        value = state.password, onValueChange = onPasswordChange,
                        label = AppStrings.passwordLabel,
                        placeholder = AppStrings.passwordPlaceholder,
                        isPassword = true,
                        keyboardType = KeyboardType.Password,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        // 2.1：错误提示走输入框 errorMessage 通道（替代独立 Text）
                        isError = state.errorMessage != null,
                        errorMessage = state.errorMessage,
                    )
                    Spacer(Modifier.height(spacing.lg))

                    AppButton(
                        onClick = onSubmit,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        label = if (state.isLoading) AppStrings.loading
                        else if (state.isRegisterMode) AppStrings.register else AppStrings.login,
                    )
                    Spacer(Modifier.height(spacing.xs))

                    AppButton(
                        variant = ButtonVariant.Ghost,
                        onClick = onToggleMode,
                        label = if (state.isRegisterMode) AppStrings.toLogin else AppStrings.toRegister,
                    )
                }
            }
            Spacer(Modifier.height(spacing.xl))
            Text(AppStrings.loginOptional, style = typography.labelSmall, color = c.textTertiary)
            Spacer(Modifier.height(spacing.md))
        }
    }
}
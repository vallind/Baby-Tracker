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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
            AppTopBar(title = AppStrings.accountPage, onBack = { navController.popBackStack() })
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
                text = if (uiState.isRegisterMode) AppStrings.registerTitle else AppStrings.loginTitle,
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = if (uiState.isRegisterMode) AppStrings.registerSubtitle else AppStrings.loginSubtitle,
                style = typography.bodyLarge,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.xl))

            // —— 表单卡片 ——
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(spacing.lg)) {
                    AppInput(
                        value = uiState.account, onValueChange = vm::onAccountChange,
                        label = AppStrings.accountLabel,
                        placeholder = AppStrings.accountPlaceholder,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                    )
                    Spacer(Modifier.height(spacing.md))

                    AppInput(
                        value = uiState.password, onValueChange = vm::onPasswordChange,
                        label = AppStrings.passwordLabel,
                        placeholder = AppStrings.passwordPlaceholder,
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
                        label = if (uiState.isLoading) "..." else if (uiState.isRegisterMode) AppStrings.register else AppStrings.login,
                    )
                    Spacer(Modifier.height(spacing.xs))

                    AppButton(
                        variant = ButtonVariant.Text,
                        onClick = vm::toggleMode,
                        label = if (uiState.isRegisterMode) AppStrings.toLogin else AppStrings.toRegister,
                    )
                }
            }
            Spacer(Modifier.height(spacing.xl))
            Text(AppStrings.loginOptional, style = typography.labelSmall, color = c.textTertiary)
            Spacer(Modifier.height(spacing.md))
        }
    }
}
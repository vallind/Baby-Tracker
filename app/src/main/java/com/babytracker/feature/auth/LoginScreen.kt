package com.babytracker.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.babytracker.designsystem.components.button.PrimaryButton
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import org.koin.compose.koinInject

/**
 * 登录 / 注册页面。
 *
 * 登录为可选操作：不强制登录，用户从 Settings → 账户 进入。
 * 支持账户名 + 密码登录，也可切换到注册模式。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val vm: LoginViewModel = koinInject()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val c = LocalAppColors.current
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            AppTopBar(title = "账户", onBack = { navController.popBackStack() })
        },
        containerColor = c.pageBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                text = if (uiState.isRegisterMode) "创建账户" else "登录账户",
                fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (uiState.isRegisterMode) "注册后可开启云同步和家庭共享" else "登录后同步数据到云端",
                fontSize = 14.sp, color = c.textSecondary,
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.account, onValueChange = vm::onAccountChange,
                label = { Text("账户名") }, placeholder = { Text("请输入账户名") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password, onValueChange = vm::onPasswordChange,
                label = { Text("密码") }, placeholder = { Text("请输入密码（至少 6 位）") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { vm.submit() }),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
            )
            Spacer(Modifier.height(8.dp))

            uiState.errorMessage?.let {
                Text(it, color = c.error, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            }
            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                onClick = vm::submit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                label = if (uiState.isLoading) "..." else if (uiState.isRegisterMode) "注册" else "登录",
            )
            Spacer(Modifier.height(16.dp))

            TextButton(onClick = vm::toggleMode) {
                Text(
                    text = if (uiState.isRegisterMode) "已有账户？去登录" else "没有账户？去注册",
                    color = c.primary, fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(32.dp))
            Text("登录为可选操作，不登录不影响本地使用", fontSize = 12.sp, color = c.textTertiary)
            Spacer(Modifier.height(16.dp))
        }
    }
}

package com.babytracker.feature.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.button.PrimaryButton
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.markdown.AppMarkdownText
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypographyStyle
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun AiChatScreen(navController: NavController) {
    val viewModel: AiChatViewModel = koinViewModel()
    val babyController: BabyController = koinInject()
    val state by viewModel.state.collectAsState()
    val currentBabyId = babyController.currentBabyId
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember(snackbarHostState) { AppSnackbar(snackbarHostState) }
    val copyAnswer: (String) -> Unit = { answer ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(AppStrings.aiAssistant, answer))
        if (state.preferences.showCopyFeedback) {
            scope.launch { appSnackbar.showMessage(AppStrings.aiCopied) }
        }
    }

    LaunchedEffect(currentBabyId) {
        viewModel.selectBaby(currentBabyId)
    }
    LaunchedEffect(state.messages.size, state.isSending) {
        if (state.preferences.autoScroll && state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = AppStrings.aiAssistant,
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AiSettings.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = AppStrings.aiSettings,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LocalAppColors.current.pageBackground),
        ) {
            AiBabySummary(state)
            AiModelSelector(
                state = state,
                onSelect = viewModel::selectModel,
                onRefresh = viewModel::refreshConfig,
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = LocalAppSpacing.current.md,
                    vertical = LocalAppSpacing.current.sm,
                ),
                verticalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.sm),
            ) {
                if (state.messages.isEmpty() && state.preferences.showRecommendedQuestions) {
                    item { AiWelcomeCard(onQuestion = viewModel::updateInput) }
                }
                items(state.messages, key = { it.id }) { message ->
                    AiMessageBubble(
                        message = message,
                        renderMarkdown = state.preferences.renderMarkdown,
                        onCopy = copyAnswer,
                    )
                }
                if (state.isSending) {
                    item { AiTypingIndicator() }
                }
            }

            state.error?.takeUnless { it == AiChatError.CONFIG_UNAVAILABLE }?.let { error ->
                AiErrorBanner(
                    error = error,
                    canRetry = state.messages.lastOrNull()?.role == AiChatRole.USER && !state.isSending,
                    onRetry = viewModel::retry,
                )
            }
            AiComposer(
                state = state,
                onInputChange = viewModel::updateInput,
                onSend = viewModel::send,
                onStop = viewModel::stop,
            )
        }
    }
}

@Composable
private fun AiBabySummary(state: AiChatUiState) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypographyStyle.current
    val baby = state.baby
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md, vertical = spacing.sm)
            .fillMaxWidth(),
        containerColor = colors.primaryContainer,
    ) {
        Column(Modifier.padding(spacing.md)) {
            Text(
                text = baby?.let {
                    val age = DateUtils.safeParseDate(it.birthDate)?.let(DateUtils::monthAge)
                        ?: AppStrings.aiMonthAgeUnknown
                    "${it.name} · $age"
                } ?: AppStrings.aiNoBaby,
                style = typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = if (state.preferences.useRecentRecords) {
                    AppStrings.aiDataNotice
                } else {
                    AppStrings.aiDataNoticeDisabled
                },
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun AiModelSelector(
    state: AiChatUiState,
    onSelect: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypographyStyle.current
    Column(Modifier.padding(horizontal = spacing.md)) {
        when {
            state.prerequisite != AiChatPrerequisite.READY -> Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = prerequisiteMessage(state.prerequisite),
                    style = typography.bodyMedium,
                    color = if (state.prerequisite == AiChatPrerequisite.CONFIG_UNAVAILABLE) {
                        colors.error
                    } else {
                        colors.textSecondary
                    },
                    modifier = Modifier.weight(1f),
                )
                if (state.prerequisite == AiChatPrerequisite.CONFIG_UNAVAILABLE) {
                    AppTextButton(onClick = onRefresh, label = AppStrings.aiRetry)
                }
            }
            else -> Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                state.modelOptions.forEach { option ->
                    val selected = option.id == state.selectedOptionId
                    AppChip(
                        label = option.name,
                        backgroundColor = if (selected) colors.primary else colors.surfaceElevated,
                        textColor = if (selected) colors.onPrimary else colors.textSecondary,
                        modifier = Modifier.clickable { onSelect(option.id) },
                    )
                }
            }
        }
    }
}

private fun prerequisiteMessage(prerequisite: AiChatPrerequisite): String = when (prerequisite) {
    AiChatPrerequisite.READY -> ""
    AiChatPrerequisite.DISABLED -> AppStrings.aiDisabled
    AiChatPrerequisite.NOT_LOGGED_IN -> AppStrings.aiNotLoggedIn
    AiChatPrerequisite.NO_FAMILY -> AppStrings.aiNoFamily
    AiChatPrerequisite.FAMILY_VERIFYING -> AppStrings.aiFamilyVerifying
    AiChatPrerequisite.FAMILY_UNVERIFIED -> AppStrings.aiFamilyUnverified
    AiChatPrerequisite.NO_BABY -> AppStrings.aiNoBaby
    AiChatPrerequisite.CONFIG_LOADING -> AppStrings.aiConfigLoading
    AiChatPrerequisite.CONFIG_UNAVAILABLE -> AppStrings.aiConfigUnavailable
}

@Composable
private fun AiWelcomeCard(onQuestion: (String) -> Unit) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypographyStyle.current
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(spacing.md)) {
            Text(AppStrings.aiWelcome, style = typography.bodyLarge, color = colors.textPrimary)
            Spacer(Modifier.height(spacing.md))
            listOf(
                AppStrings.aiQuestionAge,
                AppStrings.aiQuestionSleep,
                AppStrings.aiQuestionFeeding,
            ).forEach { question ->
                AppChip(
                    label = question,
                    modifier = Modifier
                        .padding(bottom = spacing.sm)
                        .clickable { onQuestion(question) },
                )
            }
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: AiChatEntry,
    renderMarkdown: Boolean,
    onCopy: (String) -> Unit,
) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val typography = LocalAppTypographyStyle.current
    val isUser = message.role == AiChatRole.USER
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        ) {
            Column(
                Modifier
                    .widthIn(max = 340.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(if (isUser) colors.primary else colors.surfaceElevated)
                    .padding(spacing.md),
            ) {
                if (!isUser && renderMarkdown) {
                    AppMarkdownText(
                        markdown = message.content,
                        style = typography.bodyLarge,
                        color = colors.textPrimary,
                    )
                } else {
                    Text(
                        text = message.content,
                        style = typography.bodyLarge,
                        color = if (isUser) colors.onPrimary else colors.textPrimary,
                    )
                }
                if (!isUser) {
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = if (message.references.isEmpty()) {
                            AppStrings.aiNoRecentRecordReference
                        } else {
                            AppStrings.aiReferencePrefix + message.references.joinToString("、")
                        },
                        style = typography.label,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(spacing.xs))
                    Text(
                        text = AppStrings.aiDisclaimer,
                        style = typography.label,
                        color = colors.textTertiary,
                    )
                    AppTextButton(
                        onClick = { onCopy(message.content) },
                        label = AppStrings.aiCopy,
                    )
                }
            }
        }
        message.riskLevel?.let { riskLevel ->
            Spacer(Modifier.height(spacing.sm))
            AiRiskCard(riskLevel)
        }
    }
}

@Composable
private fun AiRiskCard(riskLevel: AiRiskLevel) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    val accent = if (riskLevel == AiRiskLevel.ATTENTION) colors.warning else colors.error
    val title = when (riskLevel) {
        AiRiskLevel.EMERGENCY -> AppStrings.aiRiskEmergencyTitle
        AiRiskLevel.HIGH -> AppStrings.aiRiskHighTitle
        AiRiskLevel.ATTENTION -> AppStrings.aiRiskAttentionTitle
    }
    val message = when (riskLevel) {
        AiRiskLevel.EMERGENCY -> AppStrings.aiRiskEmergencyMessage
        AiRiskLevel.HIGH -> AppStrings.aiRiskHighMessage
        AiRiskLevel.ATTENTION -> AppStrings.aiRiskAttentionMessage
    }
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.surfaceElevated,
        borderColor = accent,
        borderWidth = 1.dp,
    ) {
        Column(Modifier.padding(spacing.md)) {
            Text(title, style = typography.titleMedium, color = accent)
            Spacer(Modifier.height(spacing.xs))
            Text(message, style = typography.bodyMedium, color = colors.textPrimary)
        }
    }
}

@Composable
private fun AiTypingIndicator() {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    Text(
        text = AppStrings.aiAnswering,
        style = typography.bodyMedium,
        color = colors.textSecondary,
        modifier = Modifier.padding(vertical = spacing.sm),
    )
}

@Composable
private fun AiErrorBanner(error: AiChatError, canRetry: Boolean, onRetry: () -> Unit) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    val message = when (error) {
        AiChatError.INPUT_TOO_LONG -> AppStrings.aiInputTooLong
        AiChatError.CONFIG_UNAVAILABLE -> AppStrings.aiConfigUnavailable
        AiChatError.AUTHENTICATION -> AppStrings.aiAuthError
        AiChatError.INSUFFICIENT_BALANCE -> AppStrings.aiBalanceError
        AiChatError.INVALID_REQUEST -> AppStrings.aiRequestError
        AiChatError.RATE_LIMIT -> AppStrings.aiRateLimitError
        AiChatError.SERVICE_UNAVAILABLE -> AppStrings.aiServiceError
        AiChatError.NETWORK -> AppStrings.aiNetworkError
        AiChatError.UNKNOWN -> AppStrings.aiUnknownError
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.error.copy(alpha = 0.1f))
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, style = typography.bodyMedium, color = colors.error, modifier = Modifier.weight(1f))
        if (canRetry) {
            AppTextButton(onClick = onRetry, label = AppStrings.aiRetry)
        }
    }
}

@Composable
private fun AiComposer(
    state: AiChatUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.surface)
            .padding(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppInput(
            value = state.input,
            onValueChange = onInputChange,
            label = AppStrings.aiInputLabel,
            placeholder = AppStrings.aiInputPlaceholder,
            enabled = state.prerequisite == AiChatPrerequisite.READY && !state.isSending,
            isError = state.error == AiChatError.INPUT_TOO_LONG,
            errorMessage = if (state.error == AiChatError.INPUT_TOO_LONG) AppStrings.aiInputTooLong else null,
            singleLine = false,
            minLines = 1,
            maxLines = 4,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(spacing.sm))
        PrimaryButton(
            onClick = if (state.isSending) onStop else onSend,
            label = if (state.isSending) AppStrings.aiStop else AppStrings.aiSend,
            enabled = state.isSending || state.canSend,
        )
    }
}

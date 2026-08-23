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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.markdown.AppMarkdownText
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.navigation.AiSettings
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var showHistory by remember { mutableStateOf(false) }
    var headerExpanded by rememberSaveable { mutableStateOf(true) }
    var deletingConversationId by remember { mutableStateOf<Long?>(null) }
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
    val lastMessageLength = state.messages.lastOrNull()?.let {
        it.content.length + it.reasoningContent.length
    } ?: 0
    LaunchedEffect(
        state.messages.size,
        lastMessageLength,
        state.isSending,
        state.preferences.autoScroll,
    ) {
        if (state.preferences.autoScroll && state.messages.isNotEmpty()) {
            val typingCount = if (state.isSending && !state.hasStreamingAnswer) 1 else 0
            val bottomAnchorIndex = state.messages.size + typingCount
            listState.scrollToItem(bottomAnchorIndex)
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = AppStrings.aiAssistant,
                onBack = { navController.popBackStack() },
                actions = {
                    AppIconButton(icon = Icons.Default.History, onClick = { showHistory = true }, contentDescription = AppStrings.aiHistory)
                    AppIconButton(icon = Icons.Default.Settings, onClick = { navController.navigate(AiSettings) }, contentDescription = AppStrings.aiSettings)
                },
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LocalAppColors.current.pageBackground),
        ) {
            // 2.1 H4：顶栏可折叠——展开显示完整摘要+模型选择，收起为一枚胶囊
            if (headerExpanded) {
                AiBabySummary(state = state, onCollapse = { headerExpanded = false })
                AiModelSelector(
                    state = state,
                    onSelect = viewModel::selectModel,
                    onRefresh = viewModel::refreshConfig,
                )
            } else {
                CollapsedAiHeader(state = state, onExpand = { headerExpanded = true })
            }

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
                if (state.messages.isEmpty()) {
                    item {
                        AiQuickAnalysisSection(
                            state = state,
                            onSelect = viewModel::prepareAnalysis,
                            onSelectPeriod = viewModel::selectAnalysisPeriod,
                        )
                    }
                    if (state.preferences.showRecommendedQuestions) {
                        item { AiWelcomeCard(onQuestion = viewModel::updateInput) }
                    }
                }
                items(state.messages, key = { it.id }) { message ->
                    AiMessageBubble(
                        message = message,
                        renderMarkdown = state.preferences.renderMarkdown,
                        isStreaming = state.isStreaming(message),
                        canRevise = state.canReviseLastAnswer &&
                            state.messages.lastOrNull()?.id == message.id,
                        onCopy = copyAnswer,
                        onRegenerate = viewModel::regenerateLastAnswer,
                        onEditQuestion = viewModel::editLastQuestion,
                    )
                }
                if (state.isSending && !state.hasStreamingAnswer) {
                    item { AiTypingIndicator() }
                }
                item(key = "ai-chat-bottom-anchor") {
                    Spacer(Modifier.height(1.dp))
                }
            }

            state.analysisContext?.let { source ->
                AiAnalysisContextBar(
                    source = source,
                    period = state.analysisPeriod,
                    canRemove = !state.isSending,
                    onRemove = viewModel::removeAnalysisContext,
                )
            }
            val unavailableReason = state.analysisUnavailableReason
            val unavailableSource = state.analysisUnavailableSource
            if (unavailableReason != null && unavailableSource != null) {
                AiAnalysisUnavailableBanner(
                    source = unavailableSource,
                    reason = unavailableReason,
                )
            }
            state.error?.takeUnless { it == AiChatError.CONFIG_UNAVAILABLE }?.let { error ->
                AiErrorBanner(
                    error = error,
                    canRetry = state.messages.lastOrNull()?.role == AiChatRole.USER && !state.isSending,
                    onRetry = viewModel::retry,
                )
            }
            AiHistorySaveStatusBanner(state.historySaveStatus)
            AiComposer(
                state = state,
                onInputChange = viewModel::updateInput,
                onSend = viewModel::send,
                onStop = viewModel::stop,
            )
        }
    }

    AiHistorySheet(
        show = showHistory,
        state = state,
        onDismiss = { showHistory = false },
        onQueryChange = viewModel::updateHistoryQuery,
        onNewConversation = {
            viewModel.newConversation()
            showHistory = false
        },
        onLoadConversation = { conversationId ->
            viewModel.loadConversation(conversationId)
            showHistory = false
        },
        onDeleteConversation = { conversationId ->
            deletingConversationId = conversationId
        },
    )

    AppConfirmDialog(
        show = deletingConversationId != null,
        title = AppStrings.aiHistoryDeleteTitle,
        message = AppStrings.aiHistoryDeleteMessage,
        onConfirm = {
            deletingConversationId?.let(viewModel::deleteConversation)
            deletingConversationId = null
        },
        onDismiss = { deletingConversationId = null },
    )
}

@Composable
private fun AiHistorySheet(
    show: Boolean,
    state: AiChatUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onNewConversation: () -> Unit,
    onLoadConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    AppBottomSheet(show = show, onDismiss = onDismiss) {
        Text(
            text = AppStrings.aiHistory,
            style = typography.titleLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = spacing.md),
        )
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = AppStrings.aiHistoryLocalNotice,
            style = typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = spacing.md),
        )
        Spacer(Modifier.height(spacing.sm))
        AppInput(
            value = state.historyQuery,
            onValueChange = onQueryChange,
            label = AppStrings.aiHistorySearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md),
        )
        Spacer(Modifier.height(spacing.sm))
        AppButton(
            onClick = onNewConversation,
            label = AppStrings.aiHistoryNewChat,
            icon = Icons.Default.Add,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md),
        )
        Spacer(Modifier.height(spacing.sm))
        val conversations = state.filteredConversations
        if (state.isHistoryLoading) {
            Text(
                text = AppStrings.aiAnalysisLoading,
                style = typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg),
            )
        } else if (conversations.isEmpty()) {
            Text(
                text = if (state.historyQuery.isBlank()) {
                    AppStrings.aiHistoryEmpty
                } else {
                    AppStrings.aiHistoryNoMatch
                },
                style = typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = spacing.md,
                    vertical = spacing.xs,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(conversations, key = { it.id }) { conversation ->
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoadConversation(conversation.id) },
                        containerColor = if (state.conversationId == conversation.id) {
                            colors.primaryContainer
                        } else {
                            colors.surfaceElevated
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = spacing.md, top = spacing.sm, bottom = spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = conversation.title,
                                    style = typography.titleMedium,
                                    color = colors.textPrimary,
                                )
                                if (conversation.preview.isNotBlank()) {
                                    Spacer(Modifier.height(spacing.xs))
                                    Text(
                                        text = conversation.preview.replace(
                                            Regex("[\\r\\n]+"),
                                            " ",
                                        ).take(80),
                                        style = typography.bodyMedium,
                                        color = colors.textSecondary,
                                        maxLines = 2,
                                    )
                                }
                                Spacer(Modifier.height(spacing.xs))
                                Text(
                                    text = formatAiHistoryTime(conversation.updatedAt),
                                    style = typography.labelMedium,
                                    color = colors.textTertiary,
                                )
                            }
                            AppIconButton(icon = Icons.Default.Delete, onClick = { onDeleteConversation(conversation.id) }, contentDescription = AppStrings.aiHistoryDeleteTitle, tint = colors.danger)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
    }
}

private fun formatAiHistoryTime(timestamp: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

@Composable
private fun AiQuickAnalysisSection(
    state: AiChatUiState,
    onSelect: (AiAnalysisSource) -> Unit,
    onSelectPeriod: (AiAnalysisPeriod) -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    Column {
        Text(
            text = AppStrings.aiQuickAnalysis,
            style = typography.titleMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = AppStrings.aiQuickAnalysisSubtitle,
            style = typography.bodyMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = AppStrings.aiAnalysisPeriod,
            style = typography.labelMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(spacing.xs))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            AiAnalysisPeriod.entries.forEach { period ->
                val selected = state.analysisPeriod == period
                AppChip(
                    label = "${period.days} 天",
                    backgroundColor = if (selected) colors.primary else colors.surfaceElevated,
                    textColor = if (selected) colors.onPrimary else colors.textSecondary,
                    modifier = Modifier.clickable { onSelectPeriod(period) },
                )
            }
        }
        Spacer(Modifier.height(spacing.sm))
        AiAnalysisCardRow(
            sources = listOf(AiAnalysisSource.SLEEP, AiAnalysisSource.FEEDING),
            state = state,
            onSelect = onSelect,
        )
        Spacer(Modifier.height(spacing.sm))
        AiAnalysisCardRow(
            sources = listOf(AiAnalysisSource.HEALTH, AiAnalysisSource.OVERVIEW),
            state = state,
            onSelect = onSelect,
        )
    }
}

@Composable
private fun AiAnalysisCardRow(
    sources: List<AiAnalysisSource>,
    state: AiChatUiState,
    onSelect: (AiAnalysisSource) -> Unit,
) {
    val spacing = LocalAppSpacing.current
    Row(Modifier.fillMaxWidth()) {
        sources.forEachIndexed { index, source ->
            if (index > 0) Spacer(Modifier.width(spacing.sm))
            AiAnalysisCard(
                source = source,
                state = state,
                onClick = { onSelect(source) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AiAnalysisCard(
    source: AiAnalysisSource,
    state: AiChatUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    val enabled = isAnalysisSourceEnabled(source, state.preferences)
    val status = when {
        state.isAnalysisAvailabilityLoading -> AppStrings.aiAnalysisLoading
        !enabled -> AppStrings.aiAnalysisDataDisabled
        source in state.availableAnalyses -> AppStrings.aiAnalysisAvailable
        else -> AppStrings.aiAnalysisNoRecords
    }
    val selected = state.analysisContext == source
    AppCard(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clickable(onClick = onClick),
        containerColor = if (selected) colors.primaryContainer else colors.surfaceElevated,
        borderColor = if (selected) colors.primary else colors.outline,
        borderWidth = 1.dp,
    ) {
        Column(Modifier.padding(spacing.md)) {
            Text(
                text = analysisTitle(source),
                style = typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = analysisRange(state.analysisPeriod),
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = status,
                style = typography.labelMedium,
                color = if (source in state.availableAnalyses) {
                    colors.primary
                } else {
                    colors.textTertiary
                },
            )
        }
    }
}

@Composable
private fun AiAnalysisContextBar(
    source: AiAnalysisSource,
    period: AiAnalysisPeriod,
    canRemove: Boolean,
    onRemove: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md, vertical = spacing.xs)
            .fillMaxWidth(),
        containerColor = colors.primaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = spacing.md, end = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = spacing.sm),
            ) {
                Text(
                    text = AppStrings.aiAnalysisContext,
                    style = typography.labelMedium,
                    color = colors.textSecondary,
                )
                Text(
                    text = "${analysisTitle(source)} · ${analysisRange(period)}",
                    style = typography.bodyMedium,
                    color = colors.textPrimary,
                )
            }
            AppButton(
                variant = ButtonVariant.Text,
                onClick = onRemove,
                label = AppStrings.aiAnalysisRemove,
                enabled = canRemove,
            )
        }
    }
}

@Composable
private fun AiAnalysisUnavailableBanner(
    source: AiAnalysisSource,
    reason: AiAnalysisUnavailableReason,
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    val message = if (reason == AiAnalysisUnavailableReason.DATA_DISABLED) {
        AppStrings.aiAnalysisEnableRecords
    } else {
        when (source) {
            AiAnalysisSource.SLEEP -> AppStrings.aiAnalysisNoSleepRecords
            AiAnalysisSource.FEEDING -> AppStrings.aiAnalysisNoFeedingRecords
            AiAnalysisSource.HEALTH -> AppStrings.aiAnalysisNoHealthRecords
            AiAnalysisSource.OVERVIEW -> AppStrings.aiAnalysisNoOverviewRecords
        }
    }
    Text(
        text = message,
        style = typography.bodyMedium,
        color = colors.warning,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColorScale.fromSeed(colors.warning).tintContainer(colors))
            .padding(horizontal = spacing.md, vertical = spacing.sm),
    )
}

private fun analysisTitle(source: AiAnalysisSource): String = when (source) {
    AiAnalysisSource.SLEEP -> AppStrings.aiAnalysisSleep
    AiAnalysisSource.FEEDING -> AppStrings.aiAnalysisFeeding
    AiAnalysisSource.HEALTH -> AppStrings.aiAnalysisHealth
    AiAnalysisSource.OVERVIEW -> AppStrings.aiAnalysisOverview
}

private fun analysisRange(period: AiAnalysisPeriod): String =
    "最近 ${period.days} 天 · 对比前 ${period.days} 天"

@Composable
private fun AiBabySummary(state: AiChatUiState, onCollapse: () -> Unit = {}) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    val baby = state.baby
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md, vertical = spacing.sm)
            .fillMaxWidth(),
        containerColor = colors.primaryContainer,
    ) {
        Column(Modifier.padding(start = spacing.md, end = spacing.xs, top = spacing.sm, bottom = spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = baby?.let {
                        val age = DateUtils.safeParseDate(it.birthDate)?.let(DateUtils::monthAge)
                            ?: AppStrings.aiMonthAgeUnknown
                        "${it.name} · $age"
                    } ?: AppStrings.aiNoBaby,
                    style = typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                AppIconButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    onClick = onCollapse,
                    contentDescription = AppStrings.aiSettings,
                    tint = colors.textSecondary,
                )
            }
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

/** 折叠态顶栏：一枚胶囊展示宝宝名 + 当前模型，点击展开（H4） */
@Composable
private fun CollapsedAiHeader(state: AiChatUiState, onExpand: () -> Unit) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    val modelName = state.modelOptions.firstOrNull { it.id == state.selectedOptionId }?.name ?: ""
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md, vertical = spacing.sm)
            .fillMaxWidth()
            .clickable(onClick = onExpand),
        containerColor = colors.primaryContainer,
    ) {
        Row(
            Modifier.padding(horizontal = spacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("👶", style = typography.titleMedium)
            Spacer(Modifier.width(spacing.sm))
            Text(
                text = state.baby?.name ?: AppStrings.aiNoBaby,
                style = typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Text(modelName, style = typography.labelMedium, color = colors.textSecondary)
            Spacer(Modifier.width(spacing.xs))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(16.dp))
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
    val typography = LocalAppTypography.current
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
                    AppButton(variant = ButtonVariant.Text, onClick = onRefresh, label = AppStrings.aiRetry)
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
    val typography = LocalAppTypography.current
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
                    backgroundColor = AppColorScale.fromSeed(colors.primary).tintContainer(colors),
                    textColor = colors.textPrimary,
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
    isStreaming: Boolean,
    canRevise: Boolean,
    onCopy: (String) -> Unit,
    onRegenerate: () -> Unit,
    onEditQuestion: () -> Unit,
) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val typography = LocalAppTypography.current
    val isUser = message.role == AiChatRole.USER
    var answerBasisExpanded by remember(message.id) { mutableStateOf(false) }
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
                if (!isUser && message.reasoningContent.isNotBlank()) {
                    AiReasoningBlock(
                        reasoning = message.reasoningContent,
                        isStreaming = isStreaming,
                        renderMarkdown = renderMarkdown,
                    )
                    if (message.content.isNotBlank()) Spacer(Modifier.height(spacing.sm))
                }
                if (!isUser && message.safetyStatus != null) {
                    Text(
                        text = when (message.safetyStatus) {
                            AiAnswerSafetyStatus.SUPPLEMENTED -> AppStrings.aiSafetySupplemented
                            AiAnswerSafetyStatus.BLOCKED -> AppStrings.aiSafetyBlocked
                        },
                        style = typography.labelMedium,
                        color = if (message.safetyStatus == AiAnswerSafetyStatus.BLOCKED) {
                            colors.error
                        } else {
                            colors.warning
                        },
                    )
                    Spacer(Modifier.height(spacing.sm))
                }
                if (message.content.isNotBlank()) {
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
                }
                if (!isUser && !isStreaming && message.content.isNotBlank()) {
                    Spacer(Modifier.height(spacing.sm))
                    AppButton(
                        variant = ButtonVariant.Text,
                        onClick = { answerBasisExpanded = !answerBasisExpanded },
                        label = if (answerBasisExpanded) {
                            AppStrings.aiHideAnswerBasis
                        } else {
                            AppStrings.aiShowAnswerBasis
                        },
                    )
                    if (answerBasisExpanded) {
                        AiAnswerBasis(message.references)
                    }
                    Row {
                        AppButton(
                            variant = ButtonVariant.Text,
                            onClick = { onCopy(message.content) },
                            label = AppStrings.aiCopy,
                        )
                        if (canRevise) {
                            AppButton(
                                variant = ButtonVariant.Text,
                                onClick = onEditQuestion,
                                label = AppStrings.aiEditQuestion,
                            )
                            AppButton(
                                variant = ButtonVariant.Text,
                                onClick = onRegenerate,
                                label = AppStrings.aiRegenerate,
                            )
                        }
                    }
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
private fun AiAnswerBasis(references: List<String>) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Text(
            text = AppStrings.aiRecordFacts,
            style = typography.labelMedium,
            color = colors.textPrimary,
        )
        Text(
            text = if (references.isEmpty()) {
                AppStrings.aiNoRecentRecordReference
            } else {
                AppStrings.aiReferencePrefix + references.joinToString("、")
            },
            style = typography.labelMedium,
            color = colors.textSecondary,
        )
        Text(
            text = AppStrings.aiInference,
            style = typography.labelMedium,
            color = colors.textPrimary,
        )
        Text(
            text = AppStrings.aiInferenceNotice,
            style = typography.labelMedium,
            color = colors.textSecondary,
        )
        Text(
            text = AppStrings.aiActionAdvice,
            style = typography.labelMedium,
            color = colors.textPrimary,
        )
        Text(
            text = AppStrings.aiDisclaimer,
            style = typography.labelMedium,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun AiReasoningBlock(
    reasoning: String,
    isStreaming: Boolean,
    renderMarkdown: Boolean,
) {
    var expanded by remember { mutableStateOf(isStreaming) }
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    AppButton(
        variant = ButtonVariant.Text,
        onClick = { expanded = !expanded },
        label = when {
            isStreaming -> AppStrings.aiThinking
            expanded -> AppStrings.aiHideReasoning
            else -> AppStrings.aiShowReasoning
        },
    )
    if (expanded) {
        Spacer(Modifier.height(spacing.xs))
        if (renderMarkdown) {
            AppMarkdownText(
                markdown = reasoning,
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )
        } else {
            Text(
                text = reasoning,
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun AiRiskCard(riskLevel: AiRiskLevel) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
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
    val typography = LocalAppTypography.current
    Row(
        Modifier.padding(vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceMuted)
                .padding(horizontal = spacing.md, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppCircularProgress(indicatorColor = colors.primary)
                Spacer(Modifier.width(spacing.sm))
                Text(
                    text = AppStrings.aiAnswering,
                    style = typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun AiErrorBanner(error: AiChatError, canRetry: Boolean, onRetry: () -> Unit) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
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
            .padding(horizontal = spacing.md)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColorScale.fromSeed(colors.error).tintContainer(colors))
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, style = typography.bodyMedium, color = colors.error, modifier = Modifier.weight(1f))
        if (canRetry) {
            AppButton(variant = ButtonVariant.Text, onClick = onRetry, label = AppStrings.aiRetry)
        }
    }
}

@Composable
private fun AiHistorySaveStatusBanner(status: AiHistorySaveStatus) {
    if (status == AiHistorySaveStatus.IDLE) return
    val colors = LocalAppColors.current
    val text = when (status) {
        AiHistorySaveStatus.IDLE -> return
        AiHistorySaveStatus.SAVING -> AppStrings.aiHistorySaving
        AiHistorySaveStatus.SAVED -> AppStrings.aiHistorySaved
        AiHistorySaveStatus.FAILED -> AppStrings.aiHistorySaveFailed
    }
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = LocalAppSpacing.current.md,
                vertical = LocalAppSpacing.current.xs,
            ),
        style = LocalAppTypography.current.labelMedium,
        color = if (status == AiHistorySaveStatus.FAILED) {
            colors.error
        } else {
            colors.textSecondary
        },
    )
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
        AppButton(
            onClick = if (state.isSending) onStop else onSend,
            label = if (state.isSending) AppStrings.aiStop else AppStrings.aiSend,
            enabled = state.isSending || state.canSend,
        )
    }
}

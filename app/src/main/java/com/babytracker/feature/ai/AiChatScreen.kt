package com.babytracker.feature.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.card.CardColors
import com.babytracker.designsystem.components.card.CardVariant
import com.babytracker.ui.patterns.chat.AppChatInputBar
import com.babytracker.ui.patterns.chat.AppCollapsedHeader
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.components.chip.AppChipCarouselRow
import com.babytracker.designsystem.components.chip.AppChipSpec
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.inlinebanner.AppBannerSeverity
import com.babytracker.designsystem.components.inlinebanner.AppInlineBanner
import com.babytracker.ui.patterns.chat.AppMarkdownText
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.ui.patterns.chat.AppTypingIndicator
import com.babytracker.ui.patterns.chat.AppChatBubble
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.core.util.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AI 聊天屏 — 纯 UI 渲染层：只收 UiState 与命名回调，不感知 Koin / NavController / BabyController。
 * 回调全部由 [AiChatRoute] 装配（VM 方法引用 + 导航映射）。
 */
@Composable
fun AiChatScreen(
    state: AiChatUiState,
    onBack: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onSelectModel: (String) -> Unit,
    onRefreshConfig: () -> Unit,
    onPrepareAnalysis: (AiAnalysisSource) -> Unit,
    onSelectAnalysisPeriod: (AiAnalysisPeriod) -> Unit,
    onUpdateInput: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onRetry: () -> Unit,
    onRemoveAnalysisContext: () -> Unit,
    onRegenerateLastAnswer: () -> Unit,
    onEditLastQuestion: () -> Unit,
    onUpdateHistoryQuery: (String) -> Unit,
    onNewConversation: () -> Unit,
    onLoadConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
) {
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
                onBack = onBack,
                actions = {
                    AppIconButton(icon = Icons.Default.History, onClick = { showHistory = true }, contentDescription = AppStrings.aiHistory)
                    AppIconButton(icon = Icons.Default.Settings, onClick = onOpenAiSettings, contentDescription = AppStrings.aiSettings)
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
                    onSelect = onSelectModel,
                    onRefresh = onRefreshConfig,
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
                            onSelect = onPrepareAnalysis,
                            onSelectPeriod = onSelectAnalysisPeriod,
                        )
                    }
                    if (state.preferences.showRecommendedQuestions) {
                        item {
                            // 欢迎语卡：AppCard 组合（欢迎语 + 推荐问题胶囊），G3 收编为调用点内联
                            val itemSpacing = LocalAppSpacing.current
                            val itemColors = LocalAppColors.current
                            val itemTypography = LocalAppTypography.current
                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(itemSpacing.md)) {
                                    Text(AppStrings.aiWelcome, style = itemTypography.bodyLarge, color = itemColors.textPrimary)
                                    Spacer(Modifier.height(itemSpacing.md))
                                    listOf(
                                        AppStrings.aiQuestionAge,
                                        AppStrings.aiQuestionSleep,
                                        AppStrings.aiQuestionFeeding,
                                    ).forEach { question ->
                                        AppChip(
                                            label = question,
                                            onClick = { onUpdateInput(question) },
                                            backgroundColor = AppColorScale.fromSeed(itemColors.primary).tintContainer(itemColors),
                                            textColor = itemColors.textPrimary,
                                            modifier = Modifier.padding(bottom = itemSpacing.sm),
                                        )
                                    }
                                }
                            }
                        }
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
                        onRegenerate = onRegenerateLastAnswer,
                        onEditQuestion = onEditLastQuestion,
                    )
                }
                if (state.isSending && !state.hasStreamingAnswer) {
                    item { AppTypingIndicator() }
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
                    onRemove = onRemoveAnalysisContext,
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
                    onRetry = onRetry,
                )
            }
            AiHistorySaveStatusBanner(state.historySaveStatus)
            AiComposer(
                state = state,
                onInputChange = onUpdateInput,
                onSend = onSend,
                onStop = onStop,
            )
        }
    }

    AiHistorySheet(
        show = showHistory,
        state = state,
        onDismiss = { showHistory = false },
        onQueryChange = onUpdateHistoryQuery,
        onNewConversation = {
            onNewConversation()
            showHistory = false
        },
        onLoadConversation = { conversationId ->
            onLoadConversation(conversationId)
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
            deletingConversationId?.let(onDeleteConversation)
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
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onLoadConversation(conversation.id) },
                        colors = CardColors(
                            containerColor = if (state.conversationId == conversation.id) {
                                colors.primaryContainer
                            } else {
                                colors.surfaceElevated
                            },
                        ),
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
        // 周期枚举→(key,label) 映射留在 feature；横滚单选 chip 条复用 AppChipCarouselRow（G4 遗留项接线）
        AppChipCarouselRow(
            options = AiAnalysisPeriod.entries.map { period ->
                AppChipSpec(key = period.name, label = "${period.days} 天")
            },
            selectedKey = state.analysisPeriod.name,
            onSelect = { key ->
                AiAnalysisPeriod.entries.firstOrNull { it.name == key }?.let(onSelectPeriod)
            },
        )
        Spacer(Modifier.height(spacing.sm))
        AiAnalysisRow(
            sources = listOf(AiAnalysisSource.SLEEP, AiAnalysisSource.FEEDING),
            state = state,
            onSelect = onSelect,
        )
        Spacer(Modifier.height(spacing.sm))
        AiAnalysisRow(
            sources = listOf(AiAnalysisSource.HEALTH, AiAnalysisSource.OVERVIEW),
            state = state,
            onSelect = onSelect,
        )
    }
}

// 快捷分析一行两卡：行容器保留命名（两个调用点共享），卡体直接用 AppCard 组合表达
@Composable
private fun AiAnalysisRow(
    sources: List<AiAnalysisSource>,
    state: AiChatUiState,
    onSelect: (AiAnalysisSource) -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    Row(Modifier.fillMaxWidth()) {
        sources.forEachIndexed { index, source ->
            if (index > 0) Spacer(Modifier.width(spacing.sm))
            val enabled = isAnalysisSourceEnabled(source, state.preferences)
            val status = when {
                state.isAnalysisAvailabilityLoading -> AppStrings.aiAnalysisLoading
                !enabled -> AppStrings.aiAnalysisDataDisabled
                source in state.availableAnalyses -> AppStrings.aiAnalysisAvailable
                else -> AppStrings.aiAnalysisNoRecords
            }
            val selected = state.analysisContext == source
            AppCard(
                modifier = Modifier.weight(1f).heightIn(min = 96.dp),
                variant = CardVariant.Outlined,
                onClick = { onSelect(source) },
                selected = selected,
                colors = CardColors(
                    containerColor = if (selected) colors.primaryContainer else colors.surfaceElevated,
                ),
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
        colors = CardColors(containerColor = colors.primaryContainer),
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
                variant = ButtonVariant.Ghost,
                onClick = onRemove,
                label = AppStrings.aiAnalysisRemove,
                enabled = canRemove,
            )
        }
    }
}

/**
 * 分析不可用提示条 — 枚举→文案映射留在 feature，视觉统一交由 AppInlineBanner（Warning 档）。
 * 存量视觉：16dp 圆角 + warning 浅底（tintContainer 档）+ 横 md/纵 sm 内边距，与令牌众数一致。
 */
@Composable
private fun AiAnalysisUnavailableBanner(
    source: AiAnalysisSource,
    reason: AiAnalysisUnavailableReason,
) {
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
    AppInlineBanner(
        message = message,
        severity = AppBannerSeverity.Warning,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LocalAppSpacing.current.md),
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
        colors = CardColors(containerColor = colors.primaryContainer),
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

/** 折叠态顶栏：一枚胶囊展示宝宝名 + 当前模型，点击展开（H4）；胶囊基座收编为 AppCollapsedHeader */
@Composable
private fun CollapsedAiHeader(state: AiChatUiState, onExpand: () -> Unit) {
    val spacing = LocalAppSpacing.current
    val modelName = state.modelOptions.firstOrNull { it.id == state.selectedOptionId }?.name ?: ""
    AppCollapsedHeader(
        emoji = "👶",
        title = state.baby?.name ?: AppStrings.aiNoBaby,
        subtitle = modelName,
        onClick = onExpand,
        modifier = Modifier
            .padding(horizontal = spacing.md, vertical = spacing.sm)
            .fillMaxWidth(),
    )
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
                    AppButton(variant = ButtonVariant.Ghost, onClick = onRefresh, label = AppStrings.aiRetry)
                }
            }
            else -> AppChipCarouselRow(
                // 模型枚举→(key,label) 映射留在 feature；横滚单选 chip 条收编为 AppChipCarouselRow
                options = state.modelOptions.map { option ->
                    AppChipSpec(key = option.id, label = option.name)
                },
                selectedKey = state.selectedOptionId,
                onSelect = onSelect,
            )
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
    val typography = LocalAppTypography.current
    val isUser = message.role == AiChatRole.USER
    var answerBasisExpanded by remember(message.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        // 气泡壳收编为 AppChatBubble：角色对齐 + 双色底 + 内容槽；业务组合经槽注入
        AppChatBubble(fromUser = isUser) {
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
                    variant = ButtonVariant.Ghost,
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
                        variant = ButtonVariant.Ghost,
                        onClick = { onCopy(message.content) },
                        label = AppStrings.aiCopy,
                    )
                    if (canRevise) {
                        AppButton(
                            variant = ButtonVariant.Ghost,
                            onClick = onEditQuestion,
                            label = AppStrings.aiEditQuestion,
                        )
                        AppButton(
                            variant = ButtonVariant.Ghost,
                            onClick = onRegenerate,
                            label = AppStrings.aiRegenerate,
                        )
                    }
                }
            }
        }
        message.riskLevel?.let { riskLevel ->
            Spacer(Modifier.height(spacing.sm))
            // 风险警示卡：描边强调色随风险档位变化，G3 收编为调用点内联
            val accent = if (riskLevel == AiRiskLevel.ATTENTION) colors.warning else colors.error
            val riskTitle = when (riskLevel) {
                AiRiskLevel.EMERGENCY -> AppStrings.aiRiskEmergencyTitle
                AiRiskLevel.HIGH -> AppStrings.aiRiskHighTitle
                AiRiskLevel.ATTENTION -> AppStrings.aiRiskAttentionTitle
            }
            val riskMessage = when (riskLevel) {
                AiRiskLevel.EMERGENCY -> AppStrings.aiRiskEmergencyMessage
                AiRiskLevel.HIGH -> AppStrings.aiRiskHighMessage
                AiRiskLevel.ATTENTION -> AppStrings.aiRiskAttentionMessage
            }
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Outlined,
                colors = CardColors(borderColor = accent),
            ) {
                Column(Modifier.padding(spacing.md)) {
                    Text(riskTitle, style = typography.titleMedium, color = accent)
                    Spacer(Modifier.height(spacing.xs))
                    Text(riskMessage, style = typography.bodyMedium, color = colors.textPrimary)
                }
            }
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
        variant = ButtonVariant.Ghost,
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

/**
 * 错误横幅 — 枚举→文案映射留在 feature；可选重试动作由 AppInlineBanner 尾部 Ghost 动作承载
 * （与存量形态一致：AppButton Ghost「重试」）。
 */
@Composable
private fun AiErrorBanner(error: AiChatError, canRetry: Boolean, onRetry: () -> Unit) {
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
    AppInlineBanner(
        message = message,
        severity = AppBannerSeverity.Error,
        actionLabel = if (canRetry) AppStrings.aiRetry else null,
        onAction = if (canRetry) onRetry else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LocalAppSpacing.current.md),
    )
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

/**
 * 底部输入条 — 业务态→参数映射留在 feature（前置条件轴/错误枚举不进 DS）；
 * 「多行输入 + 发送/停止切换」收编为 AppChatInputBar。
 */
@Composable
private fun AiComposer(
    state: AiChatUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    AppChatInputBar(
        value = state.input,
        onValueChange = onInputChange,
        isSending = state.isSending,
        onSend = onSend,
        onStop = onStop,
        enabled = state.prerequisite == AiChatPrerequisite.READY && !state.isSending,
        canSend = state.canSend,
        isError = state.error == AiChatError.INPUT_TOO_LONG,
        errorMessage = if (state.error == AiChatError.INPUT_TOO_LONG) AppStrings.aiInputTooLong else null,
    )
}

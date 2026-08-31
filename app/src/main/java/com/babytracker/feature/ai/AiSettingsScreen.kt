package com.babytracker.feature.ai

import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.button.AppButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.babytracker.core.ai.AiModelOption
import com.babytracker.core.ai.settings.AiAnswerDetail
import com.babytracker.core.ai.settings.AiAnswerTone
import com.babytracker.core.ai.settings.AiReasoningEffort
import com.babytracker.core.ai.settings.AiThinkingMode
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.ui.patterns.settings.AppSettingChoiceItem
import com.babytracker.ui.patterns.settings.AppSettingGroupTitle
import com.babytracker.ui.patterns.settings.AppSettingItem
import com.babytracker.ui.patterns.settings.AppSettingSwitchItem
import com.babytracker.designsystem.components.slider.AppSlider
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.ui.i18n.AppStringsProduct
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.cardgroup.AppCardGroup
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AiSettingsScreen(
    state: AiSettingsUiState,
    onBack: () -> Unit,
    onRefreshConfig: () -> Unit,
    onSetAssistantEnabled: (Boolean) -> Unit,
    onSetDefaultModel: (String) -> Unit,
    onSetContextRounds: (Int) -> Unit,
    onSetMaxOutputTokens: (Int) -> Unit,
    onSetStreaming: (Boolean) -> Unit,
    onSetThinkingMode: (AiThinkingMode) -> Unit,
    onSetReasoningEffort: (AiReasoningEffort) -> Unit,
    onSetCustomTemperature: (Boolean) -> Unit,
    onSetTemperatureTenths: (Int) -> Unit,
    onSetAnswerDetail: (AiAnswerDetail) -> Unit,
    onSetAnswerTone: (AiAnswerTone) -> Unit,
    onSetActionChecklist: (Boolean) -> Unit,
    onSetUseRecentRecords: (Boolean) -> Unit,
    onSetUseFeeding: (Boolean) -> Unit,
    onSetUseSleep: (Boolean) -> Unit,
    onSetUseDiaper: (Boolean) -> Unit,
    onSetUseGrowth: (Boolean) -> Unit,
    onSetUseHealth: (Boolean) -> Unit,
    onSetRecommendedQuestions: (Boolean) -> Unit,
    onClearConversation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences = state.preferences
    val capabilities = state.selectedModel?.capabilities
    val spacing = LocalAppSpacing.current
    var showClearConfirm by remember { mutableStateOf(false) }

    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = AppStringsProduct.aiSettings,
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
        ) {
            Spacer(Modifier.height(spacing.md))

            // 2.1 配置状态行（5.19：状态可见性前置，点击刷新）——G3 收编为调用点内联组合
            val cfgColors = LocalAppColors.current
            val cfgTypography = LocalAppTypography.current
            val cfgShapes = LocalAppShapes.current
            val configReady = state.configVersion != null && !state.isRefreshing
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 状态点：绿=可用 / 红=异常 / 转圈=刷新中
                    if (state.isRefreshing) {
                        AppCircularProgress(indicatorColor = cfgColors.primary)
                    } else {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(cfgShapes.full))
                                .background(if (configReady) cfgColors.success else cfgColors.error),
                        )
                    }
                    Spacer(Modifier.width(spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(
                            AppStringsProduct.aiSettingsConfigStatus,
                            style = cfgTypography.titleSmall,
                            color = cfgColors.textPrimary,
                        )
                        Spacer(Modifier.height(spacing.xxs))
                        Text(
                            configStatus(state),
                            style = cfgTypography.bodySmall,
                            color = if (configReady) cfgColors.textSecondary else cfgColors.error,
                        )
                    }
                    AppButton(
                        variant = ButtonVariant.Ghost,
                        onClick = onRefreshConfig,
                        label = AppStringsProduct.aiRetry,
                        enabled = !state.isRefreshing,
                    )
                }
            }

            Spacer(Modifier.height(spacing.sm))

            // AiSettingsSectionTitle → AppSettingGroupTitle 收编：组间大间隔走 showTopSpacing
            AppSettingGroupTitle(AppStringsProduct.aiSettingsGeneral, showTopSpacing = true)
            AppCardGroup {
                AppSettingSwitchItem(
                    emoji = "✨",
                    label = AppStringsProduct.aiSettingsEnabled,
                    subtitle = AppStringsProduct.aiSettingsEnabledSubtitle,
                    checked = preferences.assistantEnabled,
                    onCheckedChange = onSetAssistantEnabled,
                )
            }

            AppSettingGroupTitle(AppStringsProduct.aiSettingsModelAndAnswer, showTopSpacing = true)
            AppCardGroup {
                AppSettingChoiceItem(
                    emoji = "🧠",
                    label = AppStringsProduct.aiSettingsDefaultModel,
                    options = state.modelOptions.map { it.id to it.name },
                    selectedId = state.selectedModelId,
                    onSelect = onSetDefaultModel,
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AiCapabilitySetting(state.selectedModel)
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingChoiceItem(
                    emoji = "🧩",
                    label = AppStringsProduct.aiSettingsContextRounds,
                    subtitle = AppStringsProduct.aiSettingsContextRoundsSubtitle,
                    options = listOf(0, 2, 5, 10, 20).map { rounds ->
                        rounds.toString() to if (rounds == 0) {
                            AppStringsProduct.aiSettingsNoContext
                        } else {
                            "$rounds ${AppStringsProduct.aiSettingsRounds}"
                        }
                    },
                    selectedId = preferences.contextRounds.toString(),
                    onSelect = { onSetContextRounds(it.toInt()) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingChoiceItem(
                    emoji = "🔢",
                    label = AppStringsProduct.aiSettingsMaxTokens,
                    subtitle = AppStringsProduct.aiSettingsMaxTokensSubtitle,
                    options = listOf(0, 1_024, 2_048, 4_096, 8_192, 16_384).map { tokens ->
                        tokens.toString() to if (tokens == 0) {
                            AppStringsProduct.aiSettingsAutomatic
                        } else {
                            tokens.toString()
                        }
                    },
                    selectedId = preferences.maxOutputTokens.toString(),
                    onSelect = { onSetMaxOutputTokens(it.toInt()) },
                )
                AppInput(
                    value = preferences.maxOutputTokens.takeIf { it > 0 }?.toString().orEmpty(),
                    onValueChange = { value ->
                        val digits = value.filter(Char::isDigit)
                        onSetMaxOutputTokens(digits.toIntOrNull() ?: 0)
                    },
                    label = AppStringsProduct.aiSettingsCustomTokens,
                    placeholder = AppStringsProduct.aiSettingsAutomatic,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingSwitchItem(
                    emoji = "⚡",
                    label = AppStringsProduct.aiSettingsStreaming,
                    subtitle = if (capabilities?.streaming == true) {
                        AppStringsProduct.aiSettingsStreamingSubtitle
                    } else {
                        AppStringsProduct.aiSettingsUnsupported
                    },
                    checked = preferences.streamingEnabled && capabilities?.streaming == true,
                    enabled = capabilities?.streaming == true,
                    onCheckedChange = onSetStreaming,
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                if (capabilities?.thinking == true) {
                    AppSettingChoiceItem(
                        emoji = "💭",
                        label = AppStringsProduct.aiSettingsThinking,
                        options = AiThinkingMode.entries.map { it.name to thinkingLabel(it) },
                        selectedId = preferences.thinkingMode.name,
                        onSelect = { onSetThinkingMode(AiThinkingMode.valueOf(it)) },
                    )
                    val supportedEfforts = capabilities.reasoningEfforts
                    if (supportedEfforts.isNotEmpty()) {
                        AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                        AppSettingChoiceItem(
                            emoji = "⚙️",
                            label = AppStringsProduct.aiSettingsReasoningEffort,
                            options = AiReasoningEffort.entries
                                .filter {
                                    it == AiReasoningEffort.AUTO ||
                                        it.name.lowercase() in supportedEfforts
                                }
                                .map { it.name to effortLabel(it) },
                            selectedId = preferences.reasoningEffort.name,
                            onSelect = {
                                onSetReasoningEffort(AiReasoningEffort.valueOf(it))
                            },
                        )
                    }
                } else {
                    AppSettingItem(
                        emoji = "💭",
                        label = AppStringsProduct.aiSettingsThinking,
                        subtitle = AppStringsProduct.aiSettingsUnsupported,
                        trailing = { },
                    )
                }
                if (capabilities?.temperature == true) {
                    AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                    AppSettingSwitchItem(
                        emoji = "🌡️",
                        label = AppStringsProduct.aiSettingsTemperature,
                        subtitle = AppStringsProduct.aiSettingsTemperatureSubtitle,
                        checked = preferences.customTemperature,
                        onCheckedChange = onSetCustomTemperature,
                    )
                    if (preferences.customTemperature) {
                        AppSlider(
                            value = preferences.temperatureTenths.toFloat(),
                            onValueChange = { onSetTemperatureTenths(it.toInt()) },
                            valueRange = 0f..20f,
                            steps = 19,
                            modifier = Modifier.padding(horizontal = spacing.md),
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", preferences.temperatureTenths / 10f),
                            style = LocalAppTypography.current.bodyMedium,
                            color = LocalAppColors.current.textSecondary,
                            modifier = Modifier.padding(
                                start = spacing.md,
                                end = spacing.md,
                                bottom = spacing.sm,
                            ),
                        )
                    }
                }
            }

            AppSettingGroupTitle(AppStringsProduct.aiSettingsAnswerPreference, showTopSpacing = true)
            AppCardGroup {
                AppSettingChoiceItem(
                    emoji = "📏",
                    label = AppStringsProduct.aiSettingsDetail,
                    options = AiAnswerDetail.entries.map { it.name to detailLabel(it) },
                    selectedId = preferences.answerDetail.name,
                    onSelect = { onSetAnswerDetail(AiAnswerDetail.valueOf(it)) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingChoiceItem(
                    emoji = "💬",
                    label = AppStringsProduct.aiSettingsTone,
                    options = AiAnswerTone.entries.map { it.name to toneLabel(it) },
                    selectedId = preferences.answerTone.name,
                    onSelect = { onSetAnswerTone(AiAnswerTone.valueOf(it)) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingSwitchItem(
                    emoji = "✅",
                    label = AppStringsProduct.aiSettingsChecklist,
                    checked = preferences.includeActionChecklist,
                    onCheckedChange = onSetActionChecklist,
                )
            }

            AppSettingGroupTitle(AppStringsProduct.aiSettingsBabyData, showTopSpacing = true)
            AppCardGroup {
                AppSettingSwitchItem(
                    emoji = "📊",
                    label = AppStringsProduct.aiSettingsUseRecords,
                    subtitle = AppStringsProduct.aiSettingsUseRecordsSubtitle,
                    checked = preferences.useRecentRecords,
                    onCheckedChange = onSetUseRecentRecords,
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingSwitchItem(
                    emoji = "🍼",
                    label = AppStringsProduct.aiSettingsFeeding,
                    checked = preferences.useFeedingRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseFeeding,
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingSwitchItem(
                    emoji = "🌙",
                    label = AppStringsProduct.aiSettingsSleep,
                    checked = preferences.useSleepRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseSleep,
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingSwitchItem(
                    emoji = "🧷",
                    label = AppStringsProduct.aiSettingsDiaper,
                    checked = preferences.useDiaperRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseDiaper,
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingSwitchItem(
                    emoji = "📏",
                    label = AppStringsProduct.aiSettingsGrowth,
                    checked = preferences.useGrowthRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseGrowth,
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingSwitchItem(
                    emoji = "❤️",
                    label = AppStringsProduct.aiSettingsHealth,
                    checked = preferences.useHealthRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseHealth,
                )
            }

            AppSettingGroupTitle(AppStringsProduct.aiSettingsExperience, showTopSpacing = true)
            AppCardGroup {
                AppSettingSwitchItem(
                    emoji = "💡",
                    label = AppStringsProduct.aiSettingsRecommended,
                    checked = preferences.showRecommendedQuestions,
                    onCheckedChange = onSetRecommendedQuestions,
                )
            }

            AppSettingGroupTitle(AppStringsProduct.aiSettingsPrivacyAndStatus, showTopSpacing = true)
            AppCardGroup {
                AppSettingItem(
                    emoji = "🛡️",
                    label = AppStringsProduct.aiSettingsSafety,
                    subtitle = AppStringsProduct.aiSettingsSafetySubtitle,
                    trailing = { Text(AppStringsProduct.aiSettingsAlwaysOn) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = spacing.md))
                AppSettingItem(
                    emoji = "🔐",
                    label = AppStringsProduct.aiSettingsDataNotice,
                    subtitle = AppStringsProduct.aiSettingsDataNoticeSubtitle,
                    trailing = { },
                )

                AppSettingItem(
                    emoji = "🗑️",
                    label = AppStringsProduct.aiSettingsClearChat,
                    subtitle = AppStringsProduct.aiSettingsClearChatSubtitle,
                    onClick = { showClearConfirm = true },
                )
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }

    AppConfirmDialog(
        show = showClearConfirm,
        title = AppStringsProduct.aiSettingsClearChat,
        message = AppStringsProduct.aiSettingsClearConfirm,
        confirmText = AppStrings.clear,
        onConfirm = {
            onClearConversation()
            showClearConfirm = false
        },
        onDismiss = { showClearConfirm = false },
    )
}

@Composable
private fun AiCapabilitySetting(model: AiModelOption?) {
    val capabilities = model?.capabilities
    val labels = buildList {
        if (capabilities?.streaming == true) add(AppStringsProduct.aiCapabilityStreaming)
        if (capabilities?.thinking == true) add(AppStringsProduct.aiCapabilityThinking)
        if (capabilities?.temperature == true) add(AppStringsProduct.aiCapabilityTemperature)
        if (isEmpty()) add(AppStringsProduct.aiCapabilityBasic)
    }
    AppSettingChoiceItem(
        emoji = "✨",
        label = AppStringsProduct.aiSettingsModelCapabilities,
        options = labels.map { it to it },
        selectedId = null,
        onSelect = { },
        interactive = false,
    )
}

private fun detailLabel(value: AiAnswerDetail): String = when (value) {
    AiAnswerDetail.CONCISE -> AppStringsProduct.aiDetailConcise
    AiAnswerDetail.BALANCED -> AppStringsProduct.aiDetailBalanced
    AiAnswerDetail.DETAILED -> AppStringsProduct.aiDetailDetailed
}

private fun toneLabel(value: AiAnswerTone): String = when (value) {
    AiAnswerTone.PRACTICAL -> AppStringsProduct.aiTonePractical
    AiAnswerTone.GENTLE -> AppStringsProduct.aiToneGentle
    AiAnswerTone.PROFESSIONAL -> AppStringsProduct.aiToneProfessional
}

private fun thinkingLabel(value: AiThinkingMode): String = when (value) {
    AiThinkingMode.AUTO -> AppStringsProduct.aiSettingsThinkingAuto
    AiThinkingMode.ENABLED -> AppStringsProduct.aiSettingsThinkingOn
    AiThinkingMode.DISABLED -> AppStringsProduct.aiSettingsThinkingOff
}

private fun effortLabel(value: AiReasoningEffort): String = when (value) {
    AiReasoningEffort.AUTO -> AppStringsProduct.aiSettingsAutomatic
    AiReasoningEffort.LOW -> AppStringsProduct.aiSettingsEffortLow
    AiReasoningEffort.MEDIUM -> AppStringsProduct.aiSettingsEffortMedium
    AiReasoningEffort.HIGH -> AppStringsProduct.aiSettingsEffortHigh
    AiReasoningEffort.MAX -> AppStringsProduct.aiSettingsEffortMax
}

private fun configStatus(state: AiSettingsUiState): String {
    if (state.isRefreshing) return AppStringsProduct.aiConfigLoading
    if (state.configVersion == null) return state.errorMessage ?: AppStringsProduct.aiConfigUnavailable
    val expires = state.expiresAt?.let {
        DateTimeFormatter.ofPattern("MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(it))
    } ?: AppStrings.unknown
    return "v${state.configVersion} · ${AppStringsProduct.aiSettingsExpiresAt}$expires"
}
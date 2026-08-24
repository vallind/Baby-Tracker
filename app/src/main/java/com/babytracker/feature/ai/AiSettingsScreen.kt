package com.babytracker.feature.ai

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.slider.AppSlider
import com.babytracker.designsystem.components.switchcontrol.AppSwitch
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.feature.settings.SettingsCard
import com.babytracker.feature.settings.SettingsDivider
import com.babytracker.feature.settings.SettingsRow
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
                title = AppStrings.aiSettings,
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

            // 2.1 配置状态卡（5.19：状态可见性前置，点击刷新）
            AiConfigStatusCard(state = state, onRefresh = onRefreshConfig)

            Spacer(Modifier.height(spacing.sm))

            AiSettingsSectionTitle(AppStrings.aiSettingsGeneral)
            SettingsCard {
                AiSwitchRow(
                    emoji = "✨",
                    label = AppStrings.aiSettingsEnabled,
                    subtitle = AppStrings.aiSettingsEnabledSubtitle,
                    checked = preferences.assistantEnabled,
                    onCheckedChange = onSetAssistantEnabled,
                )
            }

            AiSettingsSectionTitle(AppStrings.aiSettingsModelAndAnswer)
            SettingsCard {
                AiChoiceSetting(
                    emoji = "🧠",
                    label = AppStrings.aiSettingsDefaultModel,
                    options = state.modelOptions.map { it.id to it.name },
                    selectedId = state.selectedModelId,
                    onSelect = onSetDefaultModel,
                )
                SettingsDivider()
                AiCapabilitySetting(state.selectedModel)
                SettingsDivider()
                AiChoiceSetting(
                    emoji = "🧩",
                    label = AppStrings.aiSettingsContextRounds,
                    subtitle = AppStrings.aiSettingsContextRoundsSubtitle,
                    options = listOf(0, 2, 5, 10, 20).map { rounds ->
                        rounds.toString() to if (rounds == 0) {
                            AppStrings.aiSettingsNoContext
                        } else {
                            "$rounds ${AppStrings.aiSettingsRounds}"
                        }
                    },
                    selectedId = preferences.contextRounds.toString(),
                    onSelect = { onSetContextRounds(it.toInt()) },
                )
                SettingsDivider()
                AiChoiceSetting(
                    emoji = "🔢",
                    label = AppStrings.aiSettingsMaxTokens,
                    subtitle = AppStrings.aiSettingsMaxTokensSubtitle,
                    options = listOf(0, 1_024, 2_048, 4_096, 8_192, 16_384).map { tokens ->
                        tokens.toString() to if (tokens == 0) {
                            AppStrings.aiSettingsAutomatic
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
                    label = AppStrings.aiSettingsCustomTokens,
                    placeholder = AppStrings.aiSettingsAutomatic,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "⚡",
                    label = AppStrings.aiSettingsStreaming,
                    subtitle = if (capabilities?.streaming == true) {
                        AppStrings.aiSettingsStreamingSubtitle
                    } else {
                        AppStrings.aiSettingsUnsupported
                    },
                    checked = preferences.streamingEnabled && capabilities?.streaming == true,
                    enabled = capabilities?.streaming == true,
                    onCheckedChange = onSetStreaming,
                )
                SettingsDivider()
                if (capabilities?.thinking == true) {
                    AiChoiceSetting(
                        emoji = "💭",
                        label = AppStrings.aiSettingsThinking,
                        options = AiThinkingMode.entries.map { it.name to thinkingLabel(it) },
                        selectedId = preferences.thinkingMode.name,
                        onSelect = { onSetThinkingMode(AiThinkingMode.valueOf(it)) },
                    )
                    val supportedEfforts = capabilities.reasoningEfforts
                    if (supportedEfforts.isNotEmpty()) {
                        SettingsDivider()
                        AiChoiceSetting(
                            emoji = "⚙️",
                            label = AppStrings.aiSettingsReasoningEffort,
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
                    SettingsRow(
                        emoji = "💭",
                        label = AppStrings.aiSettingsThinking,
                        subtitle = AppStrings.aiSettingsUnsupported,
                        trailing = { },
                    )
                }
                if (capabilities?.temperature == true) {
                    SettingsDivider()
                    AiSwitchRow(
                        emoji = "🌡️",
                        label = AppStrings.aiSettingsTemperature,
                        subtitle = AppStrings.aiSettingsTemperatureSubtitle,
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

            AiSettingsSectionTitle(AppStrings.aiSettingsAnswerPreference)
            SettingsCard {
                AiChoiceSetting(
                    emoji = "📏",
                    label = AppStrings.aiSettingsDetail,
                    options = AiAnswerDetail.entries.map { it.name to detailLabel(it) },
                    selectedId = preferences.answerDetail.name,
                    onSelect = { onSetAnswerDetail(AiAnswerDetail.valueOf(it)) },
                )
                SettingsDivider()
                AiChoiceSetting(
                    emoji = "💬",
                    label = AppStrings.aiSettingsTone,
                    options = AiAnswerTone.entries.map { it.name to toneLabel(it) },
                    selectedId = preferences.answerTone.name,
                    onSelect = { onSetAnswerTone(AiAnswerTone.valueOf(it)) },
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "✅",
                    label = AppStrings.aiSettingsChecklist,
                    checked = preferences.includeActionChecklist,
                    onCheckedChange = onSetActionChecklist,
                )
            }

            AiSettingsSectionTitle(AppStrings.aiSettingsBabyData)
            SettingsCard {
                AiSwitchRow(
                    emoji = "📊",
                    label = AppStrings.aiSettingsUseRecords,
                    subtitle = AppStrings.aiSettingsUseRecordsSubtitle,
                    checked = preferences.useRecentRecords,
                    onCheckedChange = onSetUseRecentRecords,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "🍼",
                    label = AppStrings.aiSettingsFeeding,
                    checked = preferences.useFeedingRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseFeeding,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "🌙",
                    label = AppStrings.aiSettingsSleep,
                    checked = preferences.useSleepRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseSleep,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "🧷",
                    label = AppStrings.aiSettingsDiaper,
                    checked = preferences.useDiaperRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseDiaper,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "📏",
                    label = AppStrings.aiSettingsGrowth,
                    checked = preferences.useGrowthRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseGrowth,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "❤️",
                    label = AppStrings.aiSettingsHealth,
                    checked = preferences.useHealthRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = onSetUseHealth,
                )
            }

            AiSettingsSectionTitle(AppStrings.aiSettingsExperience)
            SettingsCard {
                AiSwitchRow(
                    emoji = "💡",
                    label = AppStrings.aiSettingsRecommended,
                    checked = preferences.showRecommendedQuestions,
                    onCheckedChange = onSetRecommendedQuestions,
                )
            }

            AiSettingsSectionTitle(AppStrings.aiSettingsPrivacyAndStatus)
            SettingsCard {
                SettingsRow(
                    emoji = "🛡️",
                    label = AppStrings.aiSettingsSafety,
                    subtitle = AppStrings.aiSettingsSafetySubtitle,
                    trailing = { Text(AppStrings.aiSettingsAlwaysOn) },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "🔐",
                    label = AppStrings.aiSettingsDataNotice,
                    subtitle = AppStrings.aiSettingsDataNoticeSubtitle,
                    trailing = { },
                )

                SettingsRow(
                    emoji = "🗑️",
                    label = AppStrings.aiSettingsClearChat,
                    subtitle = AppStrings.aiSettingsClearChatSubtitle,
                    onClick = { showClearConfirm = true },
                )
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }

    AppConfirmDialog(
        show = showClearConfirm,
        title = AppStrings.aiSettingsClearChat,
        message = AppStrings.aiSettingsClearConfirm,
        confirmText = AppStrings.clear,
        onConfirm = {
            onClearConversation()
            showClearConfirm = false
        },
        onDismiss = { showClearConfirm = false },
    )
}

@Composable
private fun AiSettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = LocalAppTypography.current.labelMedium,
        color = LocalAppColors.current.textSecondary,
        modifier = Modifier.padding(top = LocalAppSpacing.current.lg, bottom = LocalAppSpacing.current.sm),
    )
}

@Composable
private fun AiSwitchRow(
    emoji: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    SettingsRow(
        emoji = emoji,
        label = label,
        subtitle = subtitle,
        trailing = {
            AppSwitch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
        onClick = {
            if (enabled) onCheckedChange(!checked)
        },
    )
}

@Composable
private fun AiChoiceSetting(
    emoji: String,
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    subtitle: String? = null,
    interactive: Boolean = true,
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    Column(Modifier.fillMaxWidth().padding(horizontal = spacing.md, vertical = spacing.sm)) {
        Text(
            text = "$emoji  $label",
            style = LocalAppTypography.current.bodyLarge,
            color = colors.textPrimary,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = LocalAppTypography.current.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = spacing.xs),
            )
        }
        Spacer(Modifier.height(spacing.sm))
        if (options.isEmpty()) {
            Text(
                text = AppStrings.aiConfigUnavailable,
                style = LocalAppTypography.current.bodyMedium,
                color = colors.textSecondary,
            )
        } else {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                options.forEach { (id, optionLabel) ->
                    val selected = id == selectedId
                    AppChip(
                        label = optionLabel,
                        backgroundColor = if (selected) colors.primary else colors.surfaceElevated,
                        textColor = if (selected) colors.onPrimary else colors.textSecondary,
                        modifier = if (interactive) {
                            Modifier.clickable { onSelect(id) }
                        } else {
                            Modifier
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AiCapabilitySetting(model: AiModelOption?) {
    val capabilities = model?.capabilities
    val labels = buildList {
        if (capabilities?.streaming == true) add(AppStrings.aiCapabilityStreaming)
        if (capabilities?.thinking == true) add(AppStrings.aiCapabilityThinking)
        if (capabilities?.temperature == true) add(AppStrings.aiCapabilityTemperature)
        if (isEmpty()) add(AppStrings.aiCapabilityBasic)
    }
    AiChoiceSetting(
        emoji = "✨",
        label = AppStrings.aiSettingsModelCapabilities,
        options = labels.map { it to it },
        selectedId = null,
        onSelect = { },
        interactive = false,
    )
}

private fun detailLabel(value: AiAnswerDetail): String = when (value) {
    AiAnswerDetail.CONCISE -> AppStrings.aiDetailConcise
    AiAnswerDetail.BALANCED -> AppStrings.aiDetailBalanced
    AiAnswerDetail.DETAILED -> AppStrings.aiDetailDetailed
}

private fun toneLabel(value: AiAnswerTone): String = when (value) {
    AiAnswerTone.PRACTICAL -> AppStrings.aiTonePractical
    AiAnswerTone.GENTLE -> AppStrings.aiToneGentle
    AiAnswerTone.PROFESSIONAL -> AppStrings.aiToneProfessional
}

private fun thinkingLabel(value: AiThinkingMode): String = when (value) {
    AiThinkingMode.AUTO -> AppStrings.aiSettingsThinkingAuto
    AiThinkingMode.ENABLED -> AppStrings.aiSettingsThinkingOn
    AiThinkingMode.DISABLED -> AppStrings.aiSettingsThinkingOff
}

private fun effortLabel(value: AiReasoningEffort): String = when (value) {
    AiReasoningEffort.AUTO -> AppStrings.aiSettingsAutomatic
    AiReasoningEffort.LOW -> AppStrings.aiSettingsEffortLow
    AiReasoningEffort.MEDIUM -> AppStrings.aiSettingsEffortMedium
    AiReasoningEffort.HIGH -> AppStrings.aiSettingsEffortHigh
    AiReasoningEffort.MAX -> AppStrings.aiSettingsEffortMax
}

private fun configStatus(state: AiSettingsUiState): String {
    if (state.isRefreshing) return AppStrings.aiConfigLoading
    if (state.configVersion == null) return state.errorMessage ?: AppStrings.aiConfigUnavailable
    val expires = state.expiresAt?.let {
        DateTimeFormatter.ofPattern("MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(it))
    } ?: AppStrings.unknown
    return "v${state.configVersion} · ${AppStrings.aiSettingsExpiresAt}$expires"
}


/** 配置状态卡：状态点 + 配置摘要 + 刷新按钮（2.1，替代列表内行内状态） */
@Composable
private fun AiConfigStatusCard(state: AiSettingsUiState, onRefresh: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val ready = state.configVersion != null && !state.isRefreshing

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = c.surface,
    ) {
        Row(
            Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 状态点：绿=可用 / 红=异常 / 转圈=刷新中
            if (state.isRefreshing) {
                AppCircularProgress(indicatorColor = c.primary)
            } else {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(shapes.full))
                        .background(if (ready) c.success else c.error),
                )
            }
            Spacer(Modifier.width(spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    AppStrings.aiSettingsConfigStatus,
                    style = typography.titleSmall,
                    color = c.textPrimary,
                )
                Spacer(Modifier.height(spacing.xxs))
                Text(
                    configStatus(state),
                    style = typography.bodySmall,
                    color = if (ready) c.textSecondary else c.error,
                )
            }
            AppButton(
                variant = ButtonVariant.Text,
                onClick = onRefresh,
                label = AppStrings.aiRetry,
                enabled = !state.isRefreshing,
            )
        }
    }
}
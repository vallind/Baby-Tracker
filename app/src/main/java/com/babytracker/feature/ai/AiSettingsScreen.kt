package com.babytracker.feature.ai
import com.babytracker.core.ui.AppSpacing
import io.elyon.kmp.theme.ElyonTheme

import androidx.compose.foundation.clickable
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
import io.elyon.kmp.basic.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.babytracker.navigation.Navigator
import com.babytracker.core.ai.AiModelOption
import com.babytracker.core.ai.settings.AiAnswerDetail
import com.babytracker.core.ai.settings.AiAnswerTone
import com.babytracker.core.ai.settings.AiReasoningEffort
import com.babytracker.core.ai.settings.AiThinkingMode
import com.babytracker.core.ui.components.chip.AppChip
import com.babytracker.core.ui.components.dialog.AppConfirmDialog
import com.babytracker.core.ui.components.input.AppInput
import com.babytracker.core.ui.components.scaffold.AppScaffold
import com.babytracker.core.ui.components.slider.AppSlider
import com.babytracker.core.ui.components.switchcontrol.AppSwitch
import com.babytracker.core.ui.components.topbar.AppTopBar
import com.babytracker.i18n.AppStrings
import com.babytracker.feature.settings.SettingsCard
import com.babytracker.feature.settings.SettingsDivider
import com.babytracker.feature.settings.SettingsRow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun AiSettingsScreen(navigator: Navigator) {
    val viewModel: AiSettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val preferences = state.preferences
    val capabilities = state.selectedModel?.capabilities
    val spacing = com.babytracker.core.ui.AppSpacing
    var showClearConfirm by remember { mutableStateOf(false) }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = AppStrings.aiSettings,
                onBack = { navigator.pop() },
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

            AiSettingsSectionTitle(AppStrings.aiSettingsGeneral)
            SettingsCard {
                AiSwitchRow(
                    emoji = "✨",
                    label = AppStrings.aiSettingsEnabled,
                    subtitle = AppStrings.aiSettingsEnabledSubtitle,
                    checked = preferences.assistantEnabled,
                    onCheckedChange = viewModel::setAssistantEnabled,
                )
            }

            AiSettingsSectionTitle(AppStrings.aiSettingsModelAndAnswer)
            SettingsCard {
                AiChoiceSetting(
                    emoji = "🧠",
                    label = AppStrings.aiSettingsDefaultModel,
                    options = state.modelOptions.map { it.id to it.name },
                    selectedId = state.selectedModelId,
                    onSelect = viewModel::setDefaultModel,
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
                    onSelect = { viewModel.setContextRounds(it.toInt()) },
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
                    onSelect = { viewModel.setMaxOutputTokens(it.toInt()) },
                )
                AppInput(
                    value = preferences.maxOutputTokens.takeIf { it > 0 }?.toString().orEmpty(),
                    onValueChange = { value ->
                        val digits = value.filter(Char::isDigit)
                        viewModel.setMaxOutputTokens(digits.toIntOrNull() ?: 0)
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
                    onCheckedChange = viewModel::setStreaming,
                )
                SettingsDivider()
                if (capabilities?.thinking == true) {
                    AiChoiceSetting(
                        emoji = "💭",
                        label = AppStrings.aiSettingsThinking,
                        options = AiThinkingMode.entries.map { it.name to thinkingLabel(it) },
                        selectedId = preferences.thinkingMode.name,
                        onSelect = { viewModel.setThinkingMode(AiThinkingMode.valueOf(it)) },
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
                                viewModel.setReasoningEffort(AiReasoningEffort.valueOf(it))
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
                        onCheckedChange = viewModel::setCustomTemperature,
                    )
                    if (preferences.customTemperature) {
                        AppSlider(
                            value = preferences.temperatureTenths.toFloat(),
                            onValueChange = { viewModel.setTemperatureTenths(it.toInt()) },
                            valueRange = 0f..20f,
                            steps = 19,
                            modifier = Modifier.padding(horizontal = spacing.md),
                        )
                        Text(
                            text = String.format("%.1f", preferences.temperatureTenths / 10f),
                            style = ElyonTheme.textStyles.body2,
                            color =  ElyonTheme.colorScheme.onSurfaceVariantSummary,
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
                    onSelect = { viewModel.setAnswerDetail(AiAnswerDetail.valueOf(it)) },
                )
                SettingsDivider()
                AiChoiceSetting(
                    emoji = "💬",
                    label = AppStrings.aiSettingsTone,
                    options = AiAnswerTone.entries.map { it.name to toneLabel(it) },
                    selectedId = preferences.answerTone.name,
                    onSelect = { viewModel.setAnswerTone(AiAnswerTone.valueOf(it)) },
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "✅",
                    label = AppStrings.aiSettingsChecklist,
                    checked = preferences.includeActionChecklist,
                    onCheckedChange = viewModel::setActionChecklist,
                )
            }

            AiSettingsSectionTitle(AppStrings.aiSettingsBabyData)
            SettingsCard {
                AiSwitchRow(
                    emoji = "📊",
                    label = AppStrings.aiSettingsUseRecords,
                    subtitle = AppStrings.aiSettingsUseRecordsSubtitle,
                    checked = preferences.useRecentRecords,
                    onCheckedChange = viewModel::setUseRecentRecords,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "🍼",
                    label = AppStrings.aiSettingsFeeding,
                    checked = preferences.useFeedingRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = viewModel::setUseFeeding,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "🌙",
                    label = AppStrings.aiSettingsSleep,
                    checked = preferences.useSleepRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = viewModel::setUseSleep,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "🧷",
                    label = AppStrings.aiSettingsDiaper,
                    checked = preferences.useDiaperRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = viewModel::setUseDiaper,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "📏",
                    label = AppStrings.aiSettingsGrowth,
                    checked = preferences.useGrowthRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = viewModel::setUseGrowth,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "❤️",
                    label = AppStrings.aiSettingsHealth,
                    checked = preferences.useHealthRecords,
                    enabled = preferences.useRecentRecords,
                    onCheckedChange = viewModel::setUseHealth,
                )
            }

            AiSettingsSectionTitle(AppStrings.aiSettingsExperience)
            SettingsCard {
                AiSwitchRow(
                    emoji = "💡",
                    label = AppStrings.aiSettingsRecommended,
                    checked = preferences.showRecommendedQuestions,
                    onCheckedChange = viewModel::setRecommendedQuestions,
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
                SettingsDivider()
                SettingsRow(
                    emoji = "☁️",
                    label = AppStrings.aiSettingsConfigStatus,
                    subtitle = configStatus(state),
                    onClick = viewModel::refreshConfig,
                )
                SettingsDivider()
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
            viewModel.clearConversation()
            showClearConfirm = false
        },
        onDismiss = { showClearConfirm = false },
    )
}

@Composable
private fun AiSettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = ElyonTheme.textStyles.footnote1,
        color =  ElyonTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(top =  com.babytracker.core.ui.AppSpacing.lg, bottom =  com.babytracker.core.ui.AppSpacing.sm),
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
    val spacing = com.babytracker.core.ui.AppSpacing
    val colors =  ElyonTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = spacing.md, vertical = spacing.sm)) {
        Text(
            text = "$emoji  $label",
            style = ElyonTheme.textStyles.body1,
            color = colors.onSurface,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = ElyonTheme.textStyles.body2,
                color = colors.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = spacing.xs),
            )
        }
        Spacer(Modifier.height(spacing.sm))
        if (options.isEmpty()) {
            Text(
                text = AppStrings.aiConfigUnavailable,
                style = ElyonTheme.textStyles.body2,
                color = colors.onSurfaceVariantSummary,
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
                        backgroundColor = if (selected) colors.primary else colors.surfaceContainerHigh,
                        textColor = if (selected) colors.onPrimary else colors.onSurfaceVariantSummary,
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

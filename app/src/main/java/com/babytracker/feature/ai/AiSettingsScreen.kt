package com.babytracker.feature.ai

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.babytracker.core.ai.settings.AiAnswerDetail
import com.babytracker.core.ai.settings.AiAnswerTone
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.switchcontrol.AppSwitch
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypographyStyle
import com.babytracker.feature.settings.SettingsCard
import com.babytracker.feature.settings.SettingsDivider
import com.babytracker.feature.settings.SettingsRow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun AiSettingsScreen(navController: NavController) {
    val viewModel: AiSettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val preferences = state.preferences
    val spacing = LocalAppSpacing.current
    var showClearConfirm by remember { mutableStateOf(false) }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = AppStrings.aiSettings,
                onBack = { navController.popBackStack() },
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
                SettingsDivider()
                AiSwitchRow(
                    emoji = "↕️",
                    label = AppStrings.aiSettingsAutoScroll,
                    checked = preferences.autoScroll,
                    onCheckedChange = viewModel::setAutoScroll,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "📝",
                    label = AppStrings.aiSettingsMarkdown,
                    checked = preferences.renderMarkdown,
                    onCheckedChange = viewModel::setRenderMarkdown,
                )
                SettingsDivider()
                AiSwitchRow(
                    emoji = "📋",
                    label = AppStrings.aiSettingsCopyFeedback,
                    checked = preferences.showCopyFeedback,
                    onCheckedChange = viewModel::setCopyFeedback,
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
        style = LocalAppTypographyStyle.current.label,
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
) {
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    Column(Modifier.fillMaxWidth().padding(horizontal = spacing.md, vertical = spacing.sm)) {
        Text(
            text = "$emoji  $label",
            style = LocalAppTypographyStyle.current.bodyLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(spacing.sm))
        if (options.isEmpty()) {
            Text(
                text = AppStrings.aiConfigUnavailable,
                style = LocalAppTypographyStyle.current.bodyMedium,
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
                        modifier = Modifier.clickable { onSelect(id) },
                    )
                }
            }
        }
    }
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

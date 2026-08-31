package com.babytracker.feature.home

import androidx.compose.foundation.background
import com.babytracker.designsystem.components.animateNumber
import com.babytracker.designsystem.theme.LocalAppMotion
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.core.util.DateUtils
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.quickstat.QuickStatPill
import com.babytracker.ui.patterns.records.AppRecordRow
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.tilegrid.AppTileGrid
import com.babytracker.designsystem.components.tilegrid.AppTileSpec
import com.babytracker.ui.patterns.dashboard.AppHeroStatCard
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.feature.common.feedingTone
import java.time.LocalDateTime
import java.util.Locale
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    baby: Baby?,
    bottomBar: @Composable () -> Unit = {},
    onOpenBabyManagement: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenFeature: (HomeFeature) -> Unit = {},
    onOpenAiAssistant: () -> Unit = {},
    onSeeAll: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current

    AppScaffold(
        bottomBar = bottomBar,
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "🍼",
                title = AppStrings.noBabyTitle,
                subtitle = AppStrings.noBabySubtitle,
                actionText = AppStrings.addBaby,
                onAction = onOpenBabyManagement,
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(c.pageBackground),
        ) {
            // —— 顶部 hero 区：奶油渐变 + 大标题 + 渐变光环头像 ——
            HeroHeader(baby, onClickProfile = onOpenProfile)

            Spacer(Modifier.height(spacing.md))
            // 宫格页边距与首行上方留白留在调用点（6dp 为存量事实值）
            Column(Modifier.padding(horizontal = spacing.md)) {
                Spacer(Modifier.height(6.dp))
                AppTileGrid(
                    tiles = homeFeatureTiles(c),
                    onTileClick = { spec ->
                        // key 反查语义枚举（宫格组件领域无关，路由映射仍留在 Route）
                        HomeFeature.entries.firstOrNull { it.name == spec.key }?.let(onOpenFeature)
                    },
                )
            }

            Spacer(Modifier.height(spacing.md))
            AiAssistantEntry(onClick = onOpenAiAssistant)

            Spacer(Modifier.height(spacing.md))
            TodayOverview(feedCount = state.feedCount, breastFeedCount = state.breastFeedCount, formulaCount = state.formulaCount, formulaTotalMl = state.formulaTotalMl, sleepHours = state.sleepHours, diaperCount = state.diaperCount)

            if (state.recentItems.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                RecentRecordsSection(
                    items = state.recentItems,
                    onSeeAll = onSeeAll,
                )
            }

            // 底部导航为悬浮胶囊，留出呼吸空间
            Spacer(Modifier.height(88.dp))
        }
    }
}

/** 按当前时段返回问候语 */
private fun greetingText(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..10 -> AppStrings.homeGreetingMorning
        in 11..17 -> AppStrings.homeGreetingAfternoon
        else -> AppStrings.homeGreetingEvening
    }
}

@Composable
private fun HeroHeader(baby: Baby, onClickProfile: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(c))
            .padding(horizontal = spacing.md),
    ) {
        Row(
            Modifier.padding(vertical = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier.weight(1f).padding(end = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    greetingText(),
                    style = typography.labelMedium,
                    color = c.textTertiary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    baby.name,
                    style = typography.headlineLarge,
                    color = c.textPrimary,
                    maxLines = 1,
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 月龄胶囊（分档浅底，不透明）
                    val ageLabel = DateUtils.monthAge(java.time.LocalDate.parse(baby.birthDate))
                    Text(
                        ageLabel,
                        style = typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = c.primaryScale.accentContent(c),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(c.primaryScale.tintContainer(c))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                    Spacer(Modifier.width(spacing.sm))
                    // 宝宝资料入口（半透明白药丸）
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(c.surface.copy(alpha = 0.72f))
                            .clickable(onClick = onClickProfile)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            AppStrings.babyProfile,
                            style = typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = c.textTertiary,
                        )
                    }
                }
            }
            // 渐变光环头像（品牌蓝→紫）
            Box(
                Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(listOf(c.primary, c.secondary))),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(82.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(c.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👶", style = typography.displayLarge)
                }
            }
        }
    }
}

/** AI 助手入口行：渐变底 hero（G3 收编：领域内容组合非通用卡片基座，改名去卡片化命名） */
@Composable
private fun AiAssistantEntry(onClick: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val contentColor = c.onSecondary
    Box(
        Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clip(RoundedCornerShape(shapes.largeIncreased))
            .background(Gradients.ai(c))
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(contentColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("✨", style = typography.headlineSmall)
            }
            Spacer(Modifier.width(spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    AppStrings.aiAssistant,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    AppStrings.aiAssistantSubtitle,
                    style = typography.bodySmall,
                    color = contentColor.copy(alpha = 0.82f),
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.9f),
            )
        }
    }
}

/** 今日概览：渐变主卡上的白色统计格（收编为 AppHeroStatCard + QuickStatPill 组合） */
@Composable
fun TodayOverview(feedCount: Int, breastFeedCount: Int, formulaCount: Int, formulaTotalMl: Int, sleepHours: String, diaperCount: Int) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val animatedFeed = animateNumber(target = feedCount)
    val animatedBreast = animateNumber(target = breastFeedCount)
    val animatedDiaper = animateNumber(target = diaperCount)
    val showBreast = breastFeedCount > 0
    val showFormula = formulaCount > 0
    val showGeneric = !showBreast && !showFormula

    AppHeroStatCard(
        modifier = Modifier.padding(horizontal = spacing.md),
        gradient = Gradients.overviewCard(c),
        contentColor = c.onPrimary,
        title = AppStrings.todayOverview,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBreast) {
                QuickStatPill(value = animatedBreast.toString(), label = AppStrings.breastFeeding, unit = AppStrings.countsUnit, contentColor = c.onPrimary, modifier = Modifier.weight(1f))
            }
            if (showFormula) {
                QuickStatPill(value = if (formulaTotalMl > 0) formulaTotalMl.toString() else "0", label = AppStrings.formulaFeeding, unit = "ml", contentColor = c.onPrimary, modifier = Modifier.weight(1f))
            }
            if (showGeneric) {
                QuickStatPill(value = animatedFeed.toString(), label = AppStrings.feedingCount, unit = AppStrings.countsUnit, contentColor = c.onPrimary, modifier = Modifier.weight(1f))
            }
            QuickStatPill(value = sleepHours, label = AppStrings.sleepHours, contentColor = c.onPrimary, modifier = Modifier.weight(1f))
            QuickStatPill(value = animatedDiaper.toString(), label = AppStrings.diaperChange, unit = AppStrings.countsUnit, contentColor = c.onPrimary, modifier = Modifier.weight(1f))
        }
    }
}

/** 宫格功能分区（设计语言映射到五个语义色系）；语义枚举定义在 HomeRoute（路由映射留在 Route） */
private fun homeFeatureTiles(c: AppColors): List<AppTileSpec> = listOf(
    // 记录类三格点击进入对应记录页（宫格直达表单已于 2.2.1 回滚）
    AppTileSpec(HomeFeature.Feeding.name, "🍼", AppStrings.feedingRecords, c.danger),              // 喂养珊瑚红
    AppTileSpec(HomeFeature.Sleep.name, "🌙", AppStrings.sleepRecords, c.secondary),               // 睡眠紫
    AppTileSpec(HomeFeature.Diaper.name, "🧷", AppStrings.diaperRecords, c.tertiary),              // 尿布青
    AppTileSpec(HomeFeature.Growth.name, "📏", AppStrings.growthRecords, c.success),               // 生长绿
    AppTileSpec(HomeFeature.Development.name, "🧠", AppStrings.developmentAssessment, c.primary),  // 发育蓝
    AppTileSpec(HomeFeature.Vaccination.name, "💉", AppStrings.vaccinationRecords, c.warning),     // 疫苗琥珀
    AppTileSpec(HomeFeature.Health.name, "❤️", AppStrings.healthRecords, c.danger),                // 健康珊瑚（医疗红）
    // 提醒中心原只有设置页一个深入口，宫格补位后可达性提升
    AppTileSpec(HomeFeature.Reminder.name, "⏰", AppStrings.reminderCenter, c.warning),             // 提醒琥珀
)

@Composable
fun RecentRecordsSection(items: List<Any>, onSeeAll: () -> Unit = {}) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    AppCard(
        modifier = Modifier.padding(horizontal = spacing.md).fillMaxWidth(),
    ) {
        Column(Modifier.padding(spacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    AppStrings.recentRecords,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                )
                AppButton(variant = ButtonVariant.Ghost, onClick = onSeeAll, label = AppStrings.viewAll)
            }
            Spacer(Modifier.height(spacing.sm))
            val recentItems = items.take(5)
            val grouped = recentItems.groupBy { item ->
                when (item) {
                    is Feeding -> item.timestamp.take(10)
                    is Sleep -> item.startTime.take(10)
                    is Diaper -> item.timestamp.take(10)
                    else -> ""
                }
            }
            val showDates = grouped.size > 1
            var isFirst = true
            grouped.forEach { (date, groupItems) ->
                if (showDates) {
                    Text(
                        date,
                        style = typography.labelMedium,
                        color = c.textSecondary,
                        modifier = Modifier.padding(top = if (isFirst) spacing.none else 12.dp, bottom = spacing.xs),
                    )
                    isFirst = false
                }
                groupItems.forEach { item ->
                    val data = recordRowData(item, c) ?: return@forEach
                    AppRecordRow(
                        emoji = data.emoji,
                        tint = data.tint,
                        title = data.title,
                        subtitle = summaryText(item),
                        trailingText = recordTimeText(item),
                    )
                }
            }
        }
    }
}

/** 记录行语义映射（分区色纪律保留在 feature）：emoji 徽章 / 分区色 / 标题 */
private data class RecordRowData(val emoji: String, val tint: Color, val title: String)

private fun recordRowData(item: Any, c: AppColors): RecordRowData? = when (item) {
    is Feeding -> {
        val type = FeedingType.raw(item.type)
        val (emoji, tint) = feedingTone(item.type, c)
        RecordRowData(emoji, tint, DateUtils.feedingTypeLabel(type))
    }
    is Sleep -> {
        val night = item.type == SleepType.NIGHT
        RecordRowData(
            emoji = if (night) "🌙" else "☀️",
            tint = if (night) c.secondary else c.tertiary,
            title = if (night) AppStrings.nightSleep else AppStrings.nap,
        )
    }
    is Diaper -> RecordRowData("🧷", c.tertiary, AppStrings.diaperChange)
    else -> null
}

private fun recordTimeText(item: Any): String = when (item) {
    is Feeding -> item.timestamp.takeIf { it.length >= 16 }?.substring(11, 16) ?: ""
    is Sleep -> item.startTime.takeIf { it.length >= 16 }?.substring(11, 16) ?: ""
    is Diaper -> item.timestamp.takeIf { it.length >= 16 }?.substring(11, 16) ?: ""
    else -> ""
}

private fun summaryText(item: Any): String = when (item) {
    is Feeding -> when (item.type) {
        FeedingType.BREAST -> "${String.format(Locale.US, AppStrings.minutesCompactFormat, item.durationMin ?: 0)}"
        FeedingType.FORMULA -> "${item.amountMl ?: 0}ml"
        FeedingType.FOOD -> item.foodName ?: AppStrings.solidFood
        else -> "${item.amountMl ?: 0}ml"
    }
    is Sleep -> DateUtils.durationFullText(
        DateUtils.durationToTotalSeconds(
            LocalDateTime.parse(item.startTime, DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse(item.endTime, DateTimeFormatter.ISO_DATE_TIME),
        )
    )
    is Diaper -> DateUtils.diaperTypeLabel(com.babytracker.core.domain.model.DiaperType.raw(item.type))
    else -> ""
}
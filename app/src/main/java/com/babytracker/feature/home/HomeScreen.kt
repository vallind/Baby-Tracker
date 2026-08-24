package com.babytracker.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.navigation.AppBottomBar
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.isDarkTheme
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.feature.common.feedingTone
import com.babytracker.navigation.AiAssistant
import com.babytracker.navigation.BabyManagement
import com.babytracker.navigation.BabyProfile
import com.babytracker.navigation.DevelopmentAssessment
import com.babytracker.navigation.Diaper as DiaperRoute
import com.babytracker.navigation.Feeding as FeedingRoute
import com.babytracker.navigation.Growth
import com.babytracker.navigation.Health
import com.babytracker.navigation.Reminder
import com.babytracker.navigation.Sleep as SleepRoute
import com.babytracker.navigation.Timeline
import com.babytracker.navigation.Vaccination
import com.babytracker.navigation.navigateToRoot
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.util.Locale
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val viewModel: HomeViewModel = org.koin.androidx.compose.koinViewModel()
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val currentBabyId = babyCtrl.currentBabyId
    val baby = babies.find { it.id == currentBabyId } ?: babies.firstOrNull()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(baby) {
        if (baby != null) {
            if (babyCtrl.currentBabyId != baby.id) babyCtrl.selectBaby(baby.id)
            viewModel.loadData(baby.id)
        }
    }

    AppScaffold(
        bottomBar = { AppBottomBar(navController) },
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "🍼",
                title = AppStrings.noBabyTitle,
                subtitle = AppStrings.noBabySubtitle,
                actionText = AppStrings.addBaby,
                onAction = { navController.navigate(BabyManagement) },
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
            HeroHeader(baby, onClickProfile = { navController.navigate(BabyProfile) })

            Spacer(Modifier.height(spacing.md))
            FeatureGrid(navController)

            Spacer(Modifier.height(spacing.md))
            AiAssistantEntryCard(navController)

            Spacer(Modifier.height(spacing.md))
            TodayOverviewCard(feedCount = state.feedCount, breastFeedCount = state.breastFeedCount, formulaCount = state.formulaCount, formulaTotalMl = state.formulaTotalMl, sleepHours = state.sleepHours, diaperCount = state.diaperCount)

            if (state.recentItems.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                RecentRecordsSection(
                    items = state.recentItems,
                    onSeeAll = { navController.navigate(Timeline) },
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

@Composable
private fun AiAssistantEntryCard(navController: NavController) {
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
            .clickable { navController.navigate(AiAssistant) },
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

@Composable
fun TodayOverviewCard(feedCount: Int, breastFeedCount: Int, formulaCount: Int, formulaTotalMl: Int, sleepHours: String, diaperCount: Int) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val animatedFeed by androidx.compose.animation.core.animateIntAsState(targetValue = feedCount, animationSpec = androidx.compose.animation.core.tween(600), label = "feed")
    val animatedBreast by androidx.compose.animation.core.animateIntAsState(targetValue = breastFeedCount, animationSpec = androidx.compose.animation.core.tween(600), label = "breast")
    val animatedDiaper by androidx.compose.animation.core.animateIntAsState(targetValue = diaperCount, animationSpec = androidx.compose.animation.core.tween(600), label = "diaper")
    val showBreast = breastFeedCount > 0
    val showFormula = formulaCount > 0
    val showGeneric = !showBreast && !showFormula
    val contentColor = c.onPrimary

    // 渐变主卡上的白色统计格（不套用 StatCell 令牌色，白字版本）
    // 注意：局部 Composable 不继承外层 RowScope 接收者，weight 由调用方传入
    @Composable
    fun StatItem(value: String, label: String, unit: String? = null, modifier: Modifier = Modifier) {
        Column(
            modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    style = typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                if (!unit.isNullOrBlank()) {
                    Spacer(Modifier.width(2.dp))
                    Text(unit, style = typography.labelMedium, color = contentColor.copy(alpha = 0.78f))
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(label, style = typography.labelMedium, color = contentColor.copy(alpha = 0.70f))
        }
    }

    @Composable
    fun StatDivider() {
        Box(
            Modifier
                .width(1.dp)
                .height(36.dp)
                .background(contentColor.copy(alpha = 0.18f)),
        )
    }

    Box(
        Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clip(RoundedCornerShape(shapes.largeIncreased))
            .background(Gradients.overviewCard(c)),
    ) {
        Column(Modifier.padding(spacing.lg)) {
            Text(
                AppStrings.todayOverview,
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor.copy(alpha = 0.92f),
            )
            Spacer(Modifier.height(spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBreast) {
                    StatItem(value = animatedBreast.toString(), label = AppStrings.breastFeeding, unit = AppStrings.countsUnit, modifier = Modifier.weight(1f))
                    StatDivider()
                }
                if (showFormula) {
                    StatItem(value = if (formulaTotalMl > 0) formulaTotalMl.toString() else "0", label = AppStrings.formulaFeeding, unit = "ml", modifier = Modifier.weight(1f))
                    StatDivider()
                }
                if (showGeneric) {
                    StatItem(value = animatedFeed.toString(), label = AppStrings.feedingCount, unit = AppStrings.countsUnit, modifier = Modifier.weight(1f))
                    StatDivider()
                }
                StatItem(value = sleepHours, label = AppStrings.sleepHours, modifier = Modifier.weight(1f))
                StatDivider()
                StatItem(value = animatedDiaper.toString(), label = AppStrings.diaperChange, unit = AppStrings.countsUnit, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FeatureGrid(navController: NavController) {
    val spacing = LocalAppSpacing.current
    val items = listOf(
        // 记录类三格点击进入对应记录页（宫格直达表单已于 2.2.1 回滚）
        FeatureGridItemData({ navController.navigateToRoot(FeedingRoute) }, "🍼", AppStrings.feedingRecords, FeatureTone.Feeding),
        FeatureGridItemData({ navController.navigateToRoot(SleepRoute) }, "🌙", AppStrings.sleepRecords, FeatureTone.Sleep),
        FeatureGridItemData({ navController.navigateToRoot(DiaperRoute) }, "🧷", AppStrings.diaperRecords, FeatureTone.Diaper),
        FeatureGridItemData({ navController.navigateToRoot(Growth) }, "📏", AppStrings.growthRecords, FeatureTone.Growth),
        FeatureGridItemData({ navController.navigateToRoot(DevelopmentAssessment) }, "🧠", AppStrings.developmentAssessment, FeatureTone.Development),
        FeatureGridItemData({ navController.navigateToRoot(Vaccination) }, "💉", AppStrings.vaccinationRecords, FeatureTone.Vaccination),
        FeatureGridItemData({ navController.navigateToRoot(Health) }, "❤️", AppStrings.healthRecords, FeatureTone.Health),
        // 提醒中心原只有设置页一个深入口，宫格补位后可达性提升
        FeatureGridItemData({ navController.navigateToRoot(Reminder) }, "⏰", AppStrings.reminderCenter, FeatureTone.Reminder),
    )
    Column(Modifier.padding(horizontal = spacing.md)) {
        Spacer(Modifier.height(6.dp))
        // 第一行 4 个
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items.subList(0, 4).forEach { item ->
                FeatureGridItem(item, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(spacing.sm))
        // 第二行 4 个
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items.subList(4, 8).forEach { item ->
                FeatureGridItem(item, Modifier.weight(1f))
            }
        }
    }
}

/** 宫格功能分区（设计语言映射到五个语义色系） */
private enum class FeatureTone { Feeding, Sleep, Diaper, Growth, Development, Vaccination, Health, Reminder }

private fun FeatureTone.color(c: AppColors) = when (this) {
    FeatureTone.Feeding -> c.danger          // 喂养珊瑚红
    FeatureTone.Sleep -> c.secondary         // 睡眠紫
    FeatureTone.Diaper -> c.tertiary         // 尿布青
    FeatureTone.Growth -> c.success          // 生长绿
    FeatureTone.Development -> c.primary     // 发育蓝
    FeatureTone.Vaccination -> c.warning     // 疫苗琥珀
    FeatureTone.Health -> c.danger           // 健康珊瑚（医疗红）
    FeatureTone.Reminder -> c.warning        // 提醒琥珀
}

@Composable
private fun FeatureGridItem(
    item: FeatureGridItemData,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val scale = AppColorScale.fromSeed(item.tone.color(c))
    // 亮色：粉彩渐变（shade100→shade200）；暗色：深彩渐变（shade800→shade700）
    val tileBrush = Brush.verticalGradient(
        if (c.isDarkTheme) listOf(scale.shade800, scale.shade700)
        else listOf(scale.shade100, scale.shade200),
    )
    Column(
        modifier
            .clip(RoundedCornerShape(shapes.large))
            .clickable { item.navigate() }
            .padding(vertical = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(shapes.largeIncreased))
                .background(tileBrush),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, fontSize = 26.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(item.label, style = typography.titleSmall, color = c.textPrimary, fontWeight = FontWeight.Medium)
    }
}

private data class FeatureGridItemData(
    val navigate: () -> Unit,
    val emoji: String,
    val label: String,
    val tone: FeatureTone,
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
                AppButton(variant = ButtonVariant.Text, onClick = onSeeAll, label = AppStrings.viewAll)
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
                    TimelineRecordRow(item)
                }
            }
        }
    }
}

/** 最近记录行：粉彩 emoji 徽章 + 标题/摘要 + 时间（分区色） */
@Composable
private fun TimelineRecordRow(item: Any) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val (emoji, tint, title) = when (item) {
        is Feeding -> {
            val type = FeedingType.raw(item.type)
            val (emoji, tint) = feedingTone(item.type, c)
            Triple(emoji, tint, DateUtils.feedingTypeLabel(type))
        }
        is Sleep -> {
            val night = item.type == SleepType.NIGHT
            Triple(if (night) "🌙" else "☀️", if (night) c.secondary else c.tertiary, if (night) AppStrings.nightSleep else AppStrings.nap)
        }
        is Diaper -> {
            Triple("🧷", c.tertiary, AppStrings.diaperChange)
        }
        else -> return
    }
    val scale = AppColorScale.fromSeed(tint)
    val time = when (item) {
        is Feeding -> item.timestamp.takeIf { it.length >= 16 }?.substring(11, 16) ?: ""
        is Sleep -> item.startTime.takeIf { it.length >= 16 }?.substring(11, 16) ?: ""
        is Diaper -> item.timestamp.takeIf { it.length >= 16 }?.substring(11, 16) ?: ""
        else -> ""
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(shapes.medium))
                .background(scale.tintContainer(c)),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 20.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = typography.titleSmall, color = c.textPrimary)
            Spacer(Modifier.height(1.dp))
            Text(summaryText(item), style = typography.labelMedium, color = c.textSecondary, maxLines = 1)
        }
        Text(time, style = typography.labelMedium, color = c.textTertiary)
    }
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
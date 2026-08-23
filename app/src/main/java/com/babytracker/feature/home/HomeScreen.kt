package com.babytracker.feature.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.components.statcell.StatCell
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.navigation.AiAssistant
import com.babytracker.navigation.BabyManagement
import com.babytracker.navigation.BabyProfile
import com.babytracker.navigation.DevelopmentAssessment
import com.babytracker.navigation.Growth
import com.babytracker.navigation.Health
import com.babytracker.navigation.Reminder
import com.babytracker.navigation.Stats
import com.babytracker.navigation.Timeline
import com.babytracker.navigation.Vaccination
import com.babytracker.navigation.navigateToRoot
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.FeedingRepository
import com.babytracker.core.data.repository.SleepRepository
import com.babytracker.core.data.repository.DiaperRepository
import com.babytracker.feature.feeding.FeedingFormDialog
import com.babytracker.feature.sleep.SleepFormDialog
import com.babytracker.feature.diaper.DiaperFormDialog
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.SleepType
import com.babytracker.core.domain.model.FeedingType
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    // 快捷记录三仓库（宫格直达表单用）
    val feedingRepo: FeedingRepository = koinInject()
    val sleepRepo: SleepRepository = koinInject()
    val diaperRepo: DiaperRepository = koinInject()
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

    // —— 快捷记录：宫格点击直接弹表单，省去"进列表页再点按钮"两步 ——
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }
    var quickRecord by remember { mutableStateOf<String?>(null) }

    AppScaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "🍼",
                title = "还没有添加宝宝",
                subtitle = "点击下方按钮，记录宝宝成长的每一个瞬间",
                actionText = "添加宝宝",
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
            // —— 顶部宝宝信息区（浅蓝渐变背景 + 圆形头像）——
            BabyHeader(baby, onClickProfile = { navController.navigate(BabyProfile) })

            Spacer(Modifier.height(spacing.md))
            FeatureGrid(navController, onQuickRecord = { quickRecord = it })

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

            Spacer(Modifier.height(80.dp))
        }
    }

    // —— 快捷记录表单：保存后留在首页（今日概览自动刷新），撤销即删除该条 ——
    // grid 仅在 baby 非空时渲染，quickRecord 只能由其触发；此处仍显式判空兜底
    val quickBabyId = baby?.id
    if (quickBabyId != null && quickBabyId != 0) when (quickRecord) {
        "feeding" -> FeedingFormDialog(
            babyId = quickBabyId,
            onDismiss = { quickRecord = null },
            onSave = { f ->
                scope.launch {
                    feedingRepo.insert(f)
                    quickRecord = null
                    viewModel.loadData(quickBabyId)   // 刷新今日概览与最近记录
                    appSnackbar.showUndo(message = "已记录喂养") { feedingRepo.delete(f) }
                }
            },
        )
        "sleep" -> SleepFormDialog(
            babyId = quickBabyId,
            onDismiss = { quickRecord = null },
            onSave = { sl ->
                scope.launch {
                    sleepRepo.insert(sl)
                    quickRecord = null
                    viewModel.loadData(quickBabyId)   // 刷新今日概览与最近记录
                    appSnackbar.showUndo(message = "已记录睡眠") { sleepRepo.delete(sl) }
                }
            },
        )
        "diaper" -> DiaperFormDialog(
            babyId = quickBabyId,
            onDismiss = { quickRecord = null },
            onSave = { d ->
                scope.launch {
                    diaperRepo.insert(d)
                    quickRecord = null
                    viewModel.loadData(quickBabyId)   // 刷新今日概览与最近记录
                    appSnackbar.showUndo(message = "已记录换尿布") { diaperRepo.delete(d) }
                }
            },
        )
    }
}

@Composable
private fun AiAssistantEntryCard(navController: NavController) {
    val colors = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clickable { navController.navigate(AiAssistant) },
        containerColor = colors.primaryContainer,
    ) {
        Row(
            Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(AppColorScale.fromSeed(colors.primary).tintContainer(colors)),
                contentAlignment = Alignment.Center,
            ) {
                Text("✨", style = typography.headlineMedium)
            }
            Spacer(Modifier.width(spacing.md))
            Column(Modifier.weight(1f)) {
                Text(AppStrings.aiAssistant, style = typography.titleMedium, color = colors.textPrimary)
                Spacer(Modifier.height(spacing.xs))
                Text(AppStrings.aiAssistantSubtitle, style = typography.bodyMedium, color = colors.textSecondary)
            }
            Text("→", style = typography.titleMedium, color = colors.primary)
        }
    }
}

@Composable
private fun BabyHeader(baby: Baby, onClickProfile: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(c))
            .padding(horizontal = spacing.md),
    ) {
        Row(
            Modifier.padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier.weight(1f).padding(end = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        baby.name,
                        style = typography.headlineMedium,
                        color = c.textPrimary,
                    )
                    Spacer(Modifier.width(spacing.sm))
                    Text(
                        DateUtils.monthAge(java.time.LocalDate.parse(baby.birthDate)),
                        style = typography.bodyMedium,
                        color = c.textSecondary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onClickProfile),
                ) {
                    Text(
                        "宝宝资料",
                        style = typography.bodyLarge,
                        color = c.primary,
                    )
                    Text(" →", style = typography.bodyLarge, color = c.primary)
                }
            }
            // 卡通宝宝插图（emoji 组合）
            Box(
                Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(shapes.full))
                    .background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("👶", style = typography.displayLarge)
            }
        }
    }
}

@Composable
fun TodayOverviewCard(feedCount: Int, breastFeedCount: Int, formulaCount: Int, formulaTotalMl: Int, sleepHours: String, diaperCount: Int) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val animatedFeed by androidx.compose.animation.core.animateIntAsState(targetValue = feedCount, animationSpec = androidx.compose.animation.core.tween(600), label = "feed")
    val animatedBreast by androidx.compose.animation.core.animateIntAsState(targetValue = breastFeedCount, animationSpec = androidx.compose.animation.core.tween(600), label = "breast")
    val animatedDiaper by androidx.compose.animation.core.animateIntAsState(targetValue = diaperCount, animationSpec = androidx.compose.animation.core.tween(600), label = "diaper")
    val showBreast = breastFeedCount > 0
    val showFormula = formulaCount > 0
    val showGeneric = !showBreast && !showFormula
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth(),
    ) {
        Column(Modifier.padding(spacing.md)) {
            Text("今日概览", style = typography.titleMedium, color = c.textPrimary)
            Spacer(Modifier.height(spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBreast) {
                    StatCell(value = animatedBreast.toString(), label = "母乳", unit = "次", modifier = Modifier.weight(1f))
                    StatDivider()
                }
                if (showFormula) {
                    StatCell(value = if (formulaTotalMl > 0) formulaTotalMl.toString() else "0", label = "配方奶", unit = "ml", modifier = Modifier.weight(1f))
                    StatDivider()
                }
                if (showGeneric) {
                    StatCell(value = animatedFeed.toString(), label = "喂养次数", unit = "次", modifier = Modifier.weight(1f))
                    StatDivider()
                }
                StatCell(value = sleepHours, label = "睡眠时长", modifier = Modifier.weight(1f))
                StatDivider()
                StatCell(value = animatedDiaper.toString(), label = "换尿布", unit = "次", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RowScope.StatDivider() {
    val c = LocalAppColors.current
    Box(Modifier.width(1.dp).height(40.dp).align(Alignment.CenterVertically).background(c.divider))
}

@Composable
fun FeatureGrid(navController: NavController, onQuickRecord: (String) -> Unit = {}) {
    val spacing = LocalAppSpacing.current
    val items = listOf(
        // 记录类三格点击直达表单（高频动作前置）；历史浏览走底部"记录"Tab，统计走底部"统计"Tab
        FeatureGridItemData({ onQuickRecord("feeding") }, "🍼", "记喂养"),
        FeatureGridItemData({ onQuickRecord("sleep") }, "🌙", "记睡眠"),
        FeatureGridItemData({ onQuickRecord("diaper") }, "🧷", "记尿布"),
        FeatureGridItemData({ navController.navigateToRoot(Growth) }, "📏", "生长记录"),
        FeatureGridItemData({ navController.navigateToRoot(DevelopmentAssessment) }, "🧠", "发育评估"),
        FeatureGridItemData({ navController.navigateToRoot(Vaccination) }, "💉", "疫苗接种"),
        FeatureGridItemData({ navController.navigateToRoot(Health) }, "❤️", "健康档案"),
        // 提醒中心原只有设置页一个深入口，宫格补位后可达性提升
        FeatureGridItemData({ navController.navigateToRoot(Reminder) }, "⏰", "提醒中心"),
    )
    Column(Modifier.padding(horizontal = spacing.md)) {
        Spacer(Modifier.height(14.dp))
        // 第一行 4 个
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items.subList(0, 4).forEachIndexed { i, item ->
                FeatureGridItem(item, useAccent = i % 2 == 1, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(spacing.sm))
        // 第二行 4 个
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items.subList(4, 8).forEachIndexed { i, item ->
                FeatureGridItem(item, useAccent = i % 2 == 1, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FeatureGridItem(
    item: FeatureGridItemData,
    useAccent: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val tint = if (useAccent) c.warning else c.primary
    Column(
        modifier
            .clip(RoundedCornerShape(shapes.large))
            .clickable {
                item.navigate()
            }
            .padding(vertical = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(shapes.large))
                .background(AppColorScale.fromSeed(tint).tintContainer(c)),

            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, style = typography.headlineMedium)
        }
        Spacer(Modifier.height(6.dp))
        Text(item.label, style = typography.labelMedium, color = c.textPrimary)
    }
}

private data class FeatureGridItemData(
    val navigate: () -> Unit,
    val emoji: String,
    val label: String,
)

@Composable
fun RecentRecordsSection(items: List<Any>, onSeeAll: () -> Unit = {}) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    AppCard(
        modifier = Modifier.padding(horizontal = spacing.md).fillMaxWidth(),
    ) {
        Column(Modifier.padding(spacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("最近记录", style = typography.titleMedium, color = c.textPrimary)
                AppButton(variant = ButtonVariant.Text, onClick = onSeeAll, label = "查看全部")
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
                    if (item != groupItems.last()) {
                        AppDivider(color = c.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRecordRow(item: Any) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    Row(Modifier.fillMaxWidth().padding(vertical = spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        // 时间轴小圆点（primary 色）
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(shapes.full))
                .background(c.primary),
        )
        Spacer(Modifier.width(12.dp))
        when (item) {
            is Feeding -> {
                Text(when (item.type) { FeedingType.BREAST -> "🤱"; FeedingType.FORMULA -> "💧"; FeedingType.FOOD -> "🥣"; else -> "🥤" }, style = typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(DateUtils.feedingTypeLabel(com.babytracker.core.domain.model.FeedingType.raw(item.type)), style = typography.titleSmall, color = c.textPrimary)
                    Text(if (item.type == FeedingType.BREAST) "${item.durationMin ?: 0}分钟" else "${item.amountMl ?: 0}ml", style = typography.labelMedium, color = c.textSecondary)
                }
                Text(item.timestamp.takeIf { it.length >= 16 }?.substring(11, 16) ?: "", style = typography.labelMedium, color = c.textTertiary)
            }
            is Sleep -> {
                Text(if (item.type == SleepType.NIGHT) "🌙" else "☀️", style = typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (item.type == SleepType.NIGHT) "夜间睡眠" else "小睡", style = typography.titleSmall, color = c.textPrimary)
                    Text(
                        DateUtils.durationFullText(
                            DateUtils.durationToTotalSeconds(
                                LocalDateTime.parse(item.startTime, DateTimeFormatter.ISO_DATE_TIME),
                                LocalDateTime.parse(item.endTime, DateTimeFormatter.ISO_DATE_TIME),
                            )
                        ),
                        style = typography.labelMedium,
                        color = c.textSecondary,
                    )
                }
                Text(item.startTime.takeIf { it.length >= 16 }?.substring(11, 16) ?: "", style = typography.labelMedium, color = c.textTertiary)
            }
            is Diaper -> {
                Text("🧷", style = typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("换尿布", style = typography.titleSmall, color = c.textPrimary)
                    Text(DateUtils.diaperTypeLabel(com.babytracker.core.domain.model.DiaperType.raw(item.type)), style = typography.labelMedium, color = c.textSecondary)
                }
                Text(item.timestamp.takeIf { it.length >= 16 }?.substring(11, 16) ?: "", style = typography.labelMedium, color = c.textTertiary)
            }
        }
    }
}

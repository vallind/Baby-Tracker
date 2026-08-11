package com.babytracker.feature.home
import com.babytracker.core.ui.AppShapes
import com.babytracker.core.ui.AppSpacing

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.ExperimentalMaterial3Api
import io.elyon.kmp.basic.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Icon
import com.babytracker.core.ui.Gradients
import com.babytracker.core.ui.components.scaffold.AppScaffold
import com.babytracker.core.ui.components.button.AppButton
import com.babytracker.core.ui.components.button.ButtonVariant
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.ui.components.card.AppCard
import com.babytracker.core.ui.components.BottomNavBar
import com.babytracker.core.ui.components.divider.AppDivider
import com.babytracker.core.ui.components.EmptyState
import com.babytracker.i18n.AppStrings
import com.babytracker.navigation.Navigator
import com.babytracker.navigation.Route
import io.elyon.kmp.theme.ElyonTheme
import com.babytracker.core.data.repository.BabyRepository
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
fun HomeScreen(navigator: Navigator) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
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
        bottomBar = { BottomNavBar(navigator) },
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "🍼",
                title = "还没有添加宝宝",
                subtitle = "点击下方按钮，记录宝宝成长的每一个瞬间",
                actionText = "添加宝宝",
                onAction = { navigator.navigate(Route.BabyManagement) },
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(c.background),
        ) {
            // —— 顶部宝宝信息区（浅蓝渐变背景 + 圆形头像）——
            BabyHeader(baby, onClickProfile = { navigator.navigate(Route.BabyProfile) })

            Spacer(Modifier.height(spacing.md))
            FeatureGrid(navigator)

            Spacer(Modifier.height(spacing.md))
            AiAssistantEntryCard(navigator)

            Spacer(Modifier.height(spacing.md))
            TodayOverviewCard(feedCount = state.feedCount, breastFeedCount = state.breastFeedCount, formulaCount = state.formulaCount, formulaTotalMl = state.formulaTotalMl, sleepHours = state.sleepHours, diaperCount = state.diaperCount)

            if (state.recentItems.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                RecentRecordsSection(
                    items = state.recentItems,
                    onSeeAll = { navigator.navigate(Route.Timeline) },
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun AiAssistantEntryCard(navigator: Navigator) {
    val colors =  ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    val shapes = com.babytracker.core.ui.AppShapes
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clickable { navigator.navigate(Route.AiAssistant) },
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
                    .background(colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = colors.primary,
                )
            }
            Spacer(Modifier.width(spacing.md))
            Column(Modifier.weight(1f)) {
                Text(AppStrings.aiAssistant, style = typography.title3, color = colors.onSurface)
                Spacer(Modifier.height(spacing.xs))
                Text(AppStrings.aiAssistantSubtitle, style = typography.body2, color = colors.onSurfaceVariantSummary)
            }
            Text("→", style = typography.title3, color = colors.primary)
        }
    }
}

@Composable
private fun BabyHeader(baby: Baby, onClickProfile: () -> Unit) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    val shapes = com.babytracker.core.ui.AppShapes
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(ElyonTheme.colorScheme))
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
                        style = typography.headline2,
                        color = c.onSurface,
                    )
                    Spacer(Modifier.width(spacing.sm))
                    Text(
                        DateUtils.monthAge(java.time.LocalDate.parse(baby.birthDate)),
                        style = typography.body2,
                        color = c.onSurfaceVariantSummary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onClickProfile),
                ) {
                    Text(
                        "宝宝资料",
                        style = typography.body1,
                        color = c.primary,
                    )
                    Text(" →", style = typography.body1, color = c.primary)
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
                Icon(
                    Icons.Filled.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = c.primary,
                )
            }
        }
    }
}

@Composable
fun TodayOverviewCard(feedCount: Int, breastFeedCount: Int, formulaCount: Int, formulaTotalMl: Int, sleepHours: String, diaperCount: Int) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
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
            Text("今日概览", style = typography.title3, color = c.onSurface)
            Spacer(Modifier.height(spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBreast) {
                    TextStatCell(animatedBreast.toString(), "次", "母乳")
                    StatDivider()
                }
                if (showFormula) {
                    TextStatCell(if (formulaTotalMl > 0) "${formulaTotalMl}" else "0", "ml", "配方奶")
                    StatDivider()
                }
                if (showGeneric) {
                    TextStatCell(animatedFeed.toString(), "次", "喂养次数")
                    StatDivider()
                }
                TextStatCell(sleepHours, "", "睡眠时长")
                StatDivider()
                TextStatCell(animatedDiaper.toString(), "次", "换尿布")
            }
        }
    }
}

@Composable
fun RowScope.TextStatCell(value: String, unit: String, label: String) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    Column(
        Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = typography.headline2, color = c.primary)
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(spacing.xxs))
                Text(unit, style = typography.footnote1, color = c.onSurfaceVariantSummary, modifier = Modifier.padding(bottom = spacing.xxs))
            }
        }
        Spacer(Modifier.height(spacing.xs))
        Text(label, style = typography.footnote1, color = c.onSurfaceVariantSummary)
    }
}

@Composable
fun RowScope.StatDivider() {
    val c = ElyonTheme.colorScheme
    Box(Modifier.width(1.dp).height(40.dp).align(Alignment.CenterVertically).background(c.dividerLine))
}

@Composable
fun FeatureGrid(navigator: Navigator) {
    val spacing = com.babytracker.core.ui.AppSpacing
    val items = listOf(
        FeatureGridItemData(Route.Feeding, Icons.Filled.Restaurant, "喂养记录"),
        FeatureGridItemData(Route.Sleep, Icons.Filled.Bedtime, "睡眠记录"),
        FeatureGridItemData(Route.Diaper, Icons.Filled.BabyChangingStation, "尿布更换"),
        FeatureGridItemData(Route.Growth, Icons.Filled.MonitorWeight, "生长记录"),
        FeatureGridItemData(Route.DevelopmentAssessment, Icons.Filled.Psychology, "发育评估"),
        FeatureGridItemData(Route.Vaccination, Icons.Filled.Vaccines, "疫苗接种"),
        FeatureGridItemData(Route.Health, Icons.Filled.Favorite, "健康档案"),
        FeatureGridItemData(Route.Stats, Icons.Filled.BarChart, "统计分析"),
    )
    Column(Modifier.padding(horizontal = spacing.md)) {
        Spacer(Modifier.height(14.dp))
        // 第一行 4 个
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items.subList(0, 4).forEachIndexed { i, item ->
                FeatureGridItem(item, useAccent = i % 2 == 1, navigator, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(spacing.sm))
        // 第二行 4 个
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items.subList(4, 8).forEachIndexed { i, item ->
                FeatureGridItem(item, useAccent = i % 2 == 1, navigator, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FeatureGridItem(
    item: FeatureGridItemData,
    useAccent: Boolean,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    val shapes = com.babytracker.core.ui.AppShapes
    val tint = if (useAccent) c.secondary else c.primary
    Column(
        modifier
            .clip(RoundedCornerShape(shapes.large))
            .clickable {
                // 顶部功能入口按 Tab 语义切换：弹回根路由再压入，避免栈膨胀
                navigator.switchTab(item.route)
            }
            .padding(vertical = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(shapes.large))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = tint,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(item.label, style = typography.footnote1, color = c.onSurface)
    }
}

private data class FeatureGridItemData(
    val route: Route,
    val icon: ImageVector,
    val label: String,
)

@Composable
fun RecentRecordsSection(items: List<Any>, onSeeAll: () -> Unit = {}) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    val shapes = com.babytracker.core.ui.AppShapes
    AppCard(
        modifier = Modifier.padding(horizontal = spacing.md).fillMaxWidth(),
    ) {
        Column(Modifier.padding(spacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("最近记录", style = typography.title3, color = c.onSurface)
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
                        style = typography.footnote1,
                        color = c.onSurfaceVariantSummary,
                        modifier = Modifier.padding(top = if (isFirst) spacing.none else 12.dp, bottom = spacing.xs),
                    )
                    isFirst = false
                }
                groupItems.forEach { item ->
                    TimelineRecordRow(item)
                    if (item != groupItems.last()) {
                        AppDivider(color = c.dividerLine, thickness = 0.5.dp, modifier = Modifier.padding(start = 28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRecordRow(item: Any) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    val shapes = com.babytracker.core.ui.AppShapes
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
                Icon(
                    Icons.Filled.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = c.primary,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(DateUtils.feedingTypeLabel(com.babytracker.core.domain.model.FeedingType.raw(item.type)), style = typography.body1, color = c.onSurface)
                    Text(if (item.type == FeedingType.BREAST) "${item.durationMin ?: 0}分钟" else "${item.amountMl ?: 0}ml", style = typography.footnote1, color = c.onSurfaceVariantSummary)
                }
                Text(item.timestamp.takeIf { it.length >= 16 }?.substring(11, 16) ?: "", style = typography.footnote1, color = c.onSurfaceVariantSummary)
            }
            is Sleep -> {
                Icon(
                    Icons.Filled.Bedtime,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = c.primary,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (item.type == SleepType.NIGHT) "夜间睡眠" else "小睡", style = typography.body1, color = c.onSurface)
                    Text(
                        DateUtils.durationFullText(
                            DateUtils.durationToTotalSeconds(
                                LocalDateTime.parse(item.startTime, DateTimeFormatter.ISO_DATE_TIME),
                                LocalDateTime.parse(item.endTime, DateTimeFormatter.ISO_DATE_TIME),
                            )
                        ),
                        style = typography.footnote1,
                        color = c.onSurfaceVariantSummary,
                    )
                }
                Text(item.startTime.takeIf { it.length >= 16 }?.substring(11, 16) ?: "", style = typography.footnote1, color = c.onSurfaceVariantSummary)
            }
            is Diaper -> {
                Icon(
                    Icons.Filled.BabyChangingStation,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = c.primary,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("换尿布", style = typography.body1, color = c.onSurface)
                    Text(DateUtils.diaperTypeLabel(com.babytracker.core.domain.model.DiaperType.raw(item.type)), style = typography.footnote1, color = c.onSurfaceVariantSummary)
                }
                Text(item.timestamp.takeIf { it.length >= 16 }?.substring(11, 16) ?: "", style = typography.footnote1, color = c.onSurfaceVariantSummary)
            }
        }
    }
}

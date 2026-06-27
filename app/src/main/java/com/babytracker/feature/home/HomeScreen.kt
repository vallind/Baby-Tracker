package com.babytracker.feature.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.designsystem.components.AppCard
import com.babytracker.designsystem.components.BottomNavBar
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.SectionHeader
import com.babytracker.navigation.Screen
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.database.entity.FeedingEntity
import com.babytracker.core.database.entity.SleepEntity
import com.babytracker.core.database.entity.DiaperEntity
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val c = LocalThemeColors.current
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

    Scaffold(
        containerColor = c.bg,
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "🍼",
                title = "还没有添加宝宝",
                subtitle = "点击下方按钮，记录宝宝成长的每一个瞬间",
                actionText = "添加宝宝",
                onAction = { navController.navigate(Screen.BabyManagement.route) },
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(c.bg),
        ) {
            // —— 顶部宝宝信息区（浅蓝渐变背景 + 圆形头像）——
            BabyHeader(baby, onClickProfile = { navController.navigate(Screen.BabyManagement.route) })

            Spacer(Modifier.height(DT.cardGap.dp))
            TodayOverviewCard(feedCount = state.feedCount, sleepHours = state.sleepHours, diaperCount = state.diaperCount)

            if (state.recentItems.isNotEmpty()) {
                Spacer(Modifier.height(DT.cardGap.dp))
                RecentRecordsSection(
                    items = state.recentItems,
                    onSeeAll = { navController.navigate(Screen.Feeding.route) },
                )
            }

            Spacer(Modifier.height(DT.cardGap.dp))
            FeatureGrid(navController)

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun BabyHeader(baby: com.babytracker.core.database.entity.BabyEntity, onClickProfile: () -> Unit) {
    val c = LocalThemeColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(c))
            .padding(horizontal = DT.pageMargin.dp),
    ) {
        Row(Modifier.padding(vertical = 24.dp)) {
            Column(
                Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        baby.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        DateUtils.monthAge(java.time.LocalDate.parse(baby.birthDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onClickProfile),
                ) {
                    Text(
                        "宝宝资料",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(" →", style = MaterialTheme.typography.labelLarge, color = c.primary)
                }
            }
            Box(
                Modifier
                    .size(72.dp)
                    .shadow(elevation = DT.cardElevation.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Gradients.primary(c)),
                contentAlignment = Alignment.Center,
            ) {
                Text(baby.name.take(1), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun TodayOverviewCard(feedCount: Int, sleepHours: String, diaperCount: Int) {
    val c = LocalThemeColors.current
    val animatedFeed by androidx.compose.animation.core.animateIntAsState(targetValue = feedCount, animationSpec = androidx.compose.animation.core.tween(600), label = "feed")
    val animatedDiaper by androidx.compose.animation.core.animateIntAsState(targetValue = diaperCount, animationSpec = androidx.compose.animation.core.tween(600), label = "diaper")
    AppCard(
        modifier = Modifier
            .padding(horizontal = DT.pageMargin.dp)
            .fillMaxWidth()
            .height(128.dp),
        cornerRadius = DT.cardRadiusLg.dp,
    ) {
        Row {
            StatCell("🍼", animatedFeed.toString(), "喂养次数")
            StatDivider()
            StatCell("😴", sleepHours, "睡眠时长")
            StatDivider()
            StatCell("🧷", animatedDiaper.toString(), "换尿布")
        }
    }
}

@Composable
fun RowScope.StatCell(emoji: String, value: String, label: String) {
    val c = LocalThemeColors.current
    Column(
        Modifier.weight(1f).fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        // 数字用 primary 大字号（强调今日数据）
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.primary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = c.textSecondary)
    }
}

@Composable
fun RowScope.StatDivider() {
    val c = LocalThemeColors.current
    Box(Modifier.width(1.dp).height(40.dp).align(Alignment.CenterVertically).background(c.divider))
}

@Composable
fun FeatureGrid(navController: NavController) {
    val c = LocalThemeColors.current
    // 8 个功能项 = 4 列 × 2 行
    val items = listOf(
        Triple(Screen.Feeding, "🍼", "喂养"),
        Triple(Screen.Sleep, "😴", "睡眠"),
        Triple(Screen.Diaper, "🧷", "尿布"),
        Triple(Screen.Growth, "📏", "生长"),
        Triple(Screen.Vaccination, "💉", "疫苗"),
        Triple(Screen.Health, "❤️", "健康"),
        Triple(Screen.Stats, "📊", "统计"),
        Triple(Screen.Settings, "⚙️", "设置"),
    )
    Column(Modifier.padding(horizontal = DT.pageMargin.dp)) {
        Text("功能", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        Spacer(Modifier.height(14.dp))
        // 第一行 4 个
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.subList(0, 4).forEachIndexed { i, item ->
                FeatureGridItem(item, useAccent = i % 2 == 1, navController, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
        // 第二行 4 个
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.subList(4, 8).forEachIndexed { i, item ->
                FeatureGridItem(item, useAccent = i % 2 == 1, navController, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FeatureGridItem(
    item: Triple<com.babytracker.navigation.Screen, String, String>,
    useAccent: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val c = LocalThemeColors.current
    val (screen, emoji, label) = item
    val tint = if (useAccent) c.accent else c.primary
    Column(
        modifier
            .clip(RoundedCornerShape(DT.cardRadius.dp))
            .clickable { navController.navigate(screen.route) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(DT.iconBgSizeLg.dp)
                .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = DT.iconSizeLg.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
    }
}

@Composable
fun RecentRecordsSection(items: List<Any>, onSeeAll: () -> Unit = {}) {
    val c = LocalThemeColors.current
    AppCard(
        modifier = Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth(),
        cornerRadius = DT.cardRadius.dp,
    ) {
        Column(Modifier.padding(DT.cardInnerPadding.dp)) {
            SectionHeader(
                title = "最近记录",
                actionText = "查看全部",
                onAction = onSeeAll,
            )
            Spacer(Modifier.height(12.dp))
            val recentItems = items.take(5)
            val grouped = recentItems.groupBy { item ->
                when (item) {
                    is FeedingEntity -> item.timestamp.take(10)
                    is SleepEntity -> item.startTime.take(10)
                    is DiaperEntity -> item.timestamp.take(10)
                    else -> ""
                }
            }
            val showDates = grouped.size > 1
            var isFirst = true
            grouped.forEach { (date, groupItems) ->
                if (showDates) {
                    Text(
                        date,
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textSecondary,
                        modifier = Modifier.padding(top = if (isFirst) 0.dp else 12.dp, bottom = 4.dp),
                    )
                    isFirst = false
                }
                groupItems.forEach { item ->
                    TimelineRecordRow(item)
                    if (item != groupItems.last()) {
                        HorizontalDivider(color = c.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRecordRow(item: Any) {
    val c = LocalThemeColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        // 时间轴小圆点（primary 色）
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(c.primary),
        )
        Spacer(Modifier.width(12.dp))
        when (item) {
            is FeedingEntity -> {
                Text(when (item.type) { "breast" -> "🤱"; "formula" -> "💧"; "food" -> "🥣"; else -> "🥤" }, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(DateUtils.feedingTypeLabel(item.type), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                    Text(if (item.type == "breast") "${item.durationMin ?: 0}分钟" else "${item.amountMl ?: 0}ml", fontSize = 12.sp, color = c.textSecondary)
                }
                Text(item.timestamp.substring(11, 16), fontSize = 12.sp, color = c.textHint)
            }
            is SleepEntity -> {
                Text(if (item.type == "night") "🌙" else "☀️", fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (item.type == "night") "夜间睡眠" else "小睡", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                    Text(
                        DateUtils.durationFullText(
                            DateUtils.durationToTotalSeconds(
                                LocalDateTime.parse(item.startTime, DateTimeFormatter.ISO_DATE_TIME),
                                LocalDateTime.parse(item.endTime, DateTimeFormatter.ISO_DATE_TIME),
                            )
                        ),
                        fontSize = 12.sp,
                        color = c.textSecondary,
                    )
                }
                Text(item.startTime.substring(11, 16), fontSize = 12.sp, color = c.textHint)
            }
            is DiaperEntity -> {
                Text("🧷", fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("换尿布", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                    Text(DateUtils.diaperTypeLabel(item.type), fontSize = 12.sp, color = c.textSecondary)
                }
                Text(item.timestamp.substring(11, 16), fontSize = 12.sp, color = c.textHint)
            }
        }
    }
}

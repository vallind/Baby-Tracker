package com.babytracker.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import com.babytracker.core.theme.DT
import com.babytracker.core.theme.LocalThemeColors
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.ui.components.BabyIllustration
import com.babytracker.ui.components.BabyPose
import com.babytracker.ui.components.EmptyState
import com.babytracker.ui.navigation.Screen
import com.babytracker.data.repository.BabyRepository
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

        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Box(
                Modifier.fillMaxWidth().background(c.primaryLight).padding(horizontal = DT.pageMargin.dp),
            ) {
                Row(Modifier.padding(vertical = 24.dp)) {
                    Column(Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp), verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(baby.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text(DateUtils.monthAge(java.time.LocalDate.parse(baby.birthDate)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { navController.navigate(Screen.BabyManagement.route) }) {
                            Text("宝宝资料", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text(" →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Box(
                        Modifier.size(72.dp).clip(CircleShape).background(c.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(baby.name.take(1), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            TodayOverviewCard(feedCount = state.feedCount, sleepHours = state.sleepHours, diaperCount = state.diaperCount)

            if (state.recentItems.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                RecentRecordsSection(items = state.recentItems)
            }

            Spacer(Modifier.height(20.dp))
            FeatureGrid(navController)

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val c = LocalThemeColors.current
    NavigationBar(containerColor = c.card, tonalElevation = 0.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, "首页") },
            label = { Text("首页", fontSize = 11.sp) },
            selected = true,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(selectedIconColor = c.primary, selectedTextColor = c.primary, indicatorColor = c.primaryLight),
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.BarChart, "统计") },
            label = { Text("统计", fontSize = 11.sp) },
            selected = false,
            onClick = { navController.navigate(Screen.Stats.route) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = c.primary, selectedTextColor = c.primary, indicatorColor = c.primaryLight),
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Settings, "设置") },
            label = { Text("设置", fontSize = 11.sp) },
            selected = false,
            onClick = { navController.navigate(Screen.Settings.route) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = c.primary, selectedTextColor = c.primary, indicatorColor = c.primaryLight),
        )
    }
}

@Composable
fun TodayOverviewCard(feedCount: Int, sleepHours: String, diaperCount: Int) {
    val animatedFeed by androidx.compose.animation.core.animateIntAsState(targetValue = feedCount, animationSpec = androidx.compose.animation.core.tween(600), label = "feed")
    val animatedDiaper by androidx.compose.animation.core.animateIntAsState(targetValue = diaperCount, animationSpec = androidx.compose.animation.core.tween(600), label = "diaper")
    Card(
        Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth().height(120.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        Text(label, fontSize = 12.sp, color = c.textSecondary)
    }
}

@Composable
fun RowScope.StatDivider() {
    val c = LocalThemeColors.current
    Box(Modifier.width(1.dp).height(36.dp).background(c.divider))
}

@Composable
fun FeatureGrid(navController: NavController) {
    val c = LocalThemeColors.current
    val items = listOf(
        Screen.Feeding to "🍼" to "喂养",
        Screen.Sleep to "😴" to "睡眠",
        Screen.Diaper to "🧷" to "尿布",
        Screen.Growth to "📏" to "生长",
        Screen.Vaccination to "💉" to "疫苗",
        Screen.Health to "❤️" to "健康",
    )
    val colors = listOf(c.pink, c.blue, c.green, c.yellow, c.purple, c.cyan)
    Column(Modifier.padding(horizontal = DT.pageMargin.dp)) {
        Text("功能", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEachIndexed { globalIndex, (screenAndEmoji, label) ->
                    val (screen, emoji) = screenAndEmoji
                    // 每行 3 个；用 weight 布局
                    if (globalIndex % 3 == 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // 当前项
                            FeatureGridItem(screen, emoji, label, colors[globalIndex % colors.size], navController, Modifier.weight(1f))
                            // 占位后续 2 个
                            for (j in 1..2) {
                                val nextIdx = globalIndex + j
                                if (nextIdx < items.size) {
                                    val (nScreen, nEmoji) = items[nextIdx].first
                                    val nLabel = items[nextIdx].second
                                    FeatureGridItem(nScreen, nEmoji, nLabel, colors[nextIdx % colors.size], navController, Modifier.weight(1f))
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureGridItem(
    screen: com.babytracker.ui.navigation.Screen,
    emoji: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp)).clickable { navController.navigate(screen.route) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RecentRecordsSection(items: List<Any>) {
    val c = LocalThemeColors.current
    Card(
        Modifier.padding(horizontal = DT.pageMargin.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("最近记录", fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 14.dp))
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
                    Text(date, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, modifier = Modifier.padding(top = if (isFirst) 0.dp else 12.dp, bottom = 4.dp))
                    isFirst = false
                }
                groupItems.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    when (item) {
                        is FeedingEntity -> {
                            Text(when (item.type) { "breast" -> "🤱"; "formula" -> "💧"; "food" -> "🥣"; else -> "🥤" }, fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(DateUtils.feedingTypeLabel(item.type), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(if (item.type == "breast") "${item.durationMin ?: 0}分钟" else "${item.amountMl ?: 0}ml", fontSize = 12.sp, color = c.textSecondary)
                            }
                            Text(item.timestamp.substring(11, 16), fontSize = 12.sp, color = c.textHint)
                        }
                        is SleepEntity -> {
                            Text(if (item.type == "night") "🌙" else "☀️", fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (item.type == "night") "夜间睡眠" else "小睡", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(DateUtils.durationFullText(DateUtils.durationToTotalSeconds(LocalDateTime.parse(item.startTime, DateTimeFormatter.ISO_DATE_TIME), LocalDateTime.parse(item.endTime, DateTimeFormatter.ISO_DATE_TIME))), fontSize = 12.sp, color = c.textSecondary)
                            }
                            Text(item.startTime.substring(11, 16), fontSize = 12.sp, color = c.textHint)
                        }
                        is DiaperEntity -> {
                            Text("🧷", fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("换尿布", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(DateUtils.diaperTypeLabel(item.type), fontSize = 12.sp, color = c.textSecondary)
                            }
                            Text(item.timestamp.substring(11, 16), fontSize = 12.sp, color = c.textHint)
                        }
                    }
                }
                if (item != groupItems.last()) HorizontalDivider(color = c.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 40.dp))
                }
            }
        }
    }
}

package com.example.myapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.room.BabyEntity
import com.example.myapp.data.room.FeedingEntity
import com.example.myapp.data.room.GrowthEntity
import com.example.myapp.data.room.SleepEntity
import com.example.myapp.ui.designsystem.ParentingCard
import com.example.myapp.ui.designsystem.SectionTitle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("育儿助手") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { BabyCard(baby = state.baby) }

            item { TodayOverview(feedingCount = state.todayFeedingCount, sleepHours = state.todaySleepHours) }

            item {
                SectionTitle("功能")
                FunctionGrid(onNavigate = onNavigate)
            }

            item { SectionTitle("最近记录") }

            if (state.recentFeedings.isNotEmpty() || state.recentSleeps.isNotEmpty() || state.recentGrowths.isNotEmpty()) {
                item { RecentRecords(feedings = state.recentFeedings, sleeps = state.recentSleeps, growths = state.recentGrowths) }
            } else {
                item {
                    Text(
                        "暂无记录，开始记录宝宝的成长吧",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BabyCard(baby: BabyEntity?) {
    ParentingCard {
        Column {
            Text(
                baby?.name ?: "小宝宝",
                style = MaterialTheme.typography.titleLarge
            )
            if (baby != null) {
                val ageMonths = ageInMonths(baby.birthday)
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                Text(
                    "${ageMonths}个月  |  出生: ${fmt.format(Date(baby.birthday))}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (baby.birthHeight > 0f || baby.birthWeight > 0f) {
                    Text(
                        "出生: ${baby.birthHeight}cm  ${baby.birthWeight}kg",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayOverview(feedingCount: Int, sleepHours: Float) {
    ParentingCard {
        Column {
            Text("今日概览", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$feedingCount", style = MaterialTheme.typography.headlineLarge)
                    Text("喂养次数", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${"%.1f".format(sleepHours)}h", style = MaterialTheme.typography.headlineLarge)
                    Text("睡眠时长", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun FunctionGrid(onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val features = listOf(
            Triple("feeding", "喂养记录", Icons.Default.ChildCare),
            Triple("sleep", "睡眠记录", Icons.Default.Bedtime),
            Triple("growth", "生长记录", Icons.Default.TrendingUp),
            Triple("vaccine", "疫苗接种", Icons.Default.Shield),
            Triple("health", "健康档案", Icons.Default.MonitorHeart),
            Triple("stats", "统计分析", Icons.Default.BabyChangingStation),
            Triple("settings", "设置", Icons.Default.Settings),
            Triple("about", "关于", Icons.Default.Info)
        )
        features.forEach { (route, label, icon) ->
            OutlinedCard(
                onClick = { onNavigate(route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun RecentRecords(
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    growths: List<GrowthEntity>
) {
    val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val dateFmt = SimpleDateFormat("MM/dd", Locale.getDefault())

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        feedings.forEach { record ->
            val typeLabels = mapOf("BREAST_MILK" to "母乳", "FORMULA" to "配方奶", "SOLID_FOOD" to "辅食", "WATER" to "水")
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🍼 ${typeLabels[record.type] ?: record.type}", style = MaterialTheme.typography.bodyMedium)
                    Text("${record.amount}${record.unit}  ${fmt.format(Date(record.createdAt))}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        sleeps.take(2).forEach { record ->
            val typeLabel = if (record.type == "NIGHT") "夜间睡眠" else "小睡"
            val hours = (record.endTime - record.startTime) / 3_600_000
            val minutes = ((record.endTime - record.startTime) % 3_600_000) / 60_000
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🛏 $typeLabel", style = MaterialTheme.typography.bodyMedium)
                    Text("${hours}h${minutes}m  ${dateFmt.format(Date(record.startTime))}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        growths.take(1).forEach { record ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📏 生长记录", style = MaterialTheme.typography.bodyMedium)
                    Text("${record.height}cm  ${record.weight}kg  ${dateFmt.format(Date(record.recordDate))}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun ageInMonths(birthdayMillis: Long): Int {
    val birth = Calendar.getInstance().apply { timeInMillis = birthdayMillis }
    val now = Calendar.getInstance()
    val years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    val months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
    return years * 12 + months
}

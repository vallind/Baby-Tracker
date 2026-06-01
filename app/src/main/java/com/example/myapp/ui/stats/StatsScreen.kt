package com.example.myapp.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("统计分析") }) }) { innerPadding ->
        if (state.loading) LoadingView()
        else {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ParentingCard(modifier = Modifier.weight(1f)) {
                        Text("喂养次数", style = MaterialTheme.typography.titleSmall)
                        Text("${state.feedingCount}", style = MaterialTheme.typography.headlineLarge)
                    }
                    ParentingCard(modifier = Modifier.weight(1f)) {
                        Text("睡眠总时长", style = MaterialTheme.typography.titleSmall)
                        Text("${"%.1f".format(state.sleepHours)}h", style = MaterialTheme.typography.headlineLarge)
                    }
                }
                if (state.growthTrend.isNotEmpty()) {
                    ParentingCard {
                        Text("身高趋势", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        val modelProducer = remember { ChartEntryModelProducer() }
                        LaunchedEffect(state.growthTrend) {
                            modelProducer.setEntries(listOf(state.growthTrend.mapIndexed { i, v -> FloatEntry(i.toFloat(), v) }))
                        }
                        Chart(
                            lineChart(),
                            modelProducer,
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        )
                    }
                }
            }
        }
    }
}

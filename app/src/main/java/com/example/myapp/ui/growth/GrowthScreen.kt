package com.example.myapp.ui.growth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.EmptyState
import com.example.myapp.ui.designsystem.ErrorView
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    viewModel: GrowthViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("身高", "体重", "头围")

    Scaffold(
        topBar = { TopAppBar(title = { Text("生长记录") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(message = state.error!!)
            state.records.isEmpty() -> EmptyState(message = "暂无生长记录")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }

                item {
                    val sorted = state.records.sortedBy { it.recordDate }
                    val entries = when (selectedTab) {
                        0 -> sorted.mapIndexed { i, r -> entryOf(i.toFloat(), r.height) }
                        1 -> sorted.mapIndexed { i, r -> entryOf(i.toFloat(), r.weight) }
                        else -> sorted.mapIndexed { i, r -> entryOf(i.toFloat(), r.headCircumference) }
                    }

                    val modelProducer = remember { ChartEntryModelProducer() }
                    val chart = remember { LineChart() }

                    LaunchedEffect(state.records, selectedTab) {
                        modelProducer.setEntries(listOf(entries))
                    }

                    Chart(
                        chart = chart,
                        chartModelProducer = modelProducer,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    )
                }

                items(state.records, key = { it.id }) { record ->
                    val fmt = SimpleDateFormat("MM/dd", Locale.getDefault())
                    ParentingCard {
                        Column {
                            Text(fmt.format(Date(record.recordDate)), style = MaterialTheme.typography.titleMedium)
                            Text("身高: ${record.height}cm  体重: ${record.weight}kg  头围: ${record.headCircumference}cm")
                        }
                    }
                }
            }
        }
    }
}

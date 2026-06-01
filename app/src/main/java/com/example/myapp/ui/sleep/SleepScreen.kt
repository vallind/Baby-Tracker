package com.example.myapp.ui.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.room.SleepEntity
import com.example.myapp.ui.designsystem.EmptyState
import com.example.myapp.ui.designsystem.ErrorView
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("睡眠记录") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(message = state.error!!)
            state.records.isEmpty() -> EmptyState(message = "暂无睡眠记录")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.records, key = { it.id }) { record ->
                    val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                    val durationMs = record.endTime - record.startTime
                    val hours = durationMs / 3_600_000
                    val minutes = (durationMs % 3_600_000) / 60_000
                    val typeLabel = if (record.type == "NIGHT") "夜间睡眠" else "白天小睡"

                    ParentingCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(typeLabel, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${fmt.format(Date(record.startTime))} → ${fmt.format(Date(record.endTime))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${hours}小时${minutes}分钟",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                            IconButton(onClick = { viewModel.onEvent(SleepEvent.Delete(record)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    }
                }
            }
        }
    }
}

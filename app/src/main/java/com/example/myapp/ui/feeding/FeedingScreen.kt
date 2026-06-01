package com.example.myapp.ui.feeding

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
import com.example.myapp.ui.designsystem.EmptyState
import com.example.myapp.ui.designsystem.ErrorView
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScreen(
    viewModel: FeedingViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("喂养记录") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(message = state.error!!)
            state.records.isEmpty() -> EmptyState(message = "暂无喂养记录")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.records, key = { it.id }) { record ->
                    val typeLabels = mapOf(
                        "BREAST_MILK" to "母乳",
                        "FORMULA" to "配方奶",
                        "SOLID_FOOD" to "辅食",
                        "WATER" to "水"
                    )
                    val timeStr = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                        .format(Date(record.createdAt))
                    ParentingCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    typeLabels[record.type] ?: record.type,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${record.amount} ${record.unit}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(timeStr, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.onEvent(FeedingEvent.Delete(record)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    }
                }
            }
        }
    }
}

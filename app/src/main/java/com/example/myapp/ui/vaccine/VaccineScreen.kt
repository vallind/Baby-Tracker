package com.example.myapp.ui.vaccine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.graphics.Color
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
fun VaccineScreen(
    viewModel: VaccineViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("疫苗接种") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(message = state.error!!)
            state.vaccines.isEmpty() -> EmptyState(message = "暂无疫苗记录")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.vaccines, key = { it.id }) { vaccine ->
                    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val statusLabel = when (vaccine.status) {
                        "COMPLETED" -> "已接种"
                        "EXPIRED" -> "已过期"
                        else -> "待接种"
                    }

                    ParentingCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${vaccine.name}（第${vaccine.dose}针）", style = MaterialTheme.typography.titleMedium)
                                Text("计划接种: ${fmt.format(Date(vaccine.plannedDate))}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                AssistChip(onClick = {}, label = { Text(statusLabel) })
                            }
                            if (vaccine.status == "PENDING") {
                                IconButton(onClick = { viewModel.onEvent(VaccineEvent.MarkCompleted(vaccine.id)) }) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "标记完成", tint = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

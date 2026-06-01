package com.example.myapp.ui.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.ParentingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepScreen(
    onSave: (startTime: Long, endTime: Long, type: String) -> Unit,
    onBack: () -> Unit
) {
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableStateOf(System.currentTimeMillis() + 3600_000) }
    var sleepType by remember { mutableStateOf("NAP") }
    var expanded by remember { mutableStateOf(false) }

    val fmt = java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("新增睡眠记录", style = MaterialTheme.typography.titleLarge)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = if (sleepType == "NIGHT") "夜间睡眠" else "白天小睡",
                onValueChange = {},
                readOnly = true,
                label = { Text("类型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("白天小睡") }, onClick = { sleepType = "NAP"; expanded = false })
                DropdownMenuItem(text = { Text("夜间睡眠") }, onClick = { sleepType = "NIGHT"; expanded = false })
            }
        }

        Text("开始时间: ${fmt.format(java.util.Date(startTime))}")
        Text("结束时间: ${fmt.format(java.util.Date(endTime))}")

        ParentingButton(text = "保存", onClick = {
            onSave(startTime, endTime, sleepType)
            onBack()
        })
    }
}

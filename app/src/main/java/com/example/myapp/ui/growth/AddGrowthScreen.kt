package com.example.myapp.ui.growth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun AddGrowthScreen(
    onSave: (height: Float, weight: Float, headCircumference: Float, date: Long) -> Unit,
    onBack: () -> Unit
) {
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var headText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("新增生长记录", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it },
            label = { Text("身高 (cm)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("体重 (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = headText,
            onValueChange = { headText = it },
            label = { Text("头围 (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        ParentingButton(
            text = "保存",
            enabled = heightText.isNotBlank() && weightText.isNotBlank(),
            onClick = {
                val h = heightText.toFloatOrNull() ?: return@ParentingButton
                val w = weightText.toFloatOrNull() ?: return@ParentingButton
                val hc = headText.toFloatOrNull() ?: 0f
                onSave(h, w, hc, System.currentTimeMillis())
                onBack()
            }
        )
    }
}

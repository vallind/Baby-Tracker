package com.example.myapp.ui.feeding

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
fun AddFeedingScreen(
    onSave: (type: String, amount: Int, unit: String, note: String?) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf("BREAST_MILK") }
    var amountText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("ml") }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val types = listOf(
        "BREAST_MILK" to "母乳",
        "FORMULA" to "配方奶",
        "SOLID_FOOD" to "辅食",
        "WATER" to "水"
    )
    val typeLabels = types.toMap()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("新增喂养记录", style = MaterialTheme.typography.titleLarge)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = typeLabels[selectedType] ?: selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("类型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                types.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = { selectedType = key; expanded = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("用量") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth()
        )

        ParentingButton(
            text = "保存",
            enabled = amountText.isNotBlank(),
            onClick = {
                val amount = amountText.toIntOrNull() ?: return@ParentingButton
                onSave(selectedType, amount, unit, note.ifBlank { null })
                onBack()
            }
        )
    }
}

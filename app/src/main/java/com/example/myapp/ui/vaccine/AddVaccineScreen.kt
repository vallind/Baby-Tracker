package com.example.myapp.ui.vaccine

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
fun AddVaccineScreen(
    onSave: (name: String, dose: Int, plannedDate: Long) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var doseText by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("新增疫苗", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("疫苗名称") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = doseText, onValueChange = { doseText = it }, label = { Text("针次") }, modifier = Modifier.fillMaxWidth())
        ParentingButton(
            text = "保存", enabled = name.isNotBlank(),
            onClick = {
                val dose = doseText.toIntOrNull() ?: return@ParentingButton
                onSave(name, dose, System.currentTimeMillis() + 30L * 24 * 3600_000)
                onBack()
            }
        )
    }
}

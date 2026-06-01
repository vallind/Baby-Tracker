package com.example.myapp.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.room.HealthProfileEntity
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(viewModel: HealthViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("健康档案") }) }) { innerPadding ->
        if (state.loading) LoadingView()
        else {
            var allergies by remember { mutableStateOf(state.profile?.allergies ?: "") }
            var history by remember { mutableStateOf(state.profile?.medicalHistory ?: "") }
            var notes by remember { mutableStateOf(state.profile?.doctorNotes ?: "") }

            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("健康档案", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("过敏史") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = history, onValueChange = { history = it }, label = { Text("既往病史") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("医生备注") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Spacer(modifier = Modifier.height(8.dp))
                ParentingButton(text = "保存", onClick = {
                    viewModel.onEvent(HealthEvent.Save(
                        HealthProfileEntity(id = state.profile?.id ?: 0, allergies = allergies.ifBlank { null }, medicalHistory = history.ifBlank { null }, doctorNotes = notes.ifBlank { null })
                    ))
                })
            }
        }
    }
}

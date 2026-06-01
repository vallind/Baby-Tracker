package com.example.myapp.ui.messages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapp.ui.designsystem.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("消息") }) }
    ) { innerPadding ->
        EmptyState(
            message = "暂无消息",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

package com.teduniversity.medicalai.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.TextField

@Composable
fun InputScreen(onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Semptomlarını yaz", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Öksürük, ateş…") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onSubmit(text) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Gönder") }
    }
}

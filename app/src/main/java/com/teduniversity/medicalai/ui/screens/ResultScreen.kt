package com.teduniversity.medicalai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button

@Composable
fun ResultScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Sonuç:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                "Örnek: Soğuk algınlığı belirtileri var.",
                Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, Modifier.fillMaxWidth()) { Text("Geri") }
    }
}

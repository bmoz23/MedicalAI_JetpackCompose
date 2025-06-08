package com.teduniversity.medicalai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teduniversity.medicalai.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bu versiyon ChatViewModel'i kullanıyor.
 */
@ExperimentalMaterial3Api
@Composable
fun ChatScreenWithViewModel(
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                // ChatApiService'i kullanarak ChatViewModel örneği oluşturuyoruz
                val apiService = com.teduniversity.medicalai.data.RetrofitInstance.api
                return ChatViewModel(apiService) as T
            }
        }
    )
) {
    // 1) Mesaj listesini ViewModel üzerinden alıyoruz:
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 2) Kullanıcı kutusunun state'ini izleyelim:
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { }, // Title kaldırıldı
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = com.teduniversity.medicalai.ui.theme.BrandOnSecondaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = com.teduniversity.medicalai.ui.theme.BrandSecondaryGreen,
                    navigationIconContentColor = com.teduniversity.medicalai.ui.theme.BrandOnSecondaryGreen
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // —— 1) Mesaj Listesi ——
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                
                // Show typing indicator when loading
                if (isLoading && messages.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            // —— 2) Mesaj Yazma Alanı + Gönder Butonu ——
            Divider(color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val newText = textState.text.trim()
                        if (newText.isNotEmpty()) {
                            // 3) ViewModel'e mesajı gönderiyoruz:
                            viewModel.sendChatMessage(newText)
                            textState = TextFieldValue("")
                        }
                    },
                    enabled = !isLoading && textState.text.trim().isNotEmpty(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (!isLoading && textState.text.trim().isNotEmpty())
                                com.teduniversity.medicalai.ui.theme.BrandSecondaryGreen
                            else
                                com.teduniversity.medicalai.ui.theme.BrandSecondaryGreen.copy(alpha = 0.6f)
                        )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * ChatBubble fonksiyonu bire bir önceki örnektekiyle aynı.
 * Burada yeniden ekliyoruz:
 */
@Composable
private fun ChatBubble(message: ChatViewModel.ChatMessage) {
    val alignment = if (message.isMine) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isMine) com.teduniversity.medicalai.ui.theme.PrimaryContainer // açık mavi (user)
    else com.teduniversity.medicalai.ui.theme.SecondaryContainer // açık yeşil (agent)
    val contentColor = if (message.isMine) com.teduniversity.medicalai.ui.theme.OnPrimaryContainer
    else com.teduniversity.medicalai.ui.theme.OnSecondaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            tonalElevation = if (message.isMine) 2.dp else 0.dp,
            shape = MaterialTheme.shapes.large,
            color = bubbleColor,
            contentColor = contentColor,
            modifier = Modifier
                .widthIn(max = 260.dp)
                .padding(horizontal = 4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                Text(
                    text = timeFormat.format(message.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = contentColor.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

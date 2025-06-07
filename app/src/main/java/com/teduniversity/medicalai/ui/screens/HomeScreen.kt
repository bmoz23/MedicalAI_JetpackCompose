package com.teduniversity.medicalai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teduniversity.medicalai.ui.theme.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

// ----------------------------------------------------
// 1) CustomBottomBar: Pastel yeşil zemin ve yeşil vurgu
// ----------------------------------------------------
@Composable
fun CustomBottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    // Home, Reports, Profile
    val items = listOf(
        Icons.Default.Home,
        Icons.Default.ChatBubble,  // rapor ikonunu buraya da koyabilirsin
        Icons.Default.Person
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(32.dp),
                color = SecondaryContainer,   // pastel yeşil zemin
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    items.forEachIndexed { index, icon ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIndex == index)
                                        BrandSecondaryGreen.copy(alpha = 0.2f)
                                    else
                                        Color.Transparent
                                )
                                .clickable { onItemSelected(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (selectedIndex == index)
                                    BrandSecondaryGreen       // seçili: parlak yeşil
                                else
                                    OnSecondaryContainer,     // seçilmemiş: koyu yeşil
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------
// 2) Model: Chat Geçmişi Verisi
// ----------------------------
data class ChatHistoryItem(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Date
)

// ------------------------------------------------
// 3) CTA Kart bileşeni: “New Chat with MedicalAI”
// ------------------------------------------------
@Composable
fun BigCTACard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = BrandPrimaryBlue  // derin mavi
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            BrandPrimaryBlue,
                            BrandPrimaryBlue.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandOnPrimaryBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = BrandOnPrimaryBlue
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "NEW CHAT WITH MEDICALAI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = BrandOnPrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Disease Prediction",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandOnPrimaryBlue.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }
    }
}

// ------------------------------------------------
// 4) Chat Geçmişi Satırı bileşeni
// ------------------------------------------------
@Composable
fun ChatHistoryRow(
    item: ChatHistoryItem,
    modifier: Modifier = Modifier,
    onClick: (ChatHistoryItem) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(vertical = 4.dp)
            .clickable { onClick(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),  // beyaz yüzey
        border = BorderStroke(1.dp, AppOutline),                       // gri outline
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubble,
                contentDescription = null,
                tint = BrandPrimaryBlue,  // mavi vurgu
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = AppOnSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppOnSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            Text(
                text = remember(item.timestamp) {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.timestamp)
                },
                style = MaterialTheme.typography.labelSmall,
                color = AppOnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// ------------------------------------------------
// 5) HomeScreen bileşeni
// ------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNotificationClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onReportClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOpenChatHistory: (ChatHistoryItem) -> Unit
) {
    // Kullanıcı adı
    val user = Firebase.auth.currentUser
    val rawName = when {
        !user?.displayName.isNullOrBlank() -> user!!.displayName!!
        !user?.email.isNullOrBlank() && user!!.email!!.contains("@") -> user.email!!.substringBefore("@")
        else -> "User"
    }
    val displayName = rawName.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    var bottomSelectedIndex by remember { mutableStateOf(0) }

    val sampleHistory = remember {
        listOf(
            ChatHistoryItem("1", "DR. BOT ile 15 MAYIS", "Öksürüğünüz hâlâ devam ediyorsa...", Calendar.getInstance().apply {
                set(2023, 4, 15, 14, 35)
            }.time),
            ChatHistoryItem("2", "DR. BOT ile 10 MAYIS", "Önce ateşinizi kontrol edin...", Calendar.getInstance().apply {
                set(2023, 4, 10, 9, 20)
            }.time),
            ChatHistoryItem("3", "DR. BOT ile 05 MAYIS", "Başınız ağrıyorsa ibuprofen...", Calendar.getInstance().apply {
                set(2023, 4, 5, 18, 10)
            }.time)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackground,   // ana arkaplan

        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "Welcome, $displayName",
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandOnSecondaryGreen
                    )
                },
                actions = {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = BrandOnSecondaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = BrandSecondaryGreen
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        },

        bottomBar = {
            CustomBottomBar(
                selectedIndex = bottomSelectedIndex,
                onItemSelected = { idx ->
                    bottomSelectedIndex = idx
                    when (idx) {
                        0 -> { /* Home */ }
                        1 -> onReportClick()
                        2 -> onProfileClick()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            BigCTACard(onClick = onNewChatClick)
            Spacer(Modifier.height(20.dp))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat Histories",
                    style = MaterialTheme.typography.titleSmall,
                    color = AppOnBackground
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrandPrimaryBlue,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.clickable { /* See All */ }
                )
            }
            Spacer(Modifier.height(12.dp))

            val listState: LazyListState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(sampleHistory) { chatItem ->
                    ChatHistoryRow(
                        item = chatItem,
                        onClick = { onOpenChatHistory(chatItem) }
                    )
                }
            }
        }
    }
}

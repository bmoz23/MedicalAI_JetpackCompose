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
import androidx.compose.ui.unit.sp
import com.teduniversity.medicalai.ui.theme.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teduniversity.medicalai.viewmodel.NotificationViewModel
import android.util.Log

// ----------------------------
// 1) Model: Chat Geçmişi Verisi
// ----------------------------
data class ChatHistoryItem(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Date
)

// ------------------------------------------------
// 2) CTA Kart bileşeni: "New Chat with MedicalAI"
// ------------------------------------------------
@Composable
fun BigCTACard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BrandSecondaryGreen,
                            BrandSecondaryGreen.copy(alpha = 0.9f),
                            BrandSecondaryGreen.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    BrandOnSecondaryGreen.copy(alpha = 0.3f),
                                    BrandOnSecondaryGreen.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = BrandOnSecondaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "NEW CHAT WITH",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BrandOnSecondaryGreen.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = "MedicalAI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = BrandOnSecondaryGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(BrandOnSecondaryGreen.copy(alpha = 0.8f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Disease Prediction & Health Analysis",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandOnSecondaryGreen.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------
// 3) Chat Geçmişi Satırı bileşeni
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
            .height(80.dp)
            .padding(vertical = 6.dp)
            .clickable { onClick(item) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppSurface.copy(alpha = 0.95f)
        ),
        border = BorderStroke(
            1.dp, 
            Brush.horizontalGradient(
                colors = listOf(
                    BrandSecondaryGreen.copy(alpha = 0.3f),
                    BrandPrimaryBlue.copy(alpha = 0.2f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppSurface,
                            AppSurface.copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandSecondaryGreen.copy(alpha = 0.15f),
                                BrandSecondaryGreen.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = null,
                    tint = BrandSecondaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = AppOnSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.lastMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        letterSpacing = 0.2.sp
                    ),
                    color = AppOnSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = remember(item.timestamp) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.timestamp)
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = BrandPrimaryBlue.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrandSecondaryGreen.copy(alpha = 0.6f))
                )
            }
        }
    }
}

// ------------------------------------------------
// 4) HomeScreen bileşeni
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
    // State for user information
    var displayName by remember { mutableStateOf("User") }
    var isLoading by remember { mutableStateOf(true) }
    
    // Notification ViewModel
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // Firebase user bilgilerini al
    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            try {
                // Önce Firestore'dan kullanıcı bilgilerini al
                val firestore = Firebase.firestore
                val userDoc = firestore.collection("patients")
                    .document(user.uid)
                    .get()
                    .await()
                
                displayName = when {
                    // Firestore'dan first_name ve last_name al
                    userDoc.exists() -> {
                        val firstName = userDoc.getString("first_name") ?: ""
                        val lastName = userDoc.getString("last_name") ?: ""
                        val fullName = userDoc.getString("full_name") ?: ""
                        
                        when {
                            fullName.isNotBlank() -> fullName.trim()
                            firstName.isNotBlank() && lastName.isNotBlank() -> "$firstName $lastName".trim()
                            firstName.isNotBlank() -> firstName.trim()
                            lastName.isNotBlank() -> lastName.trim()
                            else -> {
                                // Firestore'da isim yoksa Auth'dan al
                                getNameFromAuth(user)
                            }
                        }
                    }
                    else -> getNameFromAuth(user)
                }
                
                // İlk harfi büyük yap
                displayName = displayName.split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                    }
                }
                
            } catch (e: Exception) {
                // Hata durumunda Auth'dan al
                displayName = getNameFromAuth(user)
            }
        } else {
            displayName = "Guest"
        }
        isLoading = false
    }

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
                    Column {
                        if (isLoading) {
                            Text(
                                text = "Welcome...",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = BrandOnPrimaryBlue
                            )
                        } else {
                            Text(
                                text = "Welcome Back,",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.3.sp
                                ),
                                color = BrandOnPrimaryBlue.copy(alpha = 0.9f)
                            )
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = BrandOnPrimaryBlue
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = BrandOnPrimaryBlue.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(onClick = onNotificationClick) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = BrandOnPrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Badge for unread notifications
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = (-6).dp, y = 6.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFFF4444),
                                                Color(0xFFCC0000)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = BrandPrimaryBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                BrandPrimaryBlue,
                                BrandPrimaryBlue.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            BigCTACard(onClick = onNewChatClick)
            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandPrimaryBlue.copy(alpha = 0.05f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandPrimaryBlue)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Chat Histories",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = AppOnBackground
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandPrimaryBlue.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.clickable { /* See All */ }
                    ) {
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BrandPrimaryBlue,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.3.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            val listState: LazyListState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
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

// Helper function to get name from Firebase Auth
private fun getNameFromAuth(user: com.google.firebase.auth.FirebaseUser): String {
    return when {
        !user.displayName.isNullOrBlank() -> {
            user.displayName!!.trim()
        }
        !user.email.isNullOrBlank() && user.email!!.contains("@") -> {
            user.email!!.substringBefore("@").trim()
        }
        else -> "User"
    }
}

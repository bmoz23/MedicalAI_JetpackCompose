package com.teduniversity.medicalai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teduniversity.medicalai.ui.theme.BrandSecondaryGreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

// ----------------------------------------------------
// 1) CustomBottomBar: Pill-şeklinde alt navigasyon çubuğu
// ----------------------------------------------------
@Composable
fun CustomBottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    // İkon listesi: sırasıyla Settings, Place, ShoppingBag, Person
    val items = listOf(
        Icons.Default.Settings,
        Icons.Default.Place,
        Icons.Default.ShoppingBag,
        Icons.Default.Person
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding(), // Status bar + Navigation bar ile çakışmayı önle
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),  // Ekran kenarlarından boşluk
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                tonalElevation = 8.dp,                 // Hafif gölge
                shape = RoundedCornerShape(32.dp),     // Yarıçap 32.dp ile oval şekil
                color = Color.White,                   // Beyaz arka plan
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)                    // Yükseklik 64.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),   // İkonlar arasına yatay boşluk
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    items.forEachIndexed { index, iconImageVector ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)               // Her ikon için kutu boyutu
                                .clip(CircleShape)
                                .background(
                                    if (selectedIndex == index)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else
                                        Color.Transparent
                                )
                                .clickable { onItemSelected(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconImageVector,
                                contentDescription = null,
                                tint = if (selectedIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp) // İkon boyutu
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
    val title: String,       // Örn: "DR. BOT ile 15 MAYIS"
    val lastMessage: String, // Örn: "Öksürüğünüz hâlâ devam ediyorsa..."
    val timestamp: Date      // Örn: new Date()
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
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "NEW CHAT WITH MEDICALAI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Disease Prediction",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            val timeString = remember(item.timestamp) {
                SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(item.timestamp)
            }
            Text(
                text = timeString,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ------------------------------------------------
// 5) HomeScreen bileşeni (CustomBottomBar entegre edilmiş)
// ------------------------------------------------
@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNewChatClick: () -> Unit,
    onReportClick: () -> Unit,
    onOpenChatHistory: (ChatHistoryItem) -> Unit
) {
    // Mevcut kullanıcıyı FirebaseAuth’dan alıyoruz.
    val user = Firebase.auth.currentUser
    val rawName = when {
        !user?.displayName.isNullOrBlank() -> user!!.displayName!!
        !user?.email.isNullOrBlank() && user!!.email!!.contains("@") -> user.email!!.substringBefore("@")
        else -> "USER"
    }
    val displayNameUpper = rawName.uppercase(Locale.getDefault())

    // Pill bar’da seçili index (0..3)
    var bottomSelectedIndex by remember { mutableStateOf(0) }

    // Örnek Chat Geçmişi Listesi (gerçekte Firestore’dan çekeceksin)
    val sampleHistory = remember {
        listOf(
            ChatHistoryItem(
                id = "1",
                title = "DR. BOT ile 15 MAYIS",
                lastMessage = "Öksürüğünüz hâlâ devam ediyorsa...",
                timestamp = Calendar.getInstance().apply {
                    set(2023, 4, 15, 14, 35)
                }.time
            ),
            ChatHistoryItem(
                id = "2",
                title = "DR. BOT ile 10 MAYIS",
                lastMessage = "Önce ateşinizi kontrol edin...",
                timestamp = Calendar.getInstance().apply {
                    set(2023, 4, 10, 9, 20)
                }.time
            ),
            ChatHistoryItem(
                id = "3",
                title = "DR. BOT ile 05 MAYIS",
                lastMessage = "Başınız ağrıyorsa ibuprofen...",
                timestamp = Calendar.getInstance().apply {
                    set(2023, 4, 5, 18, 10)
                }.time
            )
        )
    }

    Scaffold(
        // ============================================
        // Burada sistem çubukları için otomatik padding
        // ============================================
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),

        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "WELCOME, $displayNameUpper",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Çıkış Yap",
                            tint = Color.White
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
            // ---------------------------------------------------
            // CustomBottomBar entegre edildi
            // ---------------------------------------------------
            CustomBottomBar(
                selectedIndex = bottomSelectedIndex,
                onItemSelected = { index ->
                    bottomSelectedIndex = index
                    // Burada index’e göre istediğin navigasyonu yapabilirsin:
                    when (index) {
                        0 -> { /* Settings veya başka bir rota */ }
                        1 -> { /* Place/Location rotası */ }
                        2 -> { onReportClick() /* örn. ReportsScreen’e git */ }
                        3 -> { /* Profile/User rotası */ }
                    }
                }
            )
        },

        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Scaffold’un verdiği innerPadding zaten sistem çubuklarına göre padding içerir
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            // ---------------------------------------------------
            // 1) Büyük CTA Kart
            // ---------------------------------------------------
            BigCTACard(onClick = onNewChatClick)
            Spacer(Modifier.height(20.dp))

            // ---------------------------------------------------
            // 2) Chat Histories Başlığı
            // ---------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat Histories",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.clickable { /* “See All” tıklaması */ }
                )
            }
            Spacer(Modifier.height(12.dp))

            // ---------------------------------------------------
            // 3) Chat Histories Listesi
            // ---------------------------------------------------
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
                        onClick = { clickedItem -> onOpenChatHistory(clickedItem) }
                    )
                }
            }
        }
    }
}

package com.teduniversity.medicalai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.teduniversity.medicalai.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

data class UserProfile(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val age: Int = 0,
    val gender: String = "",
    val location: String = "",
    val createdAt: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf(UserProfile()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }

    // Firebase'den user profilini çek
    LaunchedEffect(Unit) {
        loadUserProfile { profile, errorMsg ->
            userProfile = profile ?: UserProfile()
            error = errorMsg
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandOnPrimaryBlue
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BrandPrimaryBlue
                )
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(AppBackground),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                item {
                    ProfileHeader(userProfile)
                }

                // Personal Information Section
                item {
                    ProfileSection(
                        title = "Kişisel Bilgiler",
                        items = listOf(
                            ProfileItem("Yaş", "${userProfile.age}", Icons.Default.DateRange) {
                                editField = "age"
                                editValue = userProfile.age.toString()
                                showEditDialog = true
                            },
                            ProfileItem("Cinsiyet", userProfile.gender.replaceFirstChar { 
                                if (it == 'M' || it == 'm') 'E' else if (it == 'F' || it == 'f') 'K' else it.uppercaseChar() 
                            }, Icons.Default.Person) {
                                editField = "gender"
                                editValue = userProfile.gender
                                showEditDialog = true
                            },
                            ProfileItem("Konum", userProfile.location.ifEmpty { "Belirtilmemiş" }, Icons.Default.LocationOn) {
                                editField = "location"
                                editValue = userProfile.location
                                showEditDialog = true
                            }
                        )
                    )
                }

                // Contact Information Section
                item {
                    ProfileSection(
                        title = "İletişim Bilgileri",
                        items = listOf(
                            ProfileItem("E-posta", userProfile.email, Icons.Default.Email) {
                                // Email değiştirme daha karmaşık - şimdilik disable
                            },
                            ProfileItem("Telefon", userProfile.phoneNumber.ifEmpty { "Belirtilmemiş" }, Icons.Default.Phone) {
                                editField = "phoneNumber"
                                editValue = userProfile.phoneNumber
                                showEditDialog = true
                            }
                        )
                    )
                }

                // Account Actions
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            text = "Şifre Değiştir",
                            icon = Icons.Default.Lock,
                            onClick = { /* TODO: Implement password change */ }
                        )
                        
                        ActionButton(
                            text = "Çıkış Yap",
                            icon = Icons.Default.Logout,
                            onClick = onLogout,
                            isDestructive = true
                        )
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        EditFieldDialog(
            fieldName = editField,
            currentValue = editValue,
            onValueChange = { editValue = it },
            onDismiss = { showEditDialog = false },
            onConfirm = {
                coroutineScope.launch {
                    updateUserProfile(userProfile, editField, editValue) { success ->
                        if (success) {
                            // Refresh profile
                            coroutineScope.launch {
                                loadUserProfile { profile, _ ->
                                    userProfile = profile ?: UserProfile()
                                }
                            }
                        }
                    }
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ProfileHeader(userProfile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BrandSecondaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(userProfile.fullName.ifEmpty { "${userProfile.firstName} ${userProfile.lastName}" }),
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrandOnSecondaryGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            Text(
                text = when {
                    userProfile.fullName.isNotEmpty() -> userProfile.fullName
                    userProfile.firstName.isNotEmpty() && userProfile.lastName.isNotEmpty() -> 
                        "${userProfile.firstName} ${userProfile.lastName}"
                    userProfile.firstName.isNotEmpty() -> userProfile.firstName
                    userProfile.lastName.isNotEmpty() -> userProfile.lastName
                    userProfile.email.isNotEmpty() -> userProfile.email.substringBefore("@")
                    else -> "Kullanıcı"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppOnSurface
            )
            
            // Creation Date
            Text(
                text = if (userProfile.createdAt > 0) {
                    "Üye olma: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(userProfile.createdAt))}"
                } else "Üye",
                style = MaterialTheme.typography.bodyMedium,
                color = AppOnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppOnSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            items.forEachIndexed { index, item ->
                ProfileItemRow(item)
                if (index < items.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = AppOutline.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileItemRow(item: ProfileItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = BrandSecondaryGreen,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                color = AppOnSurface.copy(alpha = 0.7f)
            )
            Text(
                text = item.value.ifEmpty { "Belirtilmemiş" },
                style = MaterialTheme.typography.bodyMedium,
                color = AppOnSurface
            )
        }
        
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Düzenle",
            tint = BrandPrimaryBlue,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) ErrorRed.copy(alpha = 0.1f) else SecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) ErrorRed else BrandSecondaryGreen,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDestructive) ErrorRed else OnSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldDialog(
    fieldName: String,
    currentValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "${getFieldDisplayName(fieldName)} Düzenle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = onValueChange,
                    label = { Text(getFieldDisplayName(fieldName)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandSecondaryGreen,
                        focusedLabelColor = BrandSecondaryGreen
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İptal")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandSecondaryGreen
                        )
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

data class ProfileItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// Helper Functions
private fun getInitials(fullName: String): String {
    return if (fullName.isNotBlank()) {
        fullName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
    } else "U"
}

private fun getFieldDisplayName(fieldName: String): String {
    return when (fieldName) {
        "age" -> "Yaş"
        "gender" -> "Cinsiyet"
        "location" -> "Konum"
        "phoneNumber" -> "Telefon"
        else -> fieldName
    }
}

private suspend fun loadUserProfile(callback: (UserProfile?, String?) -> Unit) {
    try {
        val user = Firebase.auth.currentUser
        if (user != null) {
            Log.d("ProfileScreen", "Loading profile for user: ${user.uid}")
            val firestore = Firebase.firestore
            val document = firestore.collection("patients")
                .document(user.uid)
                .get()
                .await()
            
            Log.d("ProfileScreen", "Document exists: ${document.exists()}")
            Log.d("ProfileScreen", "Document data: ${document.data}")
            
            if (document.exists()) {
                val data = document.data
                Log.d("ProfileScreen", "Raw data: $data")
                
                val profile = UserProfile(
                    uid = document.getString("uid") ?: user.uid,
                    firstName = document.getString("first_name") ?: "",
                    lastName = document.getString("last_name") ?: "",
                    fullName = document.getString("full_name") ?: "",
                    email = document.getString("email") ?: user.email ?: "",
                    phoneNumber = document.get("phone_number")?.toString() ?: "",
                    age = when (val ageValue = document.get("age")) {
                        is Long -> ageValue.toInt()
                        is Double -> ageValue.toInt()
                        is String -> ageValue.toIntOrNull() ?: 0
                        else -> 0
                    },
                    gender = document.getString("gender") ?: "",
                    location = document.getString("location") ?: "",
                    createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                )
                
                Log.d("ProfileScreen", "Parsed profile: $profile")
                callback(profile, null)
            } else {
                Log.e("ProfileScreen", "Document does not exist for user: ${user.uid}")
                callback(null, "Profil bulunamadı")
            }
        } else {
            Log.e("ProfileScreen", "User is null")
            callback(null, "Kullanıcı oturum açmamış")
        }
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error loading profile", e)
        callback(null, e.message)
    }
}

private suspend fun updateUserProfile(
    currentProfile: UserProfile,
    fieldName: String,
    newValue: String,
    callback: (Boolean) -> Unit
) {
    try {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val firestore = Firebase.firestore
            val updateData: Map<String, Any> = when (fieldName) {
                "age" -> mapOf("age" to (newValue.toIntOrNull() ?: 0))
                "gender" -> mapOf("gender" to newValue)
                "location" -> mapOf("location" to newValue)
                "phoneNumber" -> mapOf("phone_number" to newValue)
                else -> emptyMap()
            }
            
            firestore.collection("patients")
                .document(user.uid)
                .update(updateData)
                .await()
            
            callback(true)
        } else {
            callback(false)
        }
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error updating profile", e)
        callback(false)
    }
} 
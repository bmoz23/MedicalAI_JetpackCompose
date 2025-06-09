//Login.kt
package com.teduniversity.medicalai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import kotlin.math.sin
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.teduniversity.medicalai.ui.theme.*

@Composable
fun AnimatedMedicalIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "medical_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val shadowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow_scale"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        // Pulse shadow effect
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(shadowScale)
                .clip(CircleShape)
                .background(BrandSecondaryGreen.copy(alpha = 0.2f))
        )
        
        // Main heart icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandSecondaryGreen,
                            BrandSecondaryGreen.copy(alpha = 0.8f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Medical Heart",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val auth = Firebase.auth
    val firestore = Firebase.firestore

    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf<String?>(null) }
    var loading      by remember { mutableStateOf(false) }
    var welcomeMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandPrimaryBlue.copy(alpha = 0.1f),
                        BrandSecondaryGreen.copy(alpha = 0.05f),
                        Color.White
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Animated Medical Icon
            AnimatedMedicalIcon()

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Title
            Text(
                text = "MedicalAI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = BrandPrimaryBlue,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your Personal Medical Assistant",
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPrimaryBlue.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimaryBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Sign in to continue your health journey",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandPrimaryBlue.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = BrandSecondaryGreen
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandSecondaryGreen,
                            unfocusedBorderColor = BrandSecondaryGreen.copy(alpha = 0.5f),
                            cursorColor = BrandSecondaryGreen,
                            focusedLabelColor = BrandSecondaryGreen,
                            unfocusedLabelColor = BrandSecondaryGreen.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = BrandSecondaryGreen
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = BrandSecondaryGreen.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandSecondaryGreen,
                            unfocusedBorderColor = BrandSecondaryGreen.copy(alpha = 0.5f),
                            cursorColor = BrandSecondaryGreen,
                            focusedLabelColor = BrandSecondaryGreen,
                            unfocusedLabelColor = BrandSecondaryGreen.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Hata veya yükleniyor göstergesi
                    error?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorRed.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = it,
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Welcome message
                    welcomeMessage?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = BrandSecondaryGreen.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = it,
                                color = BrandSecondaryGreen,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (loading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = BrandSecondaryGreen,
                            trackColor = BrandSecondaryGreen.copy(alpha = 0.2f)
                        )
                    }

                    // Giriş butonu
                    Button(
                        onClick = {
                            // Basit alan kontrolleri
                            if (email.isBlank() || password.isBlank()) {
                                error = "Email and password must not be empty"
                                return@Button
                            }
                            error = null
                            loading = true

                            // 1) FirebaseAuth ile giriş denemesi
                            auth.signInWithEmailAndPassword(email.trim(), password.trim())
                                .addOnSuccessListener { authResult ->
                                    // Giriş başarılı → role kontrolü yapmak için Firestore'a gidiyoruz
                                    val uid = authResult.user?.uid
                                    if (uid == null) {
                                        loading = false
                                        error = "User ID could not be retrieved."
                                        return@addOnSuccessListener
                                    }

                                    // 2) Firestore'dan users/{uid} belgesini oku
                                    firestore.collection("patients")
                                        .document(uid)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                val role = document.getString("role")
                                                if (role == "patient") {
                                                    // Sadece "patient" rolüne izin ver
                                                    val fullName = document.getString("full_name") ?: "User"
                                                    welcomeMessage = "Welcome back, $fullName!"
                                                    
                                                    // Welcome mesajı gösterip 2 saniye bekle sonra home'a git
                                                    coroutineScope.launch {
                                                        delay(2000)
                                                        loading = false
                                                        welcomeMessage = null
                                                        onLoggedIn()
                                                    }
                                                } else {
                                                    // Başka bir rol (örneğin "doctor") varsa çıkış yap
                                                    loading = false
                                                    auth.signOut()
                                                    error = "This account is not a patient account."
                                                }
                                            } else {
                                                // Firestore'da belge yoksa
                                                loading = false
                                                auth.signOut()
                                                error = "User information not found."
                                            }
                                        }
                                        .addOnFailureListener { firestoreError ->
                                            loading = false
                                            auth.signOut()
                                            error = firestoreError.message
                                        }
                                }
                                .addOnFailureListener { authError ->
                                    loading = false
                                    error = authError.message
                                }
                        },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandSecondaryGreen,
                            disabledContainerColor = BrandSecondaryGreen.copy(alpha = 0.6f)
                        )
                    ) {
                        if (loading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Text(
                                    if (welcomeMessage != null) "Getting Ready..." else "Signing In...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                "Sign In",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Kayıt ol linki
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ClickableText(
                            text = buildAnnotatedString {
                                append("Don’t have an account? ")
                                withStyle(
                                    style = SpanStyle(
                                        color = BrandSecondaryGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("Sign Up")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BrandPrimaryBlue.copy(alpha = 0.8f)
                            ),
                            onClick = { onSignUpClick() }
                        )
                    }
                }
            }
        }
    }
}
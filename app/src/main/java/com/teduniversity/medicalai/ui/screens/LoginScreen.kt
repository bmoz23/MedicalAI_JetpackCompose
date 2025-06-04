//Login.kt
package com.teduniversity.medicalai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.elevatedCardColors
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.teduniversity.medicalai.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Arka plan görseli (login_bg.xml)
        Image(
            painter = painterResource(R.drawable.login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.TopCenter)
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 160.dp),
            shape = RoundedCornerShape(24.dp),
            colors = elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Login", style = MaterialTheme.typography.headlineMedium)
                Text("Welcome back, please login to your account", style = MaterialTheme.typography.bodyMedium)

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor          = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor          = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Hata veya yükleniyor göstergesi
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                if (loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
                                    error = "Kullanıcı ID alınamadı."
                                    return@addOnSuccessListener
                                }

                                // 2) Firestore'dan users/{uid} belgesini oku
                                firestore.collection("patients")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        loading = false
                                        if (document.exists()) {
                                            val role = document.getString("role")
                                            if (role == "patient") {
                                                // Sadece "patient" rolüne izin ver
                                                onLoggedIn()
                                            } else {
                                                // Başka bir rol (örneğin "doctor") varsa çıkış yap
                                                auth.signOut()
                                                error = "Bu hesap bir hasta hesabı değil."
                                            }
                                        } else {
                                            // Firestore'da belge yoksa
                                            auth.signOut()
                                            error = "Kullanıcı bilgisi bulunamadı."
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Sign In", color = MaterialTheme.colorScheme.onPrimary)
                }

                // Kayıt ol linki
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ClickableText(
                        text = buildAnnotatedString {
                            append("Don’t have an account? ")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Sign Up")
                            }
                        },
                        modifier = Modifier,
                        style = MaterialTheme.typography.bodyMedium,
                        onClick = { onSignUpClick() }
                    )
                }
            }
        }
    }
}
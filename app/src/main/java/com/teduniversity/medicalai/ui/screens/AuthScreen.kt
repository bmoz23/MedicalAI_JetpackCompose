package com.teduniversity.medicalai.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun AuthScreen(onLoggedIn: () -> Unit) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error    by remember { mutableStateOf<String?>(null) }
    val auth     = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))

        // Giriş Yap Butonu
        Button(
            onClick = {
                val e = email.trim()
                val p = password.trim()
                // Boş geçiş kontrolü
                if (e.isEmpty() || p.isEmpty()) {
                    error = "Email and password cannot be empty"
                    return@Button
                }
                // Email formatı kontrolü
                if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                    error = "Please enter a valid email"
                    return@Button
                }
                // Şifre uzunluğu kontrolü
                if (p.length < 6) {
                    error = "Password must be at least 6 characters"
                    return@Button
                }
                // Firebase ile giriş denemesi
                auth.signInWithEmailAndPassword(e, p)
                    .addOnSuccessListener { onLoggedIn() }
                    .addOnFailureListener { error = it.message }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(Modifier.height(8.dp))

        // Kayıt Ol Butonu
        TextButton(
            onClick = {
                val e = email.trim()
                val p = password.trim()
                // Aynı kontrolleri burada da yapıyoruz
                if (e.isEmpty() || p.isEmpty()) {
                    error = "Email ve şifre boş olamaz"
                    return@TextButton
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                    error = "Lütfen geçerli bir email girin"
                    return@TextButton
                }
                if (p.length < 6) {
                    error = "Şifre en az 6 karakter olmalı"
                    return@TextButton
                }
                // Firebase ile kayıt denemesi
                auth.createUserWithEmailAndPassword(e, p)
                    .addOnSuccessListener { onLoggedIn() }
                    .addOnFailureListener { error = it.message }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Kayıt Ol")
        }
    }
}

package com.teduniversity.medicalai.ui.screens

import androidx.compose.foundation.text.KeyboardOptions
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.teduniversity.medicalai.ui.theme.*

@Composable
fun SimpleIcon() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(BrandSecondaryGreen),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = "Sign Up",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    val auth      = Firebase.auth
    val firestore = Firebase.firestore
    val context   = LocalContext.current

    // Form state
    var name         by remember { mutableStateOf("") }
    var surname      by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var age          by remember { mutableStateOf("") }
    var gender       by remember { mutableStateOf("") }
    var phoneNumber  by remember { mutableStateOf("") }

    var error   by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(60.dp))

            // Simple Icon
            SimpleIcon()

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Title
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = BrandPrimaryBlue,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Join us to start your health journey",
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPrimaryBlue.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Sign Up Card
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // First Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("First Name") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
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
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Last Name
                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        label = { Text("Last Name") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
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
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
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
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
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
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Age
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter(Char::isDigit) },
                        label = { Text("Age") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandSecondaryGreen,
                            unfocusedBorderColor = BrandSecondaryGreen.copy(alpha = 0.5f),
                            cursorColor = BrandSecondaryGreen,
                            focusedLabelColor = BrandSecondaryGreen,
                            unfocusedLabelColor = BrandSecondaryGreen.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Gender
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        placeholder = { Text("Male / Female / Other") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandSecondaryGreen,
                            unfocusedBorderColor = BrandSecondaryGreen.copy(alpha = 0.5f),
                            cursorColor = BrandSecondaryGreen,
                            focusedLabelColor = BrandSecondaryGreen,
                            unfocusedLabelColor = BrandSecondaryGreen.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it.filter(Char::isDigit) },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandSecondaryGreen,
                            unfocusedBorderColor = BrandSecondaryGreen.copy(alpha = 0.5f),
                            cursorColor = BrandSecondaryGreen,
                            focusedLabelColor = BrandSecondaryGreen,
                            unfocusedLabelColor = BrandSecondaryGreen.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        )
                    )

                    // Error
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

                    // Loading
                    if (loading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = BrandSecondaryGreen,
                            trackColor = BrandSecondaryGreen.copy(alpha = 0.2f)
                        )
                    }

                    // Register Button
                    Button(
                        onClick = {
                            // Basit validasyon
                            when {
                                name.isBlank() || surname.isBlank() ->
                                    error = "Name & surname are required"
                                email.isBlank() ->
                                    error = "Email is required"
                                password.length < 6 ->
                                    error = "Password must be at least 6 chars"
                                age.toIntOrNull() == null ->
                                    error = "Valid age is required"
                                gender.isBlank() ->
                                    error = "Gender is required"
                                phoneNumber.isBlank() ->
                                    error = "Phone number is required"
                                else -> {
                                    error = null
                                    loading = true
                                    // 1) Auth ile kullanıcı oluştur
                                    auth.createUserWithEmailAndPassword(
                                        email.trim(), password.trim()
                                    ).addOnSuccessListener { cred ->
                                        val uid = cred.user!!.uid
                                        val fullName = "${name.trim()} ${surname.trim()}"
                                        // 2) DisplayName ayarla
                                        val profileUpdate = userProfileChangeRequest {
                                            displayName = fullName
                                        }
                                        cred.user!!
                                            .updateProfile(profileUpdate)
                                            .addOnCompleteListener {
                                                // 3) Firestore'a hasta dokümanı kaydet
                                                val userData = hashMapOf(
                                                    "uid"           to uid,
                                                    "first_name"    to name.trim(),
                                                    "last_name"     to surname.trim(),
                                                    "full_name"     to fullName,
                                                    "email"         to email.trim(),
                                                    "role"          to "patient",
                                                    "createdAt"     to Timestamp.now(),
                                                    "age"           to age.toInt(),
                                                    "gender"        to gender.trim(),
                                                    "phone_number"  to phoneNumber.trim()
                                                )
                                                firestore
                                                    .collection("patients")
                                                    .document(uid)
                                                    .set(userData)
                                                    .addOnSuccessListener {
                                                        loading = false
                                                        Toast.makeText(
                                                            context,
                                                            "Registration successful!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        onBackToSignIn()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        loading = false
                                                        error = e.message
                                                    }
                                            }
                                    }.addOnFailureListener { e ->
                                        loading = false
                                        error = e.message
                                    }
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandSecondaryGreen,
                            disabledContainerColor = BrandSecondaryGreen.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "Create Account",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Already have account?
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ClickableText(
                            text = buildAnnotatedString {
                                append("Already have an account? ")
                                withStyle(
                                    SpanStyle(
                                        color = BrandPrimaryBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("Sign In")
                                }
                            },
                            onClick = { onBackToSignIn() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

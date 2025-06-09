package com.teduniversity.medicalai.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teduniversity.medicalai.ui.screens.HomeScreen
import com.teduniversity.medicalai.ui.screens.InputScreen
import com.teduniversity.medicalai.ui.screens.LoginScreen
import com.teduniversity.medicalai.ui.screens.MainScreen
import com.teduniversity.medicalai.ui.screens.ReportsScreen
import com.teduniversity.medicalai.ui.screens.SignUpScreen
import com.teduniversity.medicalai.ui.screens.NotificationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val auth = Firebase.auth
    
    // Authentication durumunu dinle
    var isAuthenticated by remember { mutableStateOf<Boolean?>(null) }
    
    LaunchedEffect(Unit) {
        val authStateListener = { firebaseAuth: com.google.firebase.auth.FirebaseAuth ->
            isAuthenticated = firebaseAuth.currentUser != null
        }
        
        // İlk durumu kontrol et
        authStateListener(auth)
        
        // Auth state değişikliklerini dinle
        auth.addAuthStateListener(authStateListener)
    }
    
    // Logout fonksiyonu
    val logout = {
        auth.signOut()
        isAuthenticated = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        when (isAuthenticated) {
            null -> {
                // Loading state
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            false -> {
                // Not authenticated - show login/signup flow
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoggedIn = {
                                isAuthenticated = true
                            },
                            onSignUpClick = {
                                navController.navigate("signup")
                            }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            onSignedUp = {
                                navController.navigate("login") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            },
                            onBackToSignIn = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
            true -> {
                // Authenticated - show main app
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            onLogout = logout,
                            onNotificationClick = {
                                navController.navigate("notifications")
                            }
                        )
                    }

                    composable("reports") {
                        ReportsScreen()
                    }

                    composable("notifications") {
                        NotificationScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("profile") {
                        /* TODO: ProfileSettingsScreen() */
                    }

                    composable("input") {
                        InputScreen(onSubmit = { symptom ->
                            val encoded = java.net.URLEncoder.encode(symptom, "utf-8")
                            navController.navigate("result/$encoded")
                        })
                    }
                }
            }
        }
    }
}

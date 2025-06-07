package com.teduniversity.medicalai.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teduniversity.medicalai.ui.screens.ChatScreenWithViewModel
import com.teduniversity.medicalai.ui.screens.HomeScreen
import com.teduniversity.medicalai.ui.screens.InputScreen
import com.teduniversity.medicalai.ui.screens.LoginScreen
import com.teduniversity.medicalai.ui.screens.ReportsScreen
import com.teduniversity.medicalai.ui.screens.SignUpScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            // 1) Login
            composable("login") {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate("signup")
                    }
                )
            }

            // 2) SignUp
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

            // 3) Home
            composable("home") {
                HomeScreen(
                    onNotificationClick = {
                        navController.navigate("notifications")
                    },
                    onNewChatClick = {
                        navController.navigate("chat")
                    },
                    onReportClick = {
                        navController.navigate("reports")
                    },
                    onProfileClick = {
                        navController.navigate("profile")
                    },
                    onOpenChatHistory = { _chatItem ->
                        // navController.navigate("chat/${_chatItem.id}")
                    }
                )
            }

            // 4) Chat (ViewModel destekli)
            composable("chat") {
                ChatScreenWithViewModel(
                    onBack = { navController.popBackStack() }
                )
            }

            // 5) Reports Screen
            composable("reports") {
                ReportsScreen()
            }

            // 6) Notifications Screen (henüz yoksa placeholder)
            composable("notifications") {
                /* TODO: NotificationScreen() */
            }

            // 7) Profile/Settings Screen (henüz yoksa placeholder)
            composable("profile") {
                /* TODO: ProfileSettingsScreen() */
            }

            // 8) Input Screen
            composable("input") {
                InputScreen(onSubmit = { symptom ->
                    val encoded = java.net.URLEncoder.encode(symptom, "utf-8")
                    navController.navigate("result/$encoded")
                })
            }

            // … gerekirse result/{symptom} vb. diğer rotalar
        }
    }
}

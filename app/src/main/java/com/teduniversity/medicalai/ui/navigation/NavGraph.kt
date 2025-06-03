package com.teduniversity.medicalai.ui.navigation

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

    NavHost(navController, startDestination = "login") {

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
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNewChatClick = {
                    navController.navigate("chat")
                },
                onReportClick = {
                    navController.navigate("reports")
                },
                onOpenChatHistory = { chatItem ->
                    // Eğer geçmişten bir sohbeti açmak isterseniz
                    // örn: navController.navigate("chat/${chatItem.id}")
                }
            )
        }

        // 4) Chat (ViewModel destekli)
        composable("chat") {
            ChatScreenWithViewModel(
                onBack = { navController.popBackStack() }
            )
        }

        // 5) Input (veya diğer) ekranlar…
        composable("input") {
            InputScreen(onSubmit = { symptom ->
                val encoded = java.net.URLEncoder.encode(symptom, "utf-8")
                navController.navigate("result/$encoded")
            })
        }

        // Reports Screen
        composable("reports") {
            ReportsScreen()
        }

        // … sonuç ekranları vs.
    }
}

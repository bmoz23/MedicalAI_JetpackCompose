package com.teduniversity.medicalai.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teduniversity.medicalai.ui.theme.*
import com.teduniversity.medicalai.viewmodel.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Create ReportsViewModel instance to share between screens
    val reportsViewModel: ReportsViewModel = viewModel()

    // Chat ekranında bottom bar gizlenmeli
    if (currentRoute == "chat") {
        // Chat için ayrı scaffold (bottom bar olmadan)
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") {
                HomeScreen(
                    onNotificationClick = { /* TODO */ },
                    onNewChatClick = { 
                        navController.navigate("chat")
                    },
                    onReportClick = {
                        navController.navigate("reports") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    onProfileClick = {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    onOpenChatHistory = { /* TODO */ }
                )
            }
            
            composable("chat") {
                ChatScreenWithViewModel(
                    onBack = { navController.popBackStack() },
                    onReportCreated = {
                        // Refresh reports when a new report is created
                        Log.d("MainScreen", "onReportCreated callback triggered - refreshing reports")
                        reportsViewModel.loadReports()
                    }
                )
            }
            
            composable("reports") {
                ReportsScreen(viewModel = reportsViewModel)
            }
            
            composable("profile") {
                ProfileScreen(onLogout = onLogout)
            }
        }
    } else {
        // Ana ekranlar için bottom bar ile scaffold
        Scaffold(
            bottomBar = {
                CustomBottomBar(
                    selectedIndex = when (currentRoute) {
                        "home" -> 0
                        "reports" -> 1
                        "profile" -> 2
                        else -> 0
                    },
                    onItemSelected = { index ->
                        val destination = when (index) {
                            0 -> "home"
                            1 -> "reports"
                            2 -> "profile"
                            else -> "home"
                        }
                        navController.navigate(destination) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            },
            containerColor = AppBackground
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(
                        onNotificationClick = { /* TODO */ },
                        onNewChatClick = { 
                            navController.navigate("chat")
                        },
                        onReportClick = {
                            navController.navigate("reports") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        onProfileClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        onOpenChatHistory = { /* TODO */ }
                    )
                }
                
                composable("chat") {
                    ChatScreenWithViewModel(
                        onBack = { navController.popBackStack() },
                        onReportCreated = {
                            // Refresh reports when a new report is created
                            Log.d("MainScreen", "onReportCreated callback triggered - refreshing reports")
                            reportsViewModel.loadReports()
                        }
                    )
                }
                
                composable("reports") {
                    ReportsScreen(viewModel = reportsViewModel)
                }
                
                composable("profile") {
                    ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    // Home, Reports, Profile
    val items = listOf(
        BottomBarItem(Icons.Default.Home, "Home"),
        BottomBarItem(Icons.Default.Description, "Reports"),  
        BottomBarItem(Icons.Default.Person, "Profile")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(32.dp),
                color = PrimaryContainer,   // mavi container rengi
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        
                        IconButton(
                            onClick = { onItemSelected(index) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) BrandPrimaryBlue else OnPrimaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomBarItem(
    val icon: ImageVector,
    val label: String
) 
package com.teduniversity.medicalai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.teduniversity.medicalai.ui.navigation.AppNavHost
import com.teduniversity.medicalai.ui.theme.MedicalAITheme
import com.google.firebase.FirebaseApp


@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Edge-to-Edge modunu etkinleştiriyoruz:
        //    Bu satır, Compose'un içerik çizimini sistem çubuklarının (status/navigation) altına kadar uzatır.
        //    Eğer bunu koymazsak Compose içeriği çubukların altına çizilmez.
        enableEdgeToEdge()

        // 2) Firebase’in initialize edilmesi:
        FirebaseApp.initializeApp(this)

        // 3) setContent içinde tema ve NavHost’u yükle:
        setContent {
            MedicalAITheme(dynamicColor = false) {
                AppNavHost()
            }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MedicalAITheme {
       AppNavHost()
    }
}
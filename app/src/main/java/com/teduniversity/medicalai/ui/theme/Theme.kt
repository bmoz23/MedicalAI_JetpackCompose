package com.teduniversity.medicalai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color


private val LightColors = lightColorScheme(
    primary            = BrandPrimaryBlue,
    onPrimary          = BrandOnPrimaryBlue,
    primaryContainer   = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary          = BrandSecondaryGreen,
    onSecondary        = BrandOnSecondaryGreen,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    background         = AppBackground,
    onBackground       = AppOnBackground,

    surface            = AppSurface,
    onSurface          = AppOnSurface,

    outline            = AppOutline,

    error              = ErrorRed,
    onError            = OnErrorRed
)

/*
private val DarkColors = darkColorScheme(
    primary            = BrandPrimaryBlue,
    onPrimary          = BrandOnPrimaryBlue,
    primaryContainer   = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary          = BrandSecondaryGreen,
    onSecondary        = BrandOnSecondaryGreen,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    background         = Color(0xFF121212),
    onBackground       = Color(0xFFFFFFFF),

    surface            = Color(0xFF1E1E1E),
    onSurface          = Color(0xFFFFFFFF),

    outline            = AppOutline,

    error              = ErrorRed,
    onError            = OnErrorRed
)
*/

@Composable
fun MedicalAITheme(
    dynamicColor: Boolean = false,   // biz her zaman sabit paleti tercih ediyoruz
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography,
        content     = content
    )
}

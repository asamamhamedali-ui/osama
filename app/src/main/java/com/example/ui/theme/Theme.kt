package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = PetrolDarkNavy,
    onPrimary = Color.White,
    primaryContainer = PetrolMidNavy,
    onPrimaryContainer = Color.White,
    secondary = GoldenDomePrimary,
    onSecondary = Color.Black,
    secondaryContainer = GoldenBellContainer,
    onSecondaryContainer = DarkNavyText,
    tertiary = GoldenDomeHighlight,
    background = SoftAppBackground,
    onBackground = DarkNavyText,
    surface = SurfaceCardWhite,
    onSurface = DarkNavyText,
    surfaceVariant = SoftAppBackground,
    onSurfaceVariant = MutedNavyText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}

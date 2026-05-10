package com.example.mymy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreen,
    onPrimaryContainer = DarkText,
    
    secondary = Beige,
    onSecondary = DarkText,
    secondaryContainer = PaleOrange,
    onSecondaryContainer = DarkText,
    
    background = BackgroundColor,
    onBackground = DarkText,
    
    surface = SurfaceColor,
    onSurface = DarkText,
    surfaceVariant = PaleOrange,
    onSurfaceVariant = LightText,
    
    error = ErrorColor,
    onError = Color.White
)

@Composable
fun MymyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.standby.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StandByColors = darkColorScheme(
    primary = Color(0xFFF4F1EA),
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color(0xFFF4F1EA),
    surface = Color(0xFF0B0B0B),
    onSurface = Color(0xFFF4F1EA),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFB9B6AF),
)

@Composable
fun StandByTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = StandByColors, content = content)
}

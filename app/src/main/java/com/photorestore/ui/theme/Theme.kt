package com.photorestore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(primary = Color(0xFF1565C0), onPrimary = Color.White, primaryContainer = Color(0xFF1E88E5), onPrimaryContainer = Color.White, secondary = Color(0xFFFFA000), onSecondary = Color.Black, secondaryContainer = Color(0xFFFFB300), onSecondaryContainer = Color.Black, background = Color(0xFFFAFAFA), onBackground = Color(0xFF212121), surface = Color.White, onSurface = Color(0xFF212121), surfaceVariant = Color(0xFFF5F5F5), onSurfaceVariant = Color(0xFF757575), error = Color(0xFFB00020), onError = Color.White)
private val DarkColorScheme = darkColorScheme(primary = Color(0xFF1E88E5), onPrimary = Color.White, primaryContainer = Color(0xFF1565C0), onPrimaryContainer = Color.White, secondary = Color(0xFFFFB300), onSecondary = Color.Black, secondaryContainer = Color(0xFFFFA000), onSecondaryContainer = Color.Black, background = Color(0xFF121212), onBackground = Color.White, surface = Color(0xFF1E1E1E), onSurface = Color.White, surfaceVariant = Color(0xFF2C2C2C), onSurfaceVariant = Color(0xFFB0B0B0), error = Color(0xFFCF6679), onError = Color.Black)

@Composable
fun PhotoRestoreTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}

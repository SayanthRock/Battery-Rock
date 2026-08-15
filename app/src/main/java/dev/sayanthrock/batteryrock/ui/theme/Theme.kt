package dev.sayanthrock.batteryrock.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Diagnostic Console Palette
private val BatteryRockColors = lightColorScheme(
    primary = Color(0xFF1B6A20), // Phosphor Amber
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA5F4A2),
    onPrimaryContainer = Color(0xFF002203),
    secondary = Color(0xFF53634F), // Cyan
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E8CE),
    onSecondaryContainer = Color(0xFF111F0F),
    tertiary = Color(0xFF1E1E1E), // Off-white text
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF6F8F2), // Graphite background
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFF0F3EB), // Slightly lighter graphite for cards
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDFE4D8),
    onSurfaceVariant = Color(0xFF43483F), // Muted text
    outline = Color(0xFF73796E), // Hairline dividers
    outlineVariant = Color(0xFFC3C8BC),
    error = Color(0xFFD32F2F), // Muted red for warnings
    onError = Color(0xFFFFFFFF),
)

@Composable
fun BatteryRockTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = BatteryRockColors,
        content = content,
    )
}

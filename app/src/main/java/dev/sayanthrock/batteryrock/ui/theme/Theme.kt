package dev.sayanthrock.batteryrock.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Diagnostic Console Palette
private val BatteryRockColors = darkColorScheme(
    primary = Color(0xFFFFB627), // Phosphor Amber
    onPrimary = Color(0xFF0E1013),
    primaryContainer = Color(0xFF2D2312),
    onPrimaryContainer = Color(0xFFFFE0A3),
    secondary = Color(0xFF4DE0FF), // Cyan
    onSecondary = Color(0xFF04191C),
    secondaryContainer = Color(0xFF0B333B),
    onSecondaryContainer = Color(0xFFB8F3FF),
    tertiary = Color(0xFFE8E6E1), // Off-white text
    onTertiary = Color(0xFF0E1013),
    background = Color(0xFF0E1013), // Graphite background
    onBackground = Color(0xFFE8E6E1),
    surface = Color(0xFF16191D), // Slightly lighter graphite for cards
    onSurface = Color(0xFFE8E6E1),
    surfaceVariant = Color(0xFF1F2329),
    onSurfaceVariant = Color(0xFFA9A9A9), // Muted text
    outline = Color(0xFF333840), // Hairline dividers
    outlineVariant = Color(0xFF22262B),
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = BatteryRockColors,
        content = content,
    )
}

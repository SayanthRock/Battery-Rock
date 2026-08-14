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

private val BatteryRockColors = darkColorScheme(
    primary = Color(0xFFA142FF),
    onPrimary = Color(0xFF05060A),
    primaryContainer = Color(0xFF20234A),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF00E5FF),
    onSecondary = Color(0xFF02131F),
    secondaryContainer = Color(0xFF0B2C3F),
    tertiary = Color(0xFF22C55E),
    onTertiary = Color(0xFF03140A),
    background = Color(0xFF0D0B14),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0D0B14),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF111827),
    onSurfaceVariant = Color(0xFFA7B0C0),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFEF4444),
    onError = Color(0xFF1A0505),
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

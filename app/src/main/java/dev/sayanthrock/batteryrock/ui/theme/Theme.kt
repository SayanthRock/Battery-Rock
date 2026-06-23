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

// ─── SayanthRock 2026 Design Language ────────────────────────────────────────
// Near-black bg · Indigo-300 primary · Glass tints at 5-10% opacity

private val BatteryRockColors = darkColorScheme(
    primary          = Color(0xFF818CF8),  // indigo-300
    onPrimary        = Color(0xFF0A0A0A),
    primaryContainer = Color(0xFF1A1A3E),
    secondary        = Color(0xFF22C55E),  // green-500 (active state)
    onSecondary      = Color(0xFF0A0A0A),
    background       = Color(0xFF0A0A0A),
    onBackground     = Color(0xFFE5E7EB),
    surface          = Color(0xFF111111),
    onSurface        = Color(0xFFD1D5DB),
    surfaceVariant   = Color(0xFF1C1C2E),
    outline          = Color(0xFF374151),
    error            = Color(0xFFEF4444),
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
        }
    }

    MaterialTheme(
        colorScheme = BatteryRockColors,
        content = content
    )
}

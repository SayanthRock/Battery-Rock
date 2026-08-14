package dev.sayanthrock.batteryrock.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.sayanthrock.batteryrock.BatteryRockConfigStore

private val BatteryRockDarkColors = darkColorScheme(
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
    surface = Color(0xFF0D0B14), // Matches CardBackground via custom theme if needed
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF111827),
    onSurfaceVariant = Color(0xFFA7B0C0),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFEF4444),
    onError = Color(0xFF1A0505),
)

private val BatteryRockLightColors = lightColorScheme(
    primary = Color(0xFFA142FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF20234A),
    secondary = Color(0xFF00E5FF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB9F2FF),
    tertiary = Color(0xFF22C55E),
    onTertiary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

// AMOLED Theme (True Black)
private val BatteryRockAmoledColors = BatteryRockDarkColors.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF090909) // Slightly off-black for gradient
)

// AMOLED Dynamic Theme (Deep Dark Tint)
private val BatteryRockAmoledDynamicColors = BatteryRockDarkColors.copy(
    background = Color(0xFF050505),
    surface = Color(0xFF050505),
    surfaceVariant = Color(0xFF0A0A0A)
)

@Composable
fun BatteryRockTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current

    // Instead of using Flow for immediate sync updates across screens within standard Compose paradigm
    // we use a remembered state derived from reading. For a full reactive approach across the app,
    // we'd typically use a DataStore or StateFlow, but for now we read it upon composition.
    // However, to make it react immediately, we can use a key or observe a shared state if available.
    // For simplicity, we'll read it here. If the user changes it in Settings, it will recompose there.

    // A simple hack to force recomposition is to pass the themeMode as a key or parameter, but standard Jetpack
    // Compose with SharedPreferences often requires a Flow or State wrapper.
    // We will read it directly for now.
    val currentConfig = BatteryRockConfigStore.read(context)
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when (currentConfig.themeMode) {
        "Light mode" -> BatteryRockLightColors
        "Dark mode" -> BatteryRockDarkColors
        "AMOLED" -> BatteryRockAmoledColors
        "AMOLED Dynamic" -> BatteryRockAmoledDynamicColors
        else -> if (isSystemDark) BatteryRockDarkColors else BatteryRockLightColors
    }

    // Determine status bar icon color based on theme
    val useDarkIcons = when (currentConfig.themeMode) {
        "Light mode" -> true
        "Dark mode", "AMOLED", "AMOLED Dynamic" -> false
        else -> !isSystemDark
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkIcons
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = useDarkIcons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

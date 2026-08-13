package dev.sayanthrock.batteryrock.widget

import androidx.compose.ui.graphics.Color

object WidgetTheme {
    val Background = Color(0xFF0F111A)
    val Surface = Color(0xFF161925)
    val SurfaceVariant = Color(0xFF1E2233)
    val PrimaryText = Color(0xFFFFFFFF)
    val SecondaryText = Color(0xFF94A3B8)
    val MutedText = Color(0xFF64748B)

    val AccentGreen = Color(0xFF22C55E)
    val AccentAmber = Color(0xFFF59E0B)
    val AccentRed = Color(0xFFEF4444)
    val AccentCyan = Color(0xFF06B6D4)
    val AccentPurple = Color(0xFF8B5CF6)

    val CardBorder = Color(0x33FFFFFF)

    fun getBatteryColor(percentage: Int): Color {
        return when {
            percentage <= 15 -> AccentRed
            percentage <= 30 -> AccentAmber
            else -> AccentGreen
        }
    }
}

package dev.sayanthrock.batteryrock.widget

import android.content.ComponentName
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import dev.sayanthrock.batteryrock.MainActivity

@Composable
fun WidgetCard(modifier: GlanceModifier = GlanceModifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WidgetTheme.Surface)
            .cornerRadius(24.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun LinearProgressBar(
    percentage: Int,
    modifier: GlanceModifier = GlanceModifier,
    isCharging: Boolean = false
) {
    val progressColor = if (isCharging) WidgetTheme.AccentGreen else WidgetTheme.getBatteryColor(percentage)
    val safePercentage = percentage.coerceIn(0, 100)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(WidgetTheme.SurfaceVariant)
            .cornerRadius(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Using nested rows/boxes to simulate a weighted progress bar
        Row(modifier = GlanceModifier.fillMaxWidth().fillMaxHeight()) {
            if (safePercentage > 0) {
                // Not a real weighted progress bar, but a placeholder that compiles with Glance
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .background(progressColor)
                        .cornerRadius(3.dp)
                ) {}
            }
        }
    }
}

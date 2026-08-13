package dev.sayanthrock.batteryrock.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.ActionModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(WidgetTheme.SurfaceVariant)
            .cornerRadius(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth(percentage / 100f)
                .fillMaxHeight()
                .background(progressColor)
                .cornerRadius(3.dp)
        ) {}
    }
}

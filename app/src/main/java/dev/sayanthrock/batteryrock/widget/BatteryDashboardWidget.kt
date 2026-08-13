package dev.sayanthrock.batteryrock.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class BatteryDashboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryDashboardWidget()
}

class BatteryDashboardWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetCard {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "78%", // Placeholder
                            style = TextStyle(
                                color = ColorProvider(day = WidgetTheme.PrimaryText, night = WidgetTheme.PrimaryText),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    LinearProgressBar(percentage = 78, isCharging = true)
                    Spacer(modifier = GlanceModifier.height(16.dp))
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = "Charging",
                                style = TextStyle(color = ColorProvider(day = WidgetTheme.AccentGreen, night = WidgetTheme.AccentGreen), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = "Fast charging",
                                style = TextStyle(color = ColorProvider(day = WidgetTheme.PrimaryText, night = WidgetTheme.PrimaryText), fontSize = 14.sp)
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = "18.6 W",
                                style = TextStyle(color = ColorProvider(day = WidgetTheme.SecondaryText, night = WidgetTheme.SecondaryText), fontSize = 14.sp)
                            )
                        }
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = "Status",
                                style = TextStyle(color = ColorProvider(day = WidgetTheme.SecondaryText, night = WidgetTheme.SecondaryText), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = "31°C",
                                style = TextStyle(color = ColorProvider(day = WidgetTheme.PrimaryText, night = WidgetTheme.PrimaryText), fontSize = 14.sp)
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = "4.42 V · 2.8 A",
                                style = TextStyle(color = ColorProvider(day = WidgetTheme.SecondaryText, night = WidgetTheme.SecondaryText), fontSize = 14.sp)
                            )
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(16.dp))
                    Text(
                        text = "ETA: ~46 min to 100%", // Placeholder
                        style = TextStyle(
                            color = ColorProvider(day = WidgetTheme.SecondaryText, night = WidgetTheme.SecondaryText),
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

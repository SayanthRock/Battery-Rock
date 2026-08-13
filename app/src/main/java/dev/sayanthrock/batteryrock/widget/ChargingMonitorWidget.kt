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

class ChargingMonitorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ChargingMonitorWidget()
}

class ChargingMonitorWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetCard {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Charging",
                        style = TextStyle(
                            color = ColorProvider(WidgetTheme.AccentGreen),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "62% → 100%", // Placeholder
                        style = TextStyle(
                            color = ColorProvider(WidgetTheme.PrimaryText),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "18.6 W · Fast charging", // Placeholder
                        style = TextStyle(
                            color = ColorProvider(WidgetTheme.SecondaryText),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "Started at 21:34", // Placeholder
                        style = TextStyle(
                            color = ColorProvider(WidgetTheme.MutedText),
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Elapsed 38 min · ~46 min remaining", // Placeholder
                        style = TextStyle(
                            color = ColorProvider(WidgetTheme.SecondaryText),
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

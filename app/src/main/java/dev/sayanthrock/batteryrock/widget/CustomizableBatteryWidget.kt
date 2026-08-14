package dev.sayanthrock.batteryrock.widget
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF


import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.LocalSize
import androidx.glance.GlanceModifier
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.ImageProvider
import androidx.glance.Image
import androidx.glance.layout.size
import androidx.glance.text.FontWeight

class CustomizableBatteryWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = (id as? androidx.glance.appwidget.AppWidgetId)?.appWidgetId ?: 0
        val config = WidgetConfigStore.getConfig(context, appWidgetId)

        provideContent {
            val size = LocalSize.current

            WidgetCard {
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(config.internalPaddingDp.dp),
                    contentAlignment = getAlignment(config)
                ) {
                    when (config.preset) {
                        LayoutPreset.COMPACT -> CompactLayout(config, size)
                        LayoutPreset.CLOCK_BATTERY -> ClockBatteryLayout(config, size)
                        LayoutPreset.LARGE_CIRCULAR -> LargeCircularLayout(config, size)
                        LayoutPreset.WIDE -> WideLayout(config, size)
                        LayoutPreset.MINIMAL -> MinimalLayout(config, size)
                        LayoutPreset.CHARGING_DASHBOARD -> ChargingDashboardLayout(config, size)
                    }
                }
            }
        }
    }

    private fun getAlignment(config: WidgetConfig): Alignment {
        val horizontal = when (config.horizontalAlignment) {
            0 -> Alignment.Horizontal.Start
            2 -> Alignment.Horizontal.End
            else -> Alignment.Horizontal.CenterHorizontally
        }
        val vertical = when (config.verticalAlignment) {
            0 -> Alignment.Vertical.Top
            2 -> Alignment.Vertical.Bottom
            else -> Alignment.Vertical.CenterVertically
        }
        return Alignment(horizontal, vertical)
    }
}






@androidx.compose.runtime.Composable
fun CompactLayout(config: WidgetConfig, size: DpSize) {
    val bitmap = BatteryRingGenerator.generateRingBitmap(config, 85) // placeholder percentage
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (config.ringEnabled) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Battery Ring",
                modifier = GlanceModifier.size(config.ringSizeDp.dp)
            )
            Spacer(modifier = GlanceModifier.height(config.elementSpacingDp.dp))
        }
        if (config.showPercentage) {
            Text(
                text = "85%",
                style = TextStyle(
                    color = ColorProvider(day = Color(config.textColor), night = Color(config.textColor)),
                    fontSize = config.percentageSizeSp.sp,
                    fontWeight = if (config.fontWeightBold) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
        if (config.showTemperature) {
            Text(
                text = "34.2°C",
                style = TextStyle(
                    color = ColorProvider(day = Color(config.textColor).copy(alpha = 0.7f), night = Color(config.textColor).copy(alpha = 0.7f)),
                    fontSize = config.temperatureSizeSp.sp
                )
            )
        }
    }
}

@androidx.compose.runtime.Composable
fun ClockBatteryLayout(config: WidgetConfig, size: DpSize) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                text = "6:15 am",
                style = TextStyle(
                    color = ColorProvider(day = Color(config.textColor), night = Color(config.textColor)),
                    fontSize = config.clockSizeSp.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Fri, August 14",
                style = TextStyle(
                    color = ColorProvider(day = Color(config.textColor).copy(alpha = 0.7f), night = Color(config.textColor).copy(alpha = 0.7f)),
                    fontSize = config.dateSizeSp.sp
                )
            )
        }
        Spacer(modifier = GlanceModifier.width(16.dp))

        val bitmap = BatteryRingGenerator.generateRingBitmap(config, 83)
        Box(contentAlignment = Alignment.Center) {
            if (config.ringEnabled) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = "Battery Ring",
                    modifier = GlanceModifier.size(config.ringSizeDp.dp)
                )
            }
            if (config.showPercentage) {
                Text(
                    text = "83%",
                    style = TextStyle(
                        color = ColorProvider(day = Color(config.textColor), night = Color(config.textColor)),
                        fontSize = config.percentageSizeSp.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun LargeCircularLayout(config: WidgetConfig, size: DpSize) {
    val bitmap = BatteryRingGenerator.generateRingBitmap(config, 83)
    Box(contentAlignment = Alignment.Center) {
        if (config.ringEnabled) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Battery Ring",
                modifier = GlanceModifier.size((config.ringSizeDp * 2).dp)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (config.showPercentage) {
                Text(
                    text = "83%",
                    style = TextStyle(
                        color = ColorProvider(day = Color(config.textColor), night = Color(config.textColor)),
                        fontSize = (config.percentageSizeSp * 1.5f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            if (config.showTemperature) {
                Text(
                    text = "34.2°C",
                    style = TextStyle(
                        color = ColorProvider(day = Color(config.textColor).copy(alpha = 0.7f), night = Color(config.textColor).copy(alpha = 0.7f)),
                        fontSize = config.temperatureSizeSp.sp
                    )
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun WideLayout(config: WidgetConfig, size: DpSize) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val bitmap = BatteryRingGenerator.generateRingBitmap(config, 85)
        if (config.ringEnabled) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Battery Ring",
                modifier = GlanceModifier.size(config.ringSizeDp.dp)
            )
            Spacer(modifier = GlanceModifier.width(config.elementSpacingDp.dp))
        }
        Column {
            if (config.showPercentage) {
                Text(
                    text = "85%",
                    style = TextStyle(
                        color = ColorProvider(day = Color(config.textColor), night = Color(config.textColor)),
                        fontSize = config.percentageSizeSp.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            if (config.showTemperature) {
                Text(
                    text = "34.2°C",
                    style = TextStyle(
                        color = ColorProvider(day = Color(config.textColor).copy(alpha = 0.7f), night = Color(config.textColor).copy(alpha = 0.7f)),
                        fontSize = config.temperatureSizeSp.sp
                    )
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun MinimalLayout(config: WidgetConfig, size: DpSize) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (config.showPercentage) {
            Text(
                text = "85%",
                style = TextStyle(
                    color = ColorProvider(day = Color(config.textColor), night = Color(config.textColor)),
                    fontSize = config.percentageSizeSp.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        if (config.showPercentage && config.showTemperature) {
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(text = "•", style = TextStyle(color = ColorProvider(day = Color(config.textColor).copy(alpha = 0.5f), night = Color(config.textColor).copy(alpha = 0.5f))))
            Spacer(modifier = GlanceModifier.width(8.dp))
        }
        if (config.showTemperature) {
            Text(
                text = "34.2°C",
                style = TextStyle(
                    color = ColorProvider(day = Color(config.textColor).copy(alpha = 0.7f), night = Color(config.textColor).copy(alpha = 0.7f)),
                    fontSize = config.temperatureSizeSp.sp
                )
            )
        }
    }
}

@androidx.compose.runtime.Composable
fun ChargingDashboardLayout(config: WidgetConfig, size: DpSize) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "83%",
                style = TextStyle(
                    color = ColorProvider(day = Color(config.textColor), night = Color(config.textColor)),
                    fontSize = config.percentageSizeSp.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(16.dp))
            Text(
                text = "Charging",
                style = TextStyle(
                    color = ColorProvider(day = Color(config.chargingColor), night = Color(config.chargingColor)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "Fast charging · 42 min until full",
            style = TextStyle(
                color = ColorProvider(day = Color(config.textColor).copy(alpha = 0.7f), night = Color(config.textColor).copy(alpha = 0.7f)),
                fontSize = 14.sp
            )
        )
    }
}

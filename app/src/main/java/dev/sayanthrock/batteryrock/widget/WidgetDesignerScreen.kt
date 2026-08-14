package dev.sayanthrock.batteryrock.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import dev.sayanthrock.batteryrock.ui.theme.BatteryRockTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetDesignerScreen(
    initialConfig: WidgetConfig,
    onSave: (WidgetConfig) -> Unit,
    onCancel: () -> Unit
) {
    var config by remember { mutableStateOf(initialConfig) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Designer") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(config) }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Live Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF0F111A)) // Dark background to simulate home screen
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                WidgetLivePreview(config)
            }

            Divider()

            // Configuration Controls
            Text(
                "Presets",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Simple preset selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LayoutPreset.values().forEach { preset ->
                    Button(
                        onClick = { config = config.copy(preset = preset) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (config.preset == preset) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (config.preset == preset) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).padding(2.dp)
                    ) {
                        Text(preset.name.take(3), fontSize = 10.sp, maxLines = 1)
                    }
                }
            }

            // Data Visibility Toggles
            Text(
                "Data Visibility",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            SwitchRow("Show Percentage", config.showPercentage) { config = config.copy(showPercentage = it) }
            SwitchRow("Show Temperature", config.showTemperature) { config = config.copy(showTemperature = it) }
            SwitchRow("Show Charging Status", config.showChargingStatus) { config = config.copy(showChargingStatus = it) }

            // Battery Ring Customization
            Text(
                "Battery Ring",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            SwitchRow("Enable Ring", config.ringEnabled) { config = config.copy(ringEnabled = it) }

            if (config.ringEnabled) {
                SliderRow("Size", config.ringSizeDp, 32f, 128f) { config = config.copy(ringSizeDp = it) }
                SliderRow("Thickness", config.ringThicknessDp, 2f, 24f) { config = config.copy(ringThicknessDp = it) }
            }

            // Typography Customization
            Text(
                "Typography",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            SliderRow("Percentage Size", config.percentageSizeSp, 10f, 64f) { config = config.copy(percentageSizeSp = it) }
            SliderRow("Temperature Size", config.temperatureSizeSp, 10f, 48f) { config = config.copy(temperatureSizeSp = it) }
            SliderRow("Clock Size", config.clockSizeSp, 12f, 64f) { config = config.copy(clockSizeSp = it) }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SliderRow(label: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text("${value.toInt()}")
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max
        )
    }
}

@Composable
fun WidgetLivePreview(config: WidgetConfig) {
    Box(
        modifier = Modifier
            .background(
                Color(config.backgroundColor).copy(alpha = config.backgroundOpacity),
                RoundedCornerShape(config.cornerRadiusDp.dp)
            )
            .padding(config.internalPaddingDp.dp)
    ) {
        when (config.preset) {
            LayoutPreset.COMPACT -> ComposeCompactLayout(config)
            LayoutPreset.CLOCK_BATTERY -> ComposeClockBatteryLayout(config)
            LayoutPreset.LARGE_CIRCULAR -> ComposeLargeCircularLayout(config)
            LayoutPreset.WIDE -> ComposeWideLayout(config)
            LayoutPreset.MINIMAL -> ComposeMinimalLayout(config)
            LayoutPreset.CHARGING_DASHBOARD -> ComposeChargingDashboardLayout(config)
        }
    }
}

@Composable
fun ComposeCompactLayout(config: WidgetConfig) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (config.ringEnabled) {
            ComposeBatteryRing(config, percentage = 85)
            Spacer(modifier = Modifier.height(config.elementSpacingDp.dp))
        }
        if (config.showPercentage) {
            Text(
                "85%",
                color = Color(config.textColor),
                fontSize = config.percentageSizeSp.sp,
                fontWeight = if (config.fontWeightBold) FontWeight.Bold else FontWeight.Normal
            )
        }
        if (config.showTemperature) {
            Text(
                "34.2°C",
                color = Color(config.textColor).copy(alpha = 0.7f),
                fontSize = config.temperatureSizeSp.sp
            )
        }
    }
}

@Composable
fun ComposeClockBatteryLayout(config: WidgetConfig) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                "6:15 am",
                color = Color(config.textColor),
                fontSize = config.clockSizeSp.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Fri, August 14",
                color = Color(config.textColor).copy(alpha = 0.7f),
                fontSize = config.dateSizeSp.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Box(contentAlignment = Alignment.Center) {
            if (config.ringEnabled) {
                ComposeBatteryRing(config, percentage = 83)
            }
            if (config.showPercentage) {
                Text(
                    "83%",
                    color = Color(config.textColor),
                    fontSize = config.percentageSizeSp.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ComposeLargeCircularLayout(config: WidgetConfig) {
    Box(contentAlignment = Alignment.Center) {
        if (config.ringEnabled) {
            ComposeBatteryRing(config, percentage = 83, scale = 2f)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (config.showPercentage) {
                Text(
                    "83%",
                    color = Color(config.textColor),
                    fontSize = (config.percentageSizeSp * 1.5f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (config.showTemperature) {
                Text(
                    "34.2°C",
                    color = Color(config.textColor).copy(alpha = 0.7f),
                    fontSize = config.temperatureSizeSp.sp
                )
            }
        }
    }
}

@Composable
fun ComposeWideLayout(config: WidgetConfig) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (config.ringEnabled) {
            ComposeBatteryRing(config, percentage = 85)
            Spacer(modifier = Modifier.width(config.elementSpacingDp.dp))
        }
        Column {
            if (config.showPercentage) {
                Text(
                    "85%",
                    color = Color(config.textColor),
                    fontSize = config.percentageSizeSp.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (config.showTemperature) {
                Text(
                    "34.2°C",
                    color = Color(config.textColor).copy(alpha = 0.7f),
                    fontSize = config.temperatureSizeSp.sp
                )
            }
        }
    }
}

@Composable
fun ComposeMinimalLayout(config: WidgetConfig) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (config.showPercentage) {
            Text(
                "85%",
                color = Color(config.textColor),
                fontSize = config.percentageSizeSp.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (config.showPercentage && config.showTemperature) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("•", color = Color(config.textColor).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (config.showTemperature) {
            Text(
                "34.2°C",
                color = Color(config.textColor).copy(alpha = 0.7f),
                fontSize = config.temperatureSizeSp.sp
            )
        }
    }
}

@Composable
fun ComposeChargingDashboardLayout(config: WidgetConfig) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "83%",
                color = Color(config.textColor),
                fontSize = config.percentageSizeSp.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Charging",
                color = Color(config.chargingColor),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Fast charging · 42 min until full",
            color = Color(config.textColor).copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}

@Composable
fun ComposeBatteryRing(config: WidgetConfig, percentage: Int, scale: Float = 1f) {
    Canvas(
        modifier = Modifier.size((config.ringSizeDp * scale).dp)
    ) {
        val thickness = (config.ringThicknessDp * scale).dp.toPx()
        val padding = thickness / 2

        if (config.ringEnabled) {
            drawArc(
                color = Color(config.ringBackgroundColor),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = thickness)
            )
        }

        val sweepAngle = (percentage / 100f) * 360f
        val actualSweep = if (config.ringDirectionClockwise) sweepAngle else -sweepAngle
        val cap = if (config.ringRoundedCaps) StrokeCap.Round else StrokeCap.Butt

        drawArc(
            color = Color(config.ringProgressColor),
            startAngle = config.ringStartAngle,
            sweepAngle = actualSweep,
            useCenter = false,
            style = Stroke(width = thickness, cap = cap)
        )
    }
}

package dev.sayanthrock.batteryrock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sayanthrock.batteryrock.ui.theme.BatteryRockTheme

private val Midnight = Color(0xFF05060A)
private val Ink = Color(0xFF0B1020)
private val Glass = Color(0x12FFFFFF)
private val BorderSoft = Color(0x1FFFFFFF)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextSecondary = Color(0xFFA7B0C0)
private val TextMuted = Color(0xFF64748B)
private val Indigo = Color(0xFF818CF8)
private val Cyan = Color(0xFF38BDF8)
private val Green = Color(0xFF22C55E)
private val Amber = Color(0xFFF59E0B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryRockTheme {
                BatteryRockScreen(isActive = BatteryRockStatus.isModuleActive())
            }
        }
    }
}

@Composable
fun BatteryRockScreen(isActive: Boolean) {
    val context = LocalContext.current
    var refreshTick by remember { mutableStateOf(0) }
    var config by remember { mutableStateOf(BatteryRockConfigStore.read(context)) }
    val batteryHealth = remember(refreshTick) { DeviceStatusReader.readBatteryHealth(context) }
    val performanceLevel = remember(refreshTick) { DeviceStatusReader.readPerformanceLevel(context) }

    fun saveConfig(next: BatteryRockConfig) {
        BatteryRockConfigStore.save(context, next)
        config = next
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Midnight, Ink, Color(0xFF111827))))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 22.dp, bottom = 28.dp)
        ) {
            item { HeroCard(isActive) }
            item { QuickStatusStrip(batteryHealth, performanceLevel) }

            item { SectionLabel("Live dashboard") }
            item { DeviceDashboardCard(batteryHealth, performanceLevel) }
            item { OptimizationProfileCard(performanceLevel) }

            item { SectionLabel("Charging care") }
            item {
                ChargingCareCard(
                    batteryHealth = batteryHealth,
                    config = config,
                    onToggleCare = { saveConfig(config.copy(batteryCare80 = !config.batteryCare80)) },
                    onOpenBatterySettings = { openBatterySettings(context) },
                    onRefresh = { refreshTick++ }
                )
            }

            item { SectionLabel("Smart controls") }
            item {
                ModeSelectorCard(
                    badge = "BAT",
                    title = "Battery Backup",
                    subtitle = "Saved profile for daily battery behavior.",
                    selected = config.batteryMode,
                    options = listOf(
                        ModeOption("Safe", "Gentle daily settings"),
                        ModeOption("Balanced", "Best default profile"),
                        ModeOption("Advanced", "Stronger background guidance")
                    ),
                    onSelected = { saveConfig(config.copy(batteryMode = it)) }
                )
            }
            item {
                ModeSelectorCard(
                    badge = "CPU",
                    title = "Phone Performance",
                    subtitle = "Saved smoothness profile based on real device status.",
                    selected = config.performanceMode,
                    options = listOf(
                        ModeOption("Standard", "Stable everyday behavior"),
                        ModeOption("Smooth", "Better scrolling feel"),
                        ModeOption("Performance", "For heavier use")
                    ),
                    onSelected = { saveConfig(config.copy(performanceMode = it)) }
                )
            }
            item {
                ModeSelectorCard(
                    badge = "ROM",
                    title = "RAM / ROM Profile",
                    subtitle = "Saved RAM and storage profile with clear status feedback.",
                    selected = config.ramRomMode,
                    options = listOf(
                        ModeOption("Safe battery profile", "Low-RAM friendly mode"),
                        ModeOption("Clean storage + Safe profile", "Tight-storage recovery mode"),
                        ModeOption("Balanced daily profile", "Stable mode for normal use"),
                        ModeOption("Smooth balanced profile", "Good balance for modern phones"),
                        ModeOption("Performance profile", "For devices with healthy RAM and ROM headroom")
                    ),
                    onSelected = { saveConfig(config.copy(ramRomMode = it)) }
                )
            }
            item {
                ModeSelectorCard(
                    badge = "HZ",
                    title = "Refresh Rate",
                    subtitle = "Saved display preference for the selected profile.",
                    selected = config.refreshRateMode,
                    options = listOf(
                        ModeOption("Auto-select", "Let the phone choose"),
                        ModeOption("High", "Smoother animations"),
                        ModeOption("Standard", "Battery-first display mode")
                    ),
                    onSelected = { saveConfig(config.copy(refreshRateMode = it)) }
                )
            }

            item { SectionLabel("Improvement center") }
            items(IMPROVEMENT_ITEMS) { ImprovementCard(it) }

            item { SectionLabel("Supported brands") }
            items(SUPPORTED_BRANDS) { BrandChip(it) }

            item { FooterNote() }
        }
    }
}

@Composable
fun HeroCard(isActive: Boolean) {
    val activeColor = if (isActive) Green else Amber
    val activeLabel = if (isActive) "ACTIVE" else "READY"
    val subtitle = if (isActive) {
        "Module status bridge is active. Dashboard controls are available."
    } else {
        "Dashboard is safe to open. Use Battery settings for phone charging options."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF141A33), Color(0xFF0B1224), Color(0xFF101827))))
            .border(1.dp, Color(0x2FFFFFFF), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeCircle(text = "BR", color = Indigo)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Battery-Rock", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("Battery health · Charging care · RAM · ROM", color = TextSecondary, fontSize = 13.sp)
            }
            StatusBadge(label = activeLabel, color = activeColor)
        }
        Spacer(Modifier.height(18.dp))
        Text(text = subtitle, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TinyInfoCard("CARE", "80% option", Indigo, Modifier.weight(1f))
            TinyInfoCard("CHARGE", "Time estimate", Cyan, Modifier.weight(1f))
            TinyInfoCard("APK", BuildConfig.VERSION_NAME, Green, Modifier.weight(1f))
        }
    }
}

@Composable
fun QuickStatusStrip(batteryHealth: BatteryHealthSnapshot, performanceLevel: DevicePerformanceSnapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStatCard("Battery", if (batteryHealth.levelPercent >= 0) "${batteryHealth.levelPercent}%" else "--", Green, Modifier.weight(1f))
            MiniStatCard("Charging", batteryHealth.minutesToFullText(), Cyan, Modifier.weight(1f))
            MiniStatCard("Health", batteryHealth.healthLabel, Indigo, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStatCard("RAM", if (performanceLevel.ramLoadPercent > 0) "${performanceLevel.ramLoadPercent}%" else "--", Amber, Modifier.weight(1f))
            MiniStatCard("ROM", if (performanceLevel.storageUsedPercent > 0) "${performanceLevel.storageUsedPercent}%" else "--", Green, Modifier.weight(1f))
        }
    }
}

@Composable
fun DeviceDashboardCard(batteryHealth: BatteryHealthSnapshot, performanceLevel: DevicePerformanceSnapshot) {
    GlassCard {
        HeaderRow("LIVE", "Device status", "Battery, RAM, ROM and performance readings with safe fallback values.", Green)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Battery", if (batteryHealth.levelPercent >= 0) "${batteryHealth.levelPercent}%" else "Unknown", batteryHealth.statusLabel, Green, Modifier.weight(1f))
            MetricTile("Full charge", batteryHealth.minutesToFullText(), "Estimate shown while charging", Cyan, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Temp", batteryHealth.temperatureC, "${batteryHealth.powerSource} · ${batteryHealth.capacityEstimate}", Amber, Modifier.weight(1f))
            MetricTile("Performance", performanceLevel.levelLabel, "${performanceLevel.score}/100 · ${performanceLevel.cores} cores", Cyan, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("RAM", performanceLevel.ramPressureLabel, "${performanceLevel.availableRam} free · ${performanceLevel.totalRam} total", Indigo, Modifier.weight(1f))
            MetricTile("ROM", performanceLevel.storagePressureLabel, "${performanceLevel.storageFree} free · ${performanceLevel.storageTotal} total", Green, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Text(batteryHealth.summary, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
fun ChargingCareCard(
    batteryHealth: BatteryHealthSnapshot,
    config: BatteryRockConfig,
    onToggleCare: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onRefresh: () -> Unit,
) {
    GlassCard {
        HeaderRow(
            badge = "80",
            title = "80% Battery Care",
            subtitle = "Status: ${if (config.batteryCare80) "On" else "Off"} · ${batteryHealth.minutesToFullText()}",
            color = if (config.batteryCare80) Green else Amber
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Battery-Rock saves your 80% care choice and opens the real phone Battery settings. Actual charging control depends on the phone ROM option.",
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton(if (config.batteryCare80) "80% care ON" else "80% care OFF", if (config.batteryCare80) Green else Amber, Modifier.weight(1f), onToggleCare)
            ActionButton("Refresh", Cyan, Modifier.weight(1f), onRefresh)
        }
        Spacer(Modifier.height(10.dp))
        ActionButton("Open Battery settings", Indigo, Modifier.fillMaxWidth(), onOpenBatterySettings)
    }
}

@Composable
fun OptimizationProfileCard(performanceLevel: DevicePerformanceSnapshot) {
    GlassCard {
        HeaderRow("OPT", "Recommended profile", performanceLevel.recommendedProfile, Cyan)
        Spacer(Modifier.height(8.dp))
        Text("ROM: ${performanceLevel.romName}", color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        Text(performanceLevel.loadSummary(), color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

private fun DevicePerformanceSnapshot.loadSummary(): String {
    val ram = if (ramLoadPercent > 0) "RAM load $ramLoadPercent%" else "RAM load unknown"
    val rom = if (storageUsedPercent > 0) "ROM used $storageUsedPercent%" else "ROM used unknown"
    return "$ram · $rom"
}

private fun BatteryHealthSnapshot.minutesToFullText(): String = when {
    statusLabel == "Full" || levelPercent >= 100 -> "Full"
    statusLabel != "Charging" -> "Not charging"
    levelPercent < 0 -> "Unknown"
    else -> "${((100 - levelPercent).coerceIn(0, 100) * 2).coerceAtLeast(1)} min"
}

private fun openBatterySettings(context: Context) {
    runCatching {
        context.startActivity(Intent(Settings.ACTION_POWER_USAGE_SUMMARY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.recoverCatching {
        context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.recoverCatching {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Composable
fun HeaderRow(badge: String, title: String, subtitle: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        BadgeBox(text = badge, color = color)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
fun MetricTile(label: String, value: String, detail: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.11f))
            .border(1.dp, accent.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
            .padding(13.dp)
    ) {
        Text(label.uppercase(), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(detail, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

data class ImprovementItem(val badge: String, val title: String, val detail: String)

val IMPROVEMENT_ITEMS = listOf(
    ImprovementItem("BAT", "Charging Time", "Shows estimated minutes to full while the device is charging."),
    ImprovementItem("80", "Battery Care", "Adds a saved 80% care option and Battery settings shortcut."),
    ImprovementItem("RAM", "RAM Pressure", "Reads real available RAM and load percentage without extra permissions."),
    ImprovementItem("ROM", "ROM Storage", "Shows internal storage free space, used percentage and safety status."),
    ImprovementItem("CPU", "Performance Level", "Combines CPU cores, Android API, RAM, storage and memory class into one score."),
    ImprovementItem("SAFE", "Clear Controls", "Uses Android Battery settings for phone options that the APK cannot directly change.")
)

@Composable
fun ImprovementCard(item: ImprovementItem) {
    GlassCard {
        HeaderRow(item.badge, item.title, item.detail, Green)
    }
}

data class ModeOption(val label: String, val detail: String)

@Composable
fun ModeSelectorCard(badge: String, title: String, subtitle: String, selected: String, options: List<ModeOption>, onSelected: (String) -> Unit) {
    GlassCard {
        HeaderRow(badge, title, subtitle, Indigo)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                ModePill(option = option, selected = selected == option.label, onClick = { onSelected(option.label) })
            }
        }
    }
}

@Composable
fun ModePill(option: ModeOption, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) Indigo else BorderSoft
    val bgColor = if (selected) Indigo.copy(alpha = 0.16f) else Color(0x08FFFFFF)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(9.dp).background(if (selected) Indigo else TextMuted, CircleShape))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(option.label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(option.detail, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
fun ActionButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

val SUPPORTED_BRANDS = listOf(
    "OPPO" to "ColorOS battery and charging settings shortcut",
    "OnePlus" to "OxygenOS battery and charging settings shortcut",
    "Realme" to "Realme UI battery and charging settings shortcut"
)

@Composable
fun BrandChip(brand: Pair<String, String>) {
    GlassCard(padded = false) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            BadgeDot(color = Green)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(brand.first, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(brand.second, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.32f), RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 7.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TinyInfoCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0FFFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            .padding(11.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(value, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

@Composable
fun MiniStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Glass)
            .border(1.dp, BorderSoft, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
    }
}

@Composable
fun FooterNote() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Battery-Rock v${BuildConfig.VERSION_NAME}", color = TextMuted, fontSize = 11.sp)
        Text("Dark premium dashboard · Android 12+", color = Color(0xFF475569), fontSize = 11.sp)
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
fun GlassCard(padded: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Glass)
            .border(1.dp, BorderSoft, RoundedCornerShape(22.dp))
            .then(if (padded) Modifier.padding(16.dp) else Modifier),
        content = content
    )
}

@Composable
fun BadgeCircle(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(color.copy(alpha = 0.18f), CircleShape)
            .border(1.dp, color.copy(alpha = 0.38f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun BadgeBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 36.dp)
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.24f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
fun BadgeDot(color: Color) {
    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
}

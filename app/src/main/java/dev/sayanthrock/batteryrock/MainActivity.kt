package dev.sayanthrock.batteryrock

import android.os.Bundle
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
private val GlassStrong = Color(0x1FFFFFFF)
private val BorderSoft = Color(0x1FFFFFFF)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextSecondary = Color(0xFFA7B0C0)
private val TextMuted = Color(0xFF64748B)
private val Indigo = Color(0xFF818CF8)
private val Cyan = Color(0xFF38BDF8)
private val Green = Color(0xFF22C55E)
private val Amber = Color(0xFFF59E0B)
private val Red = Color(0xFFEF4444)

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
    val batteryHealth = remember { DeviceStatusReader.readBatteryHealth(context) }
    val performanceLevel = remember { DeviceStatusReader.readPerformanceLevel(context) }

    var batteryMode by remember { mutableStateOf("Balanced") }
    var performanceMode by remember { mutableStateOf("Standard") }
    var refreshRateMode by remember { mutableStateOf("Auto-select") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Midnight, Ink, Color(0xFF111827))
                )
            )
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

            item { SectionLabel("Smart controls") }
            item {
                ModeSelectorCard(
                    badge = "BAT",
                    title = "Battery Backup",
                    subtitle = "Choose a balanced profile for day-to-day battery backup.",
                    selected = batteryMode,
                    options = listOf(
                        ModeOption("Safe", "Gentle settings for daily use"),
                        ModeOption("Balanced", "Best default profile"),
                        ModeOption("Advanced", "Stronger background limits")
                    ),
                    onSelected = { batteryMode = it }
                )
            }
            item {
                ModeSelectorCard(
                    badge = "CPU",
                    title = "Phone Performance",
                    subtitle = "Keep the UI smooth while reducing unnecessary background load.",
                    selected = performanceMode,
                    options = listOf(
                        ModeOption("Standard", "Stable everyday behavior"),
                        ModeOption("Smooth", "Better scrolling feel"),
                        ModeOption("Performance", "For heavier use")
                    ),
                    onSelected = { performanceMode = it }
                )
            }
            item {
                ModeSelectorCard(
                    badge = "HZ",
                    title = "Refresh Rate",
                    subtitle = "Pick a display behavior that matches battery or smoothness needs.",
                    selected = refreshRateMode,
                    options = listOf(
                        ModeOption("Auto-select", "Let the phone choose"),
                        ModeOption("High", "Smoother animations"),
                        ModeOption("Standard", "Battery-first display mode")
                    ),
                    onSelected = { refreshRateMode = it }
                )
            }

            item { SectionLabel("Improvement center") }
            items(IMPROVEMENT_ITEMS) { ImprovementCard(it) }

            item { SectionLabel("Supported brands") }
            items(SUPPORTED_BRANDS) { BrandChip(it) }

            item { SectionLabel("Target packages") }
            items(TARGETED_PACKAGES) { PackageChip(it) }

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
        "Dashboard is safe to open. Enable module setup only when needed."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF141A33), Color(0xFF0B1224), Color(0xFF101827))
                )
            )
            .border(1.dp, Color(0x2FFFFFFF), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeCircle(text = "BR", color = Indigo)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Battery-Rock", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("Battery health · Backup · Performance", color = TextSecondary, fontSize = 13.sp)
            }
            StatusBadge(label = activeLabel, color = activeColor)
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TinyInfoCard("ROM", "OPPO · Realme", Indigo, Modifier.weight(1f))
            TinyInfoCard("UI", "Premium dark", Cyan, Modifier.weight(1f))
            TinyInfoCard("APK", BuildConfig.VERSION_NAME, Green, Modifier.weight(1f))
        }
    }
}

@Composable
fun QuickStatusStrip(
    batteryHealth: BatteryHealthSnapshot,
    performanceLevel: DevicePerformanceSnapshot,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MiniStatCard(
            title = "Battery",
            value = if (batteryHealth.levelPercent >= 0) "${batteryHealth.levelPercent}%" else "--",
            color = Green,
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            title = "Health",
            value = batteryHealth.healthLabel,
            color = Indigo,
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            title = "Score",
            value = "${performanceLevel.score}",
            color = Cyan,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DeviceDashboardCard(
    batteryHealth: BatteryHealthSnapshot,
    performanceLevel: DevicePerformanceSnapshot,
) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeBox(text = "LIVE", color = Green)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Device status", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Clean battery and performance overview with safe fallback values.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                label = "Battery",
                value = if (batteryHealth.levelPercent >= 0) "${batteryHealth.levelPercent}%" else "Unknown",
                detail = batteryHealth.statusLabel,
                accent = Green,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Health",
                value = batteryHealth.healthLabel,
                detail = batteryHealth.summary,
                accent = Indigo,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                label = "Temp",
                value = batteryHealth.temperatureC,
                detail = "${batteryHealth.powerSource} · ${batteryHealth.capacityEstimate}",
                accent = Amber,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Performance",
                value = performanceLevel.levelLabel,
                detail = "${performanceLevel.score}/100 · ${performanceLevel.cores} cores",
                accent = Cyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(performanceLevel.summary, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(3.dp))
        Text(
            text = "${performanceLevel.androidVersion} · Memory class ${performanceLevel.memoryClassMb} MB",
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
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
    ImprovementItem("BAT", "Battery Backup", "Tracks idle drain, charging status, temperature and useful power details."),
    ImprovementItem("SAFE", "Crash-safe UI", "The dashboard uses fallback values when a ROM hides device data."),
    ImprovementItem("CPU", "Performance Level", "Summarizes phone capability with a clear score and status label."),
    ImprovementItem("OEM", "Brand Profiles", "Designed around ColorOS, OxygenOS and Realme UI style expectations.")
)

@Composable
fun ImprovementCard(item: ImprovementItem) {
    GlassCard {
        Row(verticalAlignment = Alignment.Top) {
            BadgeBox(text = item.badge, color = Green)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(3.dp))
                Text(item.detail, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

data class ModeOption(val label: String, val detail: String)

@Composable
fun ModeSelectorCard(
    badge: String,
    title: String,
    subtitle: String,
    selected: String,
    options: List<ModeOption>,
    onSelected: (String) -> Unit,
) {
    GlassCard {
        Row(verticalAlignment = Alignment.Top) {
            BadgeBox(text = badge, color = Indigo)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { option ->
                        ModePill(
                            option = option,
                            selected = selected == option.label,
                            onClick = { onSelected(option.label) }
                        )
                    }
                }
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
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(if (selected) Indigo else TextMuted, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(option.label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(option.detail, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

data class HookItem(val badge: String, val title: String, val detail: String)

val SUPPORTED_BRANDS = listOf(
    "OPPO" to "ColorOS friendly dashboard profile",
    "OnePlus" to "OxygenOS battery and performance profile",
    "Realme" to "Realme UI device status profile"
)

@Composable
fun BrandChip(brand: Pair<String, String>) {
    GlassCard(padded = false) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeDot(color = Green)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(brand.first, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(brand.second, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

val TARGETED_PACKAGES = listOf(
    "com.oplus.onetrace" to "Telemetry service",
    "com.oplus.appsense" to "Usage analytics",
    "com.oplus.powermonitor" to "Power monitor",
    "com.oplus.logkit" to "Log tools",
    "com.oplus.olc" to "OPLUS log center",
    "com.debug.loggerui" to "Logger UI",
    "com.oplus.sau" to "System app updater",
    "com.oplus.romupdate" to "ROM update service",
    "com.nearme.instant.platform" to "Instant platform",
    "com.oplus.appplatform" to "OPLUS app platform",
    "com.realme.systemservice" to "Realme service",
    "com.oneplus.statistics" to "OnePlus statistics"
)

@Composable
fun PackageChip(pkg: Pair<String, String>) {
    GlassCard(padded = false) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeDot(color = Indigo)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pkg.first, color = Color(0xFFD1D5DB), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(pkg.second, color = TextMuted, fontSize = 11.sp)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 8.dp),
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
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

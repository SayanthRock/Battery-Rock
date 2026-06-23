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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryRockTheme {
                BatteryRockScreen(isActive = BatteryRockInit.isModuleActive())
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
                    colors = listOf(
                        Color(0xFF05060A),
                        Color(0xFF0A0A0A),
                        Color(0xFF101323)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item { HeaderCard() }
            item { StatusCard(isActive) }

            item { SectionLabel("Device Battery Health") }
            item { DeviceDashboardCard(batteryHealth, performanceLevel) }

            item { SectionLabel("Improvement Center") }
            items(IMPROVEMENT_ITEMS) { ImprovementCard(it) }

            item { SectionLabel("Customization Options") }
            item {
                ModeSelectorCard(
                    badge = "BAT",
                    title = "Battery Backup",
                    subtitle = "Full improvement target for idle drain and background efficiency.",
                    selected = batteryMode,
                    options = listOf(
                        ModeOption("Safe", "Daily use, lower risk"),
                        ModeOption("Balanced", "Best default battery backup"),
                        ModeOption("Advanced", "Stronger control, test carefully")
                    ),
                    onSelected = { batteryMode = it }
                )
            }
            item {
                ModeSelectorCard(
                    badge = "CPU",
                    title = "Mobile Performance",
                    subtitle = "Improve smoothness by reducing unnecessary background work.",
                    selected = performanceMode,
                    options = listOf(
                        ModeOption("Standard", "Stable daily performance"),
                        ModeOption("Smooth", "Better UI responsiveness"),
                        ModeOption("Performance", "For gaming and heavy use")
                    ),
                    onSelected = { performanceMode = it }
                )
            }
            item {
                ModeSelectorCard(
                    badge = "HZ",
                    title = "Screen Refresh Rate",
                    subtitle = "Choose display smoothness behavior where the device supports it.",
                    selected = refreshRateMode,
                    options = listOf(
                        ModeOption("Auto-select", "Phone chooses best refresh rate"),
                        ModeOption("High", "Smoother scrolling and animations"),
                        ModeOption("Standard", "Better battery backup focus")
                    ),
                    onSelected = { refreshRateMode = it }
                )
            }

            item { SectionLabel("Hook Coverage") }
            items(HOOK_ITEMS) { HookCard(it) }

            item { SectionLabel("Supported Brands") }
            items(SUPPORTED_BRANDS) { BrandChip(it) }

            item { SectionLabel("Targeted Packages") }
            items(TARGETED_PACKAGES) { PackageChip(it) }

            item { FooterNote() }
        }
    }
}

@Composable
fun HeaderCard() {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeCircle(text = "BR", color = Color(0xFF818CF8))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "Battery-Rock",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = "OPPO · OnePlus · Realme",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )
                Text(
                    text = "Battery backup, battery health and performance module",
                    color = Color(0xFF818CF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusCard(isActive: Boolean) {
    val color = if (isActive) Color(0xFF22C55E) else Color(0xFFEF4444)
    val statusText = if (isActive) "Module Active" else "Module Inactive"
    val descText = if (isActive) {
        "LSPosed hooks are running. Battery and performance controls are ready."
    } else {
        "Enable Battery-Rock in LSPosed Manager, select scope, then reboot."
    }

    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isActive) "ON" else "OFF",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(statusText, color = color, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(2.dp))
                Text(descText, color = Color(0xFF9CA3AF), fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
fun DeviceDashboardCard(
    batteryHealth: BatteryHealthSnapshot,
    performanceLevel: DevicePerformanceSnapshot,
) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeBox(text = "LIVE", color = Color(0xFF22C55E))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Live Device Status", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Battery health, charging state, temperature and phone performance level.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                label = "Battery",
                value = if (batteryHealth.levelPercent >= 0) "${batteryHealth.levelPercent}%" else "Unknown",
                detail = batteryHealth.statusLabel,
                accent = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Health",
                value = batteryHealth.healthLabel,
                detail = batteryHealth.summary,
                accent = Color(0xFF818CF8),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                label = "Temp",
                value = batteryHealth.temperatureC,
                detail = "${batteryHealth.powerSource} · ${batteryHealth.capacityEstimate}",
                accent = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Performance",
                value = performanceLevel.levelLabel,
                detail = "${performanceLevel.score}/100 · ${performanceLevel.cores} cores",
                accent = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = performanceLevel.summary,
            color = Color(0xFF9CA3AF),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "${performanceLevel.androidVersion} · App memory class ${performanceLevel.memoryClassMb} MB",
            color = Color(0xFF6B7280),
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
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(label, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        Text(detail, color = Color(0xFF9CA3AF), fontSize = 11.sp, lineHeight = 15.sp)
    }
}

data class ImprovementItem(val badge: String, val title: String, val detail: String)

val IMPROVEMENT_ITEMS = listOf(
    ImprovementItem(
        "BAT",
        "Battery Backup Full Improvement",
        "Targets idle drain, repeated wakeups, background activity, and long wakelocks."
    ),
    ImprovementItem(
        "HLT",
        "Battery Health Awareness",
        "Shows battery health, level, charging state, temperature, voltage, and estimated capacity."
    ),
    ImprovementItem(
        "CPU",
        "Phone Performance Level",
        "Calculates a clear performance level from CPU cores, Android version, and app memory class."
    ),
    ImprovementItem(
        "OEM",
        "OPPO · OnePlus · Realme Support",
        "Designed around ColorOS, OxygenOS, and Realme UI scope packages and testing flow."
    )
)

@Composable
fun ImprovementCard(item: ImprovementItem) {
    GlassCard {
        Row(verticalAlignment = Alignment.Top) {
            BadgeBox(text = item.badge, color = Color(0xFF22C55E))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(3.dp))
                Text(item.detail, color = Color(0xFF9CA3AF), fontSize = 12.sp, lineHeight = 17.sp)
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
            BadgeBox(text = badge, color = Color(0xFF818CF8))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = Color(0xFF9CA3AF), fontSize = 12.sp, lineHeight = 17.sp)
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
    val borderColor = if (selected) Color(0xFF818CF8) else Color(0x1AFFFFFF)
    val bgColor = if (selected) Color(0x1F818CF8) else Color(0x08FFFFFF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (selected) Color(0xFF818CF8) else Color(0xFF4B5563), CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(option.label, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(option.detail, color = Color(0xFF6B7280), fontSize = 11.sp)
        }
    }
}

data class HookItem(val badge: String, val title: String, val detail: String)

val HOOK_ITEMS = listOf(
    HookItem("SRV", "Service Control", "Reduces selected background service activity in scoped packages."),
    HookItem("JOB", "Job Scheduler Guard", "Controls repeated background job scheduling where supported."),
    HookItem("NET", "Network Activity Guard", "Reduces selected background network activity from scoped packages."),
    HookItem("ALM", "Alarm Throttle", "Limits frequent background alarm wakeups from selected packages."),
    HookItem("WLK", "WakeLock Cap", "Capped wakelock behavior helps reduce long idle drain sessions."),
    HookItem("LOG", "Analytics Write Control", "Reduces unnecessary analytics write activity from targeted packages.")
)

@Composable
fun HookCard(item: HookItem) {
    GlassCard {
        Row(verticalAlignment = Alignment.Top) {
            BadgeBox(text = item.badge, color = Color(0xFF818CF8))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                Text(item.detail, color = Color(0xFF6B7280), fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

val SUPPORTED_BRANDS = listOf(
    "OPPO" to "ColorOS battery backup and performance profile",
    "OnePlus" to "OxygenOS battery backup and performance profile",
    "Realme" to "Realme UI battery backup and performance profile"
)

@Composable
fun BrandChip(brand: Pair<String, String>) {
    GlassCard(padded = false) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeDot(color = Color(0xFF22C55E))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(brand.first, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(brand.second, color = Color(0xFF6B7280), fontSize = 11.sp)
            }
        }
    }
}

val TARGETED_PACKAGES = listOf(
    "com.oplus.onetrace" to "OTrace Telemetry",
    "com.oplus.appsense" to "Usage Analytics",
    "com.oplus.powermonitor" to "Power Stats Tracker",
    "com.oplus.logkit" to "Log Collection",
    "com.oplus.olc" to "OPLUS Log Center",
    "com.debug.loggerui" to "MTK Logger Control",
    "com.oplus.sau" to "System App Updater",
    "com.oplus.romupdate" to "ROM Update Service",
    "com.nearme.instant.platform" to "Instant Apps Platform",
    "com.oplus.appplatform" to "OPLUS App Platform",
    "com.realme.systemservice" to "Realme System Service",
    "com.oneplus.statistics" to "OnePlus Stats"
)

@Composable
fun PackageChip(pkg: Pair<String, String>) {
    GlassCard(padded = false) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeDot(color = Color(0xFF818CF8))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(pkg.first, color = Color(0xFFD1D5DB), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(pkg.second, color = Color(0xFF6B7280), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun FooterNote() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Battery-Rock v${BuildConfig.VERSION_NAME}",
            color = Color(0xFF4B5563),
            fontSize = 11.sp
        )
        Text(
            text = "LSPosed · ColorOS · OxygenOS · Realme UI",
            color = Color(0xFF374151),
            fontSize = 11.sp
        )
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFF9CA3AF),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun GlassCard(padded: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x0DFFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(18.dp))
            .then(if (padded) Modifier.padding(16.dp) else Modifier),
        content = content
    )
}

@Composable
fun BadgeCircle(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(color.copy(alpha = 0.18f), CircleShape)
            .border(1.dp, color.copy(alpha = 0.36f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun BadgeBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 34.dp)
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(11.dp))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(11.dp)),
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

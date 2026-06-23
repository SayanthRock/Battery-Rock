package dev.sayanthrock.batteryrock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sayanthrock.batteryrock.ui.theme.BatteryRockTheme
import kotlinx.coroutines.delay

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

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun BatteryRockScreen(isActive: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Background gradient glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x1A4F46E5), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
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
                item { Header() }
                item { StatusCard(isActive) }
                item { SectionLabel("🔧 Hook Coverage") }
                items(HOOK_ITEMS) { HookCard(it) }
                item { SectionLabel("🎯 Targeted Packages") }
                items(TARGETED_PACKAGES) { PackageChip(it) }
                item { FooterNote() }
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
fun Header() {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.BatteryChargingFull,
                contentDescription = null,
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Battery-Rock",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "OPPO · Realme · OnePlus Battery Optimizer",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp
        )
        Text(
            "by sayanthrock",
            color = Color(0xFF4F46E5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Status Card ──────────────────────────────────────────────────────────────

@Composable
fun StatusCard(isActive: Boolean) {
    val activeColor = Color(0xFF22C55E)
    val inactiveColor = Color(0xFFEF4444)
    val color = if (isActive) activeColor else inactiveColor
    val statusText = if (isActive) "Module Active" else "Module Inactive"
    val descText = if (isActive)
        "LSPosed hooks are running. Battery drain sources are suppressed."
    else
        "Enable Battery-Rock in LSPosed Manager and reboot your device."

    val pulseAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.25f else 0f,
        animationSpec = tween(1000),
        label = "pulse"
    )

    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = pulseAlpha), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
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

// ─── Hook Cards ───────────────────────────────────────────────────────────────

data class HookItem(val icon: ImageVector, val title: String, val detail: String)

val HOOK_ITEMS = listOf(
    HookItem(Icons.Outlined.Block, "Service Killer",
        "Service.onStartCommand → START_NOT_STICKY in all telemetry processes"),
    HookItem(Icons.Outlined.WorkOff, "Job Scheduler Guard",
        "JobSchedulerService + JobSchedulerImpl blocked at both layers"),
    HookItem(Icons.Outlined.SignalCellularOff, "Network Block",
        "URL.openConnection + OkHttpClient.newCall throw IOException"),
    HookItem(Icons.Outlined.AlarmOff, "Alarm Throttle",
        "AlarmManagerService rate-limits telemetry alarms to ≥30 min"),
    HookItem(Icons.Outlined.BatterySaver, "WakeLock Cap",
        "Indefinite wakelocks converted to 30 s; oversized ones clamped"),
    HookItem(Icons.Outlined.Storage, "ContentProvider Drops",
        "ContentResolver.insert blocked for OPLUS telemetry URIs"),
)

@Composable
fun HookCard(item: HookItem) {
    GlassCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0x1A818CF8), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                Text(item.detail, color = Color(0xFF6B7280), fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

// ─── Package List ─────────────────────────────────────────────────────────────

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
    "com.oneplus.statistics" to "OnePlus Stats",
)

@Composable
fun PackageChip(pkg: Pair<String, String>) {
    GlassCard(padded = false) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF818CF8), CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(pkg.first, color = Color(0xFFD1D5DB), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(pkg.second, color = Color(0xFF6B7280), fontSize = 11.sp)
            }
        }
    }
}

// ─── Footer ───────────────────────────────────────────────────────────────────

@Composable
fun FooterNote() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Battery-Rock v${BuildConfig.VERSION_NAME}",
            color = Color(0xFF374151),
            fontSize = 11.sp
        )
        Text(
            "LSPosed · ColorOS 12–16 · OxygenOS 12–16",
            color = Color(0xFF374151),
            fontSize = 11.sp
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String) {
    Text(
        text,
        color = Color(0xFF6B7280),
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0DFFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            .then(if (padded) Modifier.padding(16.dp) else Modifier),
        content = content
    )
}

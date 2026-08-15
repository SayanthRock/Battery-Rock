package dev.sayanthrock.batteryrock

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.IconButton

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor


val ScreenBackground = Color(0xFFF6F8F2)
val CardBackground = Color(0xFFF0F3EB)
val CardBorder = Color(0xFFE3E5DF)
val PrimaryText = Color(0xFF1E1E1E)
val SecondaryText = Color(0xFF5E605C)
val MutedText = Color(0xFF757773)
val AccentAmber = Color(0xFF7F6710)
val AccentCyan = Color(0xFF1B6A20)
val AlertRed = Color(0xFFD32F2F)

@Composable
fun BatteryRockApp() {
    val navController = rememberNavController()
    val viewModel: BatteryViewModel = viewModel()

    val context = LocalContext.current

    // Start foreground service if not already running
    LaunchedEffect(Unit) {
        val serviceIntent = Intent(context, BatteryMonitorService::class.java)
        context.startService(serviceIntent) // Requires proper foreground starting logic depending on SDK, handled in activity or here via startForegroundService
    }

    DisposableEffect(context) {
        viewModel.start(context)
        onDispose { viewModel.stop(context) }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, viewModel) }
        composable("background_activity") { BackgroundActivityScreen(navController) }
        composable("permissions") { PermissionsScreen(navController) }
        composable("app_details/{packageName}") { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName")
            if (packageName != null) {
                AppDetailsScreen(navController, packageName)
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController, viewModel: BatteryViewModel) {
    val state by viewModel.uiState.collectAsState()

    Surface(color = ScreenBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BatteryHeader(state)
            BatteryWaveform()
            BatteryPercentCard(state)
            DetailGrid(state)

            if (state.isCharging) {
                ChargingPredictionCard(state)
            } else {
                DrainPredictionCard(state)
            }

            BackgroundActivityCard(onClick = { navController.navigate("background_activity") })

            FooterNote()
        }
    }
}

@Composable
private fun BatteryWaveform() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(CardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Simple faux waveform for the "Diagnostic Console" look
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(40) {
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = (10..40).random().dp)
                        .background(AccentCyan.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun BatteryHeader(state: BatteryUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Battery-Rock",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modern battery monitor",
                color = SecondaryText,
                fontSize = 13.sp
            )
        }
        ChargingIndicator(isCharging = state.isCharging)
    }
}

@Composable
private fun BatteryPercentCard(state: BatteryUiState) {
    val statusColor by animateColorAsState(
        targetValue = when (state.status) {
            "Charging", "Full" -> AccentCyan
            "Discharging" -> AccentAmber
            else -> AccentCyan
        },
        label = "statusColor"
    )

    BatteryCard {
        Text(
            text = "${state.percentage}%",
            color = AccentAmber, fontFamily = FontFamily.Monospace,
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 76.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (state.isCharging) "⚡ ${state.status}" else state.status,
                color = statusColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (state.isCharging && state.wattage != "Unknown") {
                Text(
                    text = " · ${state.wattage}",
                    color = PrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Health: ${state.health}",
            color = SecondaryText,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun DetailGrid(state: BatteryUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Health", state.health, Modifier.weight(1f))
            StatCard("Temperature", state.temperature, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Voltage", state.voltage, Modifier.weight(1f))
            StatCard("Current", state.current, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ChargingPredictionCard(state: BatteryUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CHARGING PREDICTION",
                color = AccentCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.timeToFullStr,
                color = PrimaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DrainPredictionCard(state: BatteryUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔋 NEED TO CHARGE SOON",
                color = AccentAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Estimated remaining time:",
                color = SecondaryText,
                fontSize = 14.sp
            )
            Text(
                text = state.timeToEmptyStr,
                color = PrimaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BackgroundActivityCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "App Breakdown",
                    color = AccentAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to view detailed app battery impact",
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                color = MutedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                color = PrimaryText,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BatteryCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        content = { content() }
    )
}

@Composable
private fun ChargingIndicator(isCharging: Boolean) {
    val transition = rememberInfiniteTransition(label = "chargePulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCharging) 1.16f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .scale(pulse)
            .shadow(elevation = if (isCharging) 12.dp else 0.dp, spotColor = AccentCyan, shape = CircleShape)
            .size(42.dp)
            .background(if (isCharging) AccentCyan.copy(alpha = 0.18f) else Color(0x0FFFFFFF), CircleShape)
            .border(1.dp, if (isCharging) AccentCyan else CardBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isCharging) "⚡" else "•",
            color = if (isCharging) AccentCyan else SecondaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FooterNote() {
    Text(
        text = "Live data from Android battery broadcast · No extra sensors required",
        color = MutedText,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun BackgroundActivityScreen(navController: NavController) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasPermission = BackgroundActivityAnalyzer.hasUsageStatsPermission(context)
                if (hasPermission) {
                    apps = BackgroundActivityAnalyzer.getBackgroundActivity(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(color = ScreenBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "App Breakdown",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.popBackStack() }
            )

            if (!hasPermission) {
                PermissionCard(permission = AppPermission.USAGE_ACCESS) {
                    val intent = PermissionManager.getSettingsIntent(context, AppPermission.USAGE_ACCESS)
                    if (intent != null) {
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            } else if (apps.isEmpty()) {
                 Text(
                    text = "No recent app activity detected.",
                    color = SecondaryText,
                    fontSize = 14.sp
                )
            } else {
                val totalForegroundTime = apps.sumOf { it.totalTimeInForeground }.coerceAtLeast(1L)
                apps.take(10).forEach { app ->
                    AppActivityItem(app, totalForegroundTime, navController)
                }
            }
        }
    }
}

@Composable
fun AppActivityItem(app: AppUsageInfo, totalForegroundTime: Long, navController: NavController) {
    val totalMinutes = app.totalTimeInForeground / 1000 / 60
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    val timeString = if (hours > 0) "${hours} hr ${mins} min" else "${mins} min"

    val percentage = (app.totalTimeInForeground.toFloat() / totalForegroundTime.toFloat()) * 100f
    val percentageString = String.format("%.1f%%", percentage)

    val estimatedMah = (percentage * 12).toInt() // Rough heuristic for visual consistency

    Card(
        modifier = Modifier.fillMaxWidth().clickable { navController.navigate("app_details/${app.packageName}") },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                ) {
                    if (app.icon != null) {
                        androidx.compose.foundation.Image(
                            painter = rememberDrawablePainter(drawable = app.icon),
                            contentDescription = app.appName,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray, CircleShape))
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))

                Column {
                    Text(
                        text = app.appName,
                        color = PrimaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Active for $timeString · $percentageString",
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "≈$estimatedMah mAh",
                    color = AccentCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    modifier = Modifier.size(width = 60.dp, height = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = percentage / 100f)
                            .height(4.dp)
                            .background(AccentCyan, RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(CardBorder, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionsCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "🔒 Permissions & Access",
                    color = AccentAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to manage required permissions",
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun AppDetailsScreen(navController: NavController, packageName: String) {
    val context = LocalContext.current
    val pm = context.packageManager

    var appInfo by remember { mutableStateOf<AppUsageInfo?>(null) }
    var installedDate by remember { mutableStateOf("") }
    var updatedDate by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    LaunchedEffect(packageName) {
        // Find usage stats
        val usageStats = BackgroundActivityAnalyzer.getBackgroundActivity(context)
        appInfo = usageStats.find { it.packageName == packageName }

        // Get install/update dates
        try {
            val packageInfo = pm.getPackageInfo(packageName, 0)
            installedDate = dateFormat.format(Date(packageInfo.firstInstallTime))
            updatedDate = dateFormat.format(Date(packageInfo.lastUpdateTime))
        } catch (e: Exception) {
            installedDate = "Unknown"
            updatedDate = "Unknown"
        }
    }

    Surface(color = ScreenBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = appInfo?.appName ?: packageName,
                    color = PrimaryText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // App Icon and Names
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    if (appInfo?.icon != null) {
                        androidx.compose.foundation.Image(
                            painter = rememberDrawablePainter(drawable = appInfo?.icon),
                            contentDescription = appInfo?.appName,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray, RoundedCornerShape(8.dp)))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = appInfo?.appName ?: packageName,
                        color = PrimaryText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = packageName,
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
            }

            // Usage Today
            Column {
                Text(
                    text = "Usage Today",
                    color = PrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("SCREEN TIME", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))

                                val totalMinutes = (appInfo?.totalTimeInForeground ?: 0) / 1000 / 60
                                val hours = totalMinutes / 60
                                val mins = totalMinutes % 60
                                val timeStr = if (hours > 0) "${hours} hr ${mins} min" else "${mins} min"

                                Text(timeStr, color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("SHARE OF SCREEN TIME", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("12.5%", color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold) // Mock
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("EST. BATTERY", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("≈214 mAh", color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold) // Mock
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("OPENED", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("42 times", color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold) // Mock
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("LAST USED", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("10 minutes ago", color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold) // Mock
                        }
                        Text(
                            "Battery use is estimated from this app's share of screen time and the battery drained today.",
                            color = SecondaryText,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // About
            Column {
                Text(
                    text = "About",
                    color = PrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Installed", color = SecondaryText, fontSize = 16.sp)
                            Text(installedDate, color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Updated", color = SecondaryText, fontSize = 16.sp)
                            Text(updatedDate, color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Open app info", color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = SecondaryText)
                    }
                }
            }
        }
    }
}

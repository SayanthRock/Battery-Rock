package dev.sayanthrock.batteryrock

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.Canvas

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


val ScreenBackground = Color(0xFF131511)
val CardBackground = Color(0xFF1B1C1A)
val CardBorder = Color(0xFF2E2E2E)
val PrimaryText = Color(0xFFE2E3DF)
val SecondaryText = Color(0xFFA6A6A6)
val MutedText = Color(0xFF757773)
val AccentAmber = Color(0xFFB18F42)
val AccentCyan = Color(0xFF98D196) // The green from the image
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
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            BatteryRing(state = state)
            Spacer(modifier = Modifier.height(8.dp))
            MonitoringButton()
            Spacer(modifier = Modifier.height(8.dp))
            DetailGrid(state = state)
            Spacer(modifier = Modifier.height(8.dp))
            DeviceProfileCard(state = state)
        }
    }
}

@Composable
fun BatteryRing(state: BatteryUiState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .aspectRatio(1.2f)
    ) {
        val percentage = state.percentageDecimal
        val sweepAngle = (percentage / 100f) * 260f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()

            // Background arc
            drawArc(
                color = Color(0xFF262A22),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Foreground arc
            drawArc(
                color = AccentCyan,
                startAngle = 140f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.US, "%.2f", state.percentageDecimal),
                    color = PrimaryText,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%",
                    color = PrimaryText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
            }
            Text(
                text = state.status.uppercase(),
                color = SecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFF262E25), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .border(1.dp, Color(0xFF3B473A), RoundedCornerShape(16.dp))
            ) {
                val sign = if (state.wattageMw > 0) "+" else ""
                Text(
                    text = "$sign${state.wattageMw} mW",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MonitoringButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(AccentCyan, RoundedCornerShape(16.dp))
            .clickable { /* Toggle Monitoring */ },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // A simple plug/monitoring icon
            Text(text = "⚡", fontSize = 18.sp, color = Color(0xFF131511))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Turn on monitoring",
                color = Color(0xFF131511),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SimpleWaveGraph(modifier: Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val path = Path()
        val width = size.width
        val height = size.height

        path.moveTo(0f, height * 0.7f)
        path.cubicTo(width * 0.2f, height * 0.8f, width * 0.3f, height * 0.4f, width * 0.5f, height * 0.5f)
        path.cubicTo(width * 0.7f, height * 0.6f, width * 0.8f, height * 0.3f, width, height * 0.4f)

        drawPath(
            path = path,
            color = color.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun DetailGrid(state: BatteryUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("VOLTAGE", state.voltage, Modifier.weight(1f), icon = "⚡")

            // Current Card with Wave
            Card(
                modifier = Modifier.weight(1f).height(90.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SimpleWaveGraph(Modifier.fillMaxSize(), AccentCyan)
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("〰", color = SecondaryText, fontSize = 14.sp)
                            Text("CURRENT", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val sign = if (state.currentMa > 0) "+" else ""
                            Text(
                                text = "$sign${String.format(Locale.US, "%.0f", state.currentMa)} mA",
                                color = AccentCyan,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("›", color = SecondaryText, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Wattage Card with Wave
            Card(
                modifier = Modifier.weight(1f).height(90.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SimpleWaveGraph(Modifier.fillMaxSize(), AccentCyan)
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("🔋", color = SecondaryText, fontSize = 12.sp)
                            Text("WATTAGE", color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val sign = if (state.wattageW > 0) "+" else ""
                            Text(
                                text = "$sign${String.format(Locale.US, "%.1f", state.wattageW)} W",
                                color = AccentCyan,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("›", color = SecondaryText, fontSize = 20.sp)
                        }
                    }
                }
            }

            StatCard("TEMPERATURE", state.temperature, Modifier.weight(1f), icon = "🌡")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("HEALTH", state.health, Modifier.weight(1f), icon = "♥", valueColor = AccentCyan)
            StatCard("PLUGGED", state.powerSource, Modifier.weight(1f), icon = "🔌")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("MAX CAPACITY", state.maxCapacity, Modifier.weight(1f), icon = "🔋")
            StatCard("CHARGE STATUS", state.status, Modifier.weight(1f), icon = "📈", valueColor = AccentCyan)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, icon: String = "", valueColor: Color = PrimaryText) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(icon, color = SecondaryText, fontSize = 14.sp)
                Text(
                    text = label.uppercase(),
                    color = SecondaryText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    color = valueColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("›", color = SecondaryText, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun DeviceProfileCard(state: BatteryUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DEVICE PROFILE",
                    color = SecondaryText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text("📱", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.model.uppercase(),
                color = PrimaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MANUFACTURER",
                        color = SecondaryText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.manufacturer,
                        color = PrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BOARD",
                        color = SecondaryText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.board,
                        color = PrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
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
                    Icon(imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
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
                        Icon(imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Open", tint = SecondaryText)
                    }
                }
            }
        }
    }
}

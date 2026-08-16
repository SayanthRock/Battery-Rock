package dev.sayanthrock.batteryrock

import androidx.compose.material.icons.filled.CheckCircle

import android.content.Intent
import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import dev.sayanthrock.batteryrock.AccentCyan
import dev.sayanthrock.batteryrock.PrimaryText
import dev.sayanthrock.batteryrock.SecondaryText

@Composable
fun SetupWizardScreen(navController: NavController) {
    var currentStep by remember { mutableIntStateOf(0) }

    when (currentStep) {
        0 -> WelcomeStep { currentStep = 1 }
        1 -> PermissionsStep { navController.navigate("home") { popUpTo("setup") { inclusive = true } } }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Placeholder for Icon - using existing launcher icon
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_round),
            contentDescription = "Battery Rock Logo",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to\nBattery Rock",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Optimize your device performance and\nmonitor battery health effortlessly.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6A20)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun PermissionsStep(onFinish: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    val sharedPreferences = context.getSharedPreferences("battery_rock_prefs", Context.MODE_PRIVATE)

    val onFinishSave = {
        sharedPreferences.edit().putBoolean("setup_complete", true).apply()
        onFinish()
    }

    val permissions = listOf(
        AppPermission.NOTIFICATIONS,
        AppPermission.USAGE_ACCESS,
        AppPermission.BATTERY_OPTIMIZATION,
        AppPermission.ALARMS_REMINDERS
    )

    // Check if root & Xposed are active (simulated or actual)
    key(refreshTrigger) {
        val allGranted = permissions.all { PermissionManager.checkPermission(context, it) }
        val isXposedActive = BatteryRockStatus.isModuleActive()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                text = "Setup Permissions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Battery Rock requires the following permissions to function fully.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Xposed / Root Status Card
                SetupPermissionCard(
                    title = "Root / Xposed Module",
                    description = "Required for deep system hooks. Please enable in LSPosed and reboot.",
                    isGranted = isXposedActive,
                    onClickEnable = {
                        // Normally you can't just "enable" Xposed via intent easily,
                        // but maybe we can open LSPosed manager if installed.
                    }
                )

                permissions.forEach { permission ->
                    SetupPermissionCard(
                        title = permission.title,
                        description = permission.description,
                        isGranted = PermissionManager.checkPermission(context, permission),
                        onClickEnable = {
                            val intent = PermissionManager.getSettingsIntent(context, permission)
                            if (intent != null) {
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallbackIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = android.net.Uri.parse("package:${context.packageName}")
                                    }
                                    try { context.startActivity(fallbackIntent) } catch (ex: Exception) {}
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFinishSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6A20)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = if (allGranted && isXposedActive) "Finish Setup" else "Continue Anyway",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SetupPermissionCard(title: String, description: String, isGranted: Boolean, onClickEnable: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isGranted) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = Color(0xFF1B6A20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = Color.Gray,
                fontSize = 14.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClickEnable,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6A20)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Grant Permission", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

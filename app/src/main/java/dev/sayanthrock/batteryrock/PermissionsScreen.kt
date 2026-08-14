package dev.sayanthrock.batteryrock

import android.content.Intent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import dev.sayanthrock.batteryrock.ui.theme.*

@Composable
fun PermissionsScreen(navController: NavController) {
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

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Permissions & Access",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.popBackStack() }
            )

            Text(
                text = "Battery Rock needs certain permissions to function correctly. Enable the required permissions below.",
                color = SecondaryText,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trigger recomposition on refreshTrigger change
            key(refreshTrigger) {
                AppPermission.values().forEach { permission ->
                    PermissionCard(permission = permission) {
                        val intent = PermissionManager.getSettingsIntent(context, permission)
                        if (intent != null) {
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle exception, maybe fallback to app details settings
                                val fallbackIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                try {
                                   context.startActivity(fallbackIntent)
                                } catch (ex: Exception) {
                                    // Give up
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionCard(permission: AppPermission, onClickEnable: () -> Unit) {
    val context = LocalContext.current
    val isGranted = PermissionManager.checkPermission(context, permission)

    val (borderColor, statusText, statusColor) = if (isGranted) {
        Triple(AccentGreen, "Enabled", AccentGreen)
    } else if (permission.isRequired) {
        Triple(AccentAmber, "Required", AccentAmber)
    } else {
        Triple(CardBorder, "Optional", SecondaryText)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.linearGradient(listOf(borderColor, borderColor)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isGranted) {
                        Text(text = "✓ ", color = AccentGreen, fontWeight = FontWeight.Bold)
                    } else if (permission.isRequired) {
                        Text(text = "⚠ ", color = AccentAmber, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = permission.title,
                        color = PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = permission.description,
                color = SecondaryText,
                fontSize = 13.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClickEnable,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Enable", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

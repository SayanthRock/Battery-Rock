package dev.sayanthrock.batteryrock

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.sayanthrock.batteryrock.CardBackground
import dev.sayanthrock.batteryrock.CardBorder
import dev.sayanthrock.batteryrock.PrimaryText
import dev.sayanthrock.batteryrock.ScreenBackground
import dev.sayanthrock.batteryrock.SecondaryText

import dev.sayanthrock.batteryrock.AccentCyan
import dev.sayanthrock.batteryrock.AlertRed


@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

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
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Settings",
                    color = PrimaryText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // About Section
            Column {
                Text(
                    text = "About",
                    color = PrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SayanthRock/Battery-Rock"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = AccentCyan, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Battery Rock", color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("by Sayanth Rock", color = SecondaryText, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Xposed Module Status
            Column {
                Text(
                    text = "Module Status",
                    color = PrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val isActive = BatteryRockStatus.isModuleActive()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Xposed Status", color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("LSPosed / KernelSU / Magisk", color = SecondaryText, fontSize = 12.sp)
                        }
                        Text(
                            text = if (isActive) "Active" else "Inactive",
                            color = if (isActive) AccentCyan else AlertRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Permissions Management
            Column {
                Text(
                    text = "Permissions",
                    color = PrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        navController.navigate("permissions")
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
                        Column {
                            Text("Manage Permissions", color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Review or change Android permissions", color = SecondaryText, fontSize = 12.sp)
                        }
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = SecondaryText)
                    }
                }
            }
        }
    }
}

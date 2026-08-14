package dev.sayanthrock.batteryrock

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

enum class AppPermission(val title: String, val description: String, val isRequired: Boolean) {
    NOTIFICATIONS("Notifications", "Required for battery alerts and monitoring status", true),
    BATTERY_OPTIMIZATION("Battery optimization", "Required for reliable background monitoring", true),
    USAGE_ACCESS("Usage access", "Required for app activity monitoring", false),
    FOREGROUND_SERVICE("Foreground service", "Required to keep monitoring active", true),
    BLUETOOTH("Bluetooth", "Required for connected device monitoring", false)
}

object PermissionManager {

    fun checkPermission(context: Context, permission: AppPermission): Boolean {
        return when (permission) {
            AppPermission.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else {
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                }
            }
            AppPermission.BATTERY_OPTIMIZATION -> {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
            AppPermission.USAGE_ACCESS -> {
                BackgroundActivityAnalyzer.hasUsageStatsPermission(context)
            }
            AppPermission.FOREGROUND_SERVICE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
                } else {
                    true // Implicitly granted on older versions
                }
            }
            AppPermission.BLUETOOTH -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                     ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                } else {
                    true // Needs to be handled more specifically if strictly needed, but simplifies for now
                }
            }
        }
    }

    fun getSettingsIntent(context: Context, permission: AppPermission): Intent? {
        return when (permission) {
            AppPermission.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                }
            }
            AppPermission.BATTERY_OPTIMIZATION -> {
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
            AppPermission.USAGE_ACCESS -> {
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
            AppPermission.FOREGROUND_SERVICE, AppPermission.BLUETOOTH -> {
                 Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
        }
    }
}

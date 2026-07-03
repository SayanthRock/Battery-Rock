package dev.sayanthrock.batteryrock

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import java.util.Locale
import kotlin.math.roundToInt

data class BatteryHealthSnapshot(
    val levelPercent: Int,
    val statusLabel: String,
    val healthLabel: String,
    val temperatureC: String,
    val voltageMv: Int,
    val capacityEstimate: String,
    val powerSource: String,
    val summary: String,
)

data class DevicePerformanceSnapshot(
    val levelLabel: String,
    val score: Int,
    val cores: Int,
    val memoryClassMb: Int,
    val isLowRam: Boolean,
    val androidVersion: String,
    val summary: String,
)

object DeviceStatusReader {

    private const val BATTERY_PLUGGED_DOCK_COMPAT = 8

    fun readBatteryHealth(context: Context): BatteryHealthSnapshot = runCatching {
        val appContext = context.applicationContext
        val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val levelPercent = if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
        } else {
            -1
        }

        val status = intent.readIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        )
        val health = intent.readIntExtra(
            BatteryManager.EXTRA_HEALTH,
            BatteryManager.BATTERY_HEALTH_UNKNOWN
        )
        val plugged = intent.readIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val temperatureRaw = intent.readIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val voltageMv = intent.readIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

        BatteryHealthSnapshot(
            levelPercent = levelPercent,
            statusLabel = batteryStatusLabel(status),
            healthLabel = batteryHealthLabel(health),
            temperatureC = if (temperatureRaw != Int.MIN_VALUE) {
                String.format(Locale.US, "%.1f°C", temperatureRaw / 10f)
            } else {
                "Unknown"
            },
            voltageMv = voltageMv,
            capacityEstimate = readCapacityEstimate(appContext),
            powerSource = powerSourceLabel(plugged),
            summary = batterySummary(health, status, levelPercent),
        )
    }.getOrElse {
        BatteryHealthSnapshot(
            levelPercent = -1,
            statusLabel = "Unknown",
            healthLabel = "Unknown",
            temperatureC = "Unknown",
            voltageMv = -1,
            capacityEstimate = "Unknown",
            powerSource = "Unknown",
            summary = "Battery status could not be read on this ROM. The app remains safe to open.",
        )
    }

    fun readPerformanceLevel(context: Context): DevicePerformanceSnapshot = runCatching {
        val appContext = context.applicationContext
        val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val memoryClass = activityManager?.memoryClass?.coerceAtLeast(1) ?: 128
        val largeMemoryClass = activityManager?.largeMemoryClass?.coerceAtLeast(memoryClass) ?: memoryClass
        val isLowRam = activityManager?.isLowRamDevice ?: false

        var score = 38
        score += cores.coerceAtMost(8) * 4
        score += (memoryClass / 128).coerceIn(1, 12) * 2
        score += if (largeMemoryClass >= 512) 8 else 0
        score += when {
            Build.VERSION.SDK_INT >= 35 -> 8
            Build.VERSION.SDK_INT >= 33 -> 6
            Build.VERSION.SDK_INT >= 31 -> 4
            else -> 2
        }
        if (isLowRam) score -= 25
        score = score.coerceIn(0, 100)

        val level = when {
            score >= 80 -> "High Performance"
            score >= 65 -> "Smooth"
            score >= 45 -> "Standard"
            else -> "Basic"
        }

        DevicePerformanceSnapshot(
            levelLabel = level,
            score = score,
            cores = cores,
            memoryClassMb = memoryClass,
            isLowRam = isLowRam,
            androidVersion = "Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}",
            summary = if (isLowRam) {
                "Low-RAM profile detected. Battery-first tuning is recommended."
            } else {
                "Device is suitable for balanced battery backup and smooth performance tuning."
            },
        )
    }.getOrElse {
        DevicePerformanceSnapshot(
            levelLabel = "Unknown",
            score = 0,
            cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
            memoryClassMb = 0,
            isLowRam = false,
            androidVersion = "Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}",
            summary = "Performance status could not be read on this ROM. The dashboard remains available.",
        )
    }

    private fun Intent?.readIntExtra(name: String, fallback: Int): Int =
        this?.getIntExtra(name, fallback) ?: fallback

    private fun readCapacityEstimate(context: Context): String = runCatching {
        val batteryManager = context.getSystemService(BatteryManager::class.java) ?: return "Unknown"
        val chargeCounterMicroAh = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacityPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        if (chargeCounterMicroAh <= 0 || capacityPercent <= 0) {
            return "Unknown"
        }

        val currentMah = chargeCounterMicroAh / 1000f
        val estimatedFullMah = (currentMah * 100f) / capacityPercent
        "${estimatedFullMah.roundToInt()} mAh est."
    }.getOrDefault("Unknown")

    private fun batterySummary(health: Int, status: Int, levelPercent: Int): String = when {
        health == BatteryManager.BATTERY_HEALTH_GOOD && levelPercent >= 20 ->
            "Battery health looks normal from Android status data."
        health == BatteryManager.BATTERY_HEALTH_OVERHEAT ->
            "Battery is hot. Let the phone cool before gaming or charging."
        health == BatteryManager.BATTERY_HEALTH_COLD ->
            "Battery is cold. Performance may be limited until temperature normalizes."
        health == BatteryManager.BATTERY_HEALTH_DEAD ->
            "Battery health is critical. Service check is recommended."
        status == BatteryManager.BATTERY_STATUS_CHARGING ->
            "Charging detected. Avoid heavy load for better battery health."
        levelPercent in 0..15 ->
            "Low battery. Standard refresh rate and battery mode are recommended."
        else ->
            "Battery status is available. Enable recommended scope for drain reduction."
    }

    private fun batteryHealthLabel(health: Int): String = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Critical"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over-voltage"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    private fun batteryStatusLabel(status: Int): String = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "Unknown"
    }

    private fun powerSourceLabel(plugged: Int): String = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC charger"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        BATTERY_PLUGGED_DOCK_COMPAT -> "Dock"
        else -> "Battery"
    }
}

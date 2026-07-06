package dev.sayanthrock.batteryrock

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
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
    val largeMemoryClassMb: Int,
    val isLowRam: Boolean,
    val totalRam: String,
    val availableRam: String,
    val ramLoadPercent: Int,
    val ramPressureLabel: String,
    val storageFree: String,
    val storageTotal: String,
    val storageUsedPercent: Int,
    val storagePressureLabel: String,
    val romName: String,
    val recommendedProfile: String,
    val androidVersion: String,
    val summary: String,
)

object DeviceStatusReader {

    private const val BATTERY_PLUGGED_DOCK_COMPAT = 8
    private const val BYTES_PER_GIB = 1024L * 1024L * 1024L

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
        val memoryInfo = readMemoryInfo(activityManager)
        val storageInfo = readStorageInfo(appContext)
        val ramLoadPercent = memoryInfo.usedPercent
        val storageUsedPercent = storageInfo.usedPercent
        val ramScoreGb = if (memoryInfo.totalBytes > 0L) {
            (memoryInfo.totalBytes / BYTES_PER_GIB).toInt().coerceIn(1, 16)
        } else {
            (memoryClass / 1024).coerceIn(1, 8)
        }

        var score = 30
        score += cores.coerceAtMost(10) * 3
        score += ramScoreGb * 2
        score += (memoryClass / 128).coerceIn(1, 12)
        score += (largeMemoryClass / 256).coerceIn(0, 6) * 2
        score += when {
            Build.VERSION.SDK_INT >= 35 -> 8
            Build.VERSION.SDK_INT >= 33 -> 6
            Build.VERSION.SDK_INT >= 31 -> 4
            else -> 2
        }
        score += when {
            storageInfo.freeBytes >= 32L * BYTES_PER_GIB -> 6
            storageInfo.freeBytes >= 16L * BYTES_PER_GIB -> 4
            storageInfo.freeBytes >= 8L * BYTES_PER_GIB -> 2
            else -> 0
        }
        score += when {
            ramLoadPercent in 1..70 -> 6
            ramLoadPercent in 71..85 -> 1
            ramLoadPercent > 90 -> -15
            ramLoadPercent > 85 -> -8
            else -> 0
        }
        score += when {
            storageUsedPercent > 92 -> -12
            storageUsedPercent > 85 -> -7
            storageUsedPercent > 78 -> -3
            else -> 0
        }
        if (isLowRam) score -= 25
        score = score.coerceIn(0, 100)

        val level = when {
            score >= 85 -> "Extreme Performance"
            score >= 75 -> "High Performance"
            score >= 60 -> "Smooth"
            score >= 45 -> "Standard"
            else -> "Basic"
        }
        val ramPressure = ramPressureLabel(isLowRam, ramLoadPercent)
        val storagePressure = storagePressureLabel(storageUsedPercent)
        val recommendedProfile = recommendedProfile(score, isLowRam, ramLoadPercent, storageUsedPercent)

        DevicePerformanceSnapshot(
            levelLabel = level,
            score = score,
            cores = cores,
            memoryClassMb = memoryClass,
            largeMemoryClassMb = largeMemoryClass,
            isLowRam = isLowRam,
            totalRam = formatBytes(memoryInfo.totalBytes),
            availableRam = formatBytes(memoryInfo.availableBytes),
            ramLoadPercent = ramLoadPercent,
            ramPressureLabel = ramPressure,
            storageFree = formatBytes(storageInfo.freeBytes),
            storageTotal = formatBytes(storageInfo.totalBytes),
            storageUsedPercent = storageUsedPercent,
            storagePressureLabel = storagePressure,
            romName = readRomName(),
            recommendedProfile = recommendedProfile,
            androidVersion = "Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}",
            summary = performanceSummary(level, ramPressure, storagePressure, recommendedProfile),
        )
    }.getOrElse {
        DevicePerformanceSnapshot(
            levelLabel = "Unknown",
            score = 0,
            cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
            memoryClassMb = 0,
            largeMemoryClassMb = 0,
            isLowRam = false,
            totalRam = "Unknown",
            availableRam = "Unknown",
            ramLoadPercent = 0,
            ramPressureLabel = "Unknown",
            storageFree = "Unknown",
            storageTotal = "Unknown",
            storageUsedPercent = 0,
            storagePressureLabel = "Unknown",
            romName = "Unknown ROM",
            recommendedProfile = "Balanced daily profile",
            androidVersion = "Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}",
            summary = "Performance status could not be read on this ROM. The dashboard remains available.",
        )
    }

    private data class MemorySnapshot(
        val totalBytes: Long,
        val availableBytes: Long,
        val usedPercent: Int,
    )

    private data class StorageSnapshot(
        val totalBytes: Long,
        val freeBytes: Long,
        val usedPercent: Int,
    )

    private fun readMemoryInfo(activityManager: ActivityManager?): MemorySnapshot {
        val info = ActivityManager.MemoryInfo()
        runCatching { activityManager?.getMemoryInfo(info) }

        val totalBytes = info.totalMem.coerceAtLeast(0L)
        val availableBytes = info.availMem.coerceAtLeast(0L)
        val usedPercent = if (totalBytes > 0L && availableBytes > 0L) {
            (((totalBytes - availableBytes).coerceAtLeast(0L) * 100f) / totalBytes)
                .roundToInt()
                .coerceIn(0, 100)
        } else {
            0
        }

        return MemorySnapshot(
            totalBytes = totalBytes,
            availableBytes = availableBytes,
            usedPercent = usedPercent,
        )
    }

    private fun readStorageInfo(context: Context): StorageSnapshot = runCatching {
        val statFs = StatFs(context.filesDir.absolutePath)
        val totalBytes = statFs.blockSizeLong * statFs.blockCountLong
        val freeBytes = statFs.blockSizeLong * statFs.availableBlocksLong
        val usedPercent = if (totalBytes > 0L) {
            (((totalBytes - freeBytes).coerceAtLeast(0L) * 100f) / totalBytes)
                .roundToInt()
                .coerceIn(0, 100)
        } else {
            0
        }

        StorageSnapshot(
            totalBytes = totalBytes.coerceAtLeast(0L),
            freeBytes = freeBytes.coerceAtLeast(0L),
            usedPercent = usedPercent,
        )
    }.getOrDefault(StorageSnapshot(totalBytes = 0L, freeBytes = 0L, usedPercent = 0))

    private fun performanceSummary(
        level: String,
        ramPressure: String,
        storagePressure: String,
        recommendedProfile: String,
    ): String =
        "$level profile detected. RAM is $ramPressure, ROM storage is $storagePressure. Recommended mode: $recommendedProfile."

    private fun ramPressureLabel(isLowRam: Boolean, usedPercent: Int): String = when {
        isLowRam -> "Low-RAM"
        usedPercent == 0 -> "Unknown"
        usedPercent <= 70 -> "Healthy"
        usedPercent <= 85 -> "Busy"
        else -> "Critical"
    }

    private fun storagePressureLabel(usedPercent: Int): String = when {
        usedPercent == 0 -> "Unknown"
        usedPercent <= 70 -> "Plenty free"
        usedPercent <= 85 -> "Watch storage"
        usedPercent <= 92 -> "Low space"
        else -> "Critical"
    }

    private fun recommendedProfile(
        score: Int,
        isLowRam: Boolean,
        ramLoadPercent: Int,
        storageUsedPercent: Int,
    ): String = when {
        isLowRam -> "Safe battery profile"
        ramLoadPercent > 90 || storageUsedPercent > 92 -> "Clean storage + Safe profile"
        ramLoadPercent > 85 || storageUsedPercent > 85 -> "Balanced daily profile"
        score >= 85 -> "Performance profile"
        score >= 65 -> "Smooth balanced profile"
        else -> "Safe battery profile"
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

    private fun readRomName(): String {
        val romVersion = readSystemProperty(
            "ro.build.version.oplusrom",
            "ro.build.version.realmeui",
            "ro.oxygen.version",
            "ro.build.version.ota",
        )
        val brand = listOf(Build.MANUFACTURER, Build.BRAND, Build.MODEL)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.US) }
            .joinToString(" · ")

        return listOfNotNull(
            brand.takeIf { it.isNotBlank() },
            romVersion,
        ).joinToString(" · ").ifBlank { "Unknown ROM" }
    }

    private fun readSystemProperty(vararg keys: String): String? {
        return keys.firstNotNullOfOrNull { key ->
            runCatching {
                val systemProperties = Class.forName("android.os.SystemProperties")
                val get = systemProperties.getMethod("get", String::class.java)
                (get.invoke(null, key) as? String)
                    ?.trim()
                    ?.takeIf { it.isNotBlank() && !it.equals("unknown", ignoreCase = true) }
            }.getOrNull()
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "Unknown"
        val gib = bytes.toDouble() / BYTES_PER_GIB.toDouble()
        return if (gib >= 1.0) {
            String.format(Locale.US, "%.1f GB", gib)
        } else {
            String.format(Locale.US, "%.0f MB", bytes / (1024.0 * 1024.0))
        }
    }
}

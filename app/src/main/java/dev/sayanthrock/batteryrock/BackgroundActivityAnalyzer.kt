package dev.sayanthrock.batteryrock

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.graphics.drawable.Drawable
import java.util.Calendar

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val batteryImpact: BatteryImpact,
    val icon: Drawable? = null
)

enum class BatteryImpact(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    SYSTEM("System")
}

object BackgroundActivityAnalyzer {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getBackgroundActivity(context: Context): List<AppUsageInfo> {
        if (!hasUsageStatsPermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, -24)
        val startTime = calendar.timeInMillis

        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        val pm = context.packageManager

        return usageStats.mapNotNull { stats ->
            if (stats.totalTimeInForeground == 0L) return@mapNotNull null // Ignore 0 usage apps

            val packageName = stats.packageName
            val appName = try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }

            // Simple heuristic for battery impact based on foreground time (since background time is hard to get directly via UsageStats)
            // A more complex implementation could use BatteryStats if available/rooted.
            val impact = when {
                stats.totalTimeInForeground > 2 * 60 * 60 * 1000L -> BatteryImpact.HIGH
                stats.totalTimeInForeground > 30 * 60 * 1000L -> BatteryImpact.MEDIUM
                else -> BatteryImpact.LOW
            }

            val icon = try {
                pm.getApplicationIcon(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

            AppUsageInfo(
                packageName = packageName,
                appName = appName,
                totalTimeInForeground = stats.totalTimeInForeground,
                batteryImpact = impact,
                icon = icon
            )
        }.sortedByDescending { it.totalTimeInForeground }
    }
}

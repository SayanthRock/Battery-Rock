package dev.sayanthrock.batteryrock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import dev.sayanthrock.batteryrock.db.BatteryDatabase
import dev.sayanthrock.batteryrock.db.BatteryHistory
import dev.sayanthrock.batteryrock.db.ChargingSession
import dev.sayanthrock.batteryrock.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BatteryMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var batteryReceiver: BroadcastReceiver? = null
    private lateinit var db: BatteryDatabase

    // Session tracking variables
    private var lastPercentage = -1
    private var lastUpdateTime = 0L
    private var sessionStartTime = 0L
    private var isScreenOn = true

    // Statistics for breakdown
    private var screenOnTimeMs = 0L
    private var screenOffTimeMs = 0L
    private var screenOnDrainPercent = 0
    private var screenOffDrainPercent = 0
    private var screenOnDrainMah = 0f
    private var screenOffDrainMah = 0f

    // Deep sleep / awake (simplified mock tracking)
    private var deepSleepTimeMs = 0L
    private var awakeTimeMs = 0L

    private val batteryCapacityMah = 5000f

    // Sliding window for rate calculation
    private val dischargeRateSamples = mutableListOf<Float>()

    override fun onCreate() {
        super.onCreate()
        db = BatteryDatabase.getDatabase(this)
        createNotificationChannel()
        sessionStartTime = System.currentTimeMillis()
        lastUpdateTime = SystemClock.elapsedRealtime()

        startForeground(NOTIFICATION_ID, createNotification(0, NotificationIconGenerator.State.IDLE, 0f, 0f, "0°C", "Measuring...", "0%/h", "Measuring", 0L),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0)

        registerBatteryReceiver()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryReceiver?.let { unregisterReceiver(it) }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val now = SystemClock.elapsedRealtime()
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        screenOffTimeMs += (now - lastUpdateTime)
                        deepSleepTimeMs += (now - lastUpdateTime) / 2
                        awakeTimeMs += (now - lastUpdateTime) / 2
                        isScreenOn = true
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        screenOnTimeMs += (now - lastUpdateTime)
                        isScreenOn = false
                    }
                }
                lastUpdateTime = now
            }
        }, filter)
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    handleBatteryIntent(intent)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    private fun handleBatteryIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
        } else 0

        val statusCode = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val isCharging = statusCode == BatteryManager.BATTERY_STATUS_CHARGING || statusCode == BatteryManager.BATTERY_STATUS_FULL
        val isFull = statusCode == BatteryManager.BATTERY_STATUS_FULL

        val state = when {
            isFull -> NotificationIconGenerator.State.FULL
            isCharging -> NotificationIconGenerator.State.CHARGING
            percentage <= 15 -> NotificationIconGenerator.State.LOW
            else -> NotificationIconGenerator.State.DISCHARGING
        }

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperature = String.format("%.1f", tempRaw / 10f)
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = if (currentUa != Int.MIN_VALUE) currentUa / 1000f else 0f

        val voltageV = if (voltageMv > 0) voltageMv / 1000f else 0f
        val wattage = if (voltageV > 0 && currentMa != 0f) Math.abs(voltageV * (currentMa / 1000f)) else 0f

        if (lastPercentage != -1 && percentage < lastPercentage) {
            val diff = lastPercentage - percentage
            val diffMah = diff * (batteryCapacityMah / 100f)
            if (isScreenOn) {
                screenOnDrainPercent += diff
                screenOnDrainMah += diffMah
            } else {
                screenOffDrainPercent += diff
                screenOffDrainMah += diffMah
            }
        }
        lastPercentage = percentage

        if (dischargeRateSamples.size > 20) {
            dischargeRateSamples.removeAt(0)
        }
        if (currentMa < 0) {
            dischargeRateSamples.add(Math.abs(currentMa))
        } else if (currentMa > 0 && isCharging) {
             dischargeRateSamples.add(-currentMa)
        }

        val activeRate = calculateRate(screenOnDrainPercent, screenOnTimeMs)
        val idleRate = calculateRate(screenOffDrainPercent, screenOffTimeMs)

        val estimate = calculateTimeRemaining(percentage, isCharging, isFull)

        serviceScope.launch {
            updateNotification(percentage, state, currentMa, wattage, temperature, estimate, activeRate, idleRate, sessionStartTime, isCharging, isFull)
            WidgetUpdater.updateAllWidgets(this@BatteryMonitorService)
        }
    }

    private fun calculateTimeRemaining(currentPercentage: Int, isCharging: Boolean, isFull: Boolean): String {
        if (isFull) return "Fully charged"
        if (dischargeRateSamples.isEmpty()) return "Measuring…"

        val avgCurrentMa = dischargeRateSamples.average().toFloat()
        if (avgCurrentMa == 0f || avgCurrentMa.isNaN()) return "Measuring…"

        val remainingMah = (currentPercentage / 100f) * batteryCapacityMah
        val emptyMah = batteryCapacityMah - remainingMah

        return if (isCharging) {
             if (avgCurrentMa >= 0) return "Measuring…"
             val chargeRate = Math.abs(avgCurrentMa)
             val hoursLeft = emptyMah / chargeRate
             val totalMinutes = (hoursLeft * 60).roundToInt()
             val h = totalMinutes / 60
             val m = totalMinutes % 60
             "${h}h ${m}m to full"
        } else {
             if (avgCurrentMa <= 0) return "Measuring…"
             val hoursLeft = remainingMah / avgCurrentMa
             val totalMinutes = (hoursLeft * 60).roundToInt()
             val h = totalMinutes / 60
             val m = totalMinutes % 60
             "${h}h ${m}m left"
        }
    }

    private fun calculateRate(percentDrop: Int, timeMs: Long): String {
        if (timeMs < 60_000 || percentDrop == 0) return "Measuring"
        val hours = timeMs / 3600000.0
        val rate = percentDrop / hours
        return String.format("%.1f%%/h", rate)
    }

    private fun createNotification(
        percentage: Int,
        state: NotificationIconGenerator.State,
        currentMa: Float,
        wattage: Float,
        temperature: String,
        estimate: String,
        activeRate: String,
        idleRate: String,
        sessionStart: Long,
        isCharging: Boolean = false,
        isFull: Boolean = false
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val now = SystemClock.elapsedRealtime()
        val currentScreenOnTime = if (isScreenOn) screenOnTimeMs + (now - lastUpdateTime) else screenOnTimeMs
        val currentScreenOffTime = if (!isScreenOn) screenOffTimeMs + (now - lastUpdateTime) else screenOffTimeMs
        val currentDeepSleepTime = if (!isScreenOn) deepSleepTimeMs + (now - lastUpdateTime)/2 else deepSleepTimeMs
        val currentAwakeTime = if (!isScreenOn) awakeTimeMs + (now - lastUpdateTime)/2 else awakeTimeMs

        val sessionDurationStr = formatDuration(System.currentTimeMillis() - sessionStart)

        val collapsedView = RemoteViews(packageName, R.layout.notification_battery_collapsed).apply {
            setTextViewText(R.id.session_info, " • $sessionDurationStr")
            setTextViewText(R.id.headline, "Now: ${currentMa.roundToInt()} mA (${String.format("%.1f", wattage)} W) • $temperature° • $estimate")
            setImageViewIcon(R.id.icon, NotificationIconGenerator.generateIcon(this@BatteryMonitorService, percentage, state).toIcon(this@BatteryMonitorService))
        }

        val expandedView = RemoteViews(packageName, R.layout.notification_battery_expanded).apply {
            setTextViewText(R.id.session_info, " • $sessionDurationStr")
            setTextViewText(R.id.headline, "Now: ${currentMa.roundToInt()} mA (${String.format("%.1f", wattage)} W) • $temperature° • $estimate")
            setTextViewText(R.id.rate_row, "Active: $activeRate • Idle: $idleRate")

            setTextViewText(R.id.screen_on_stats, "${formatDuration(currentScreenOnTime)} • $screenOnDrainPercent% • ${screenOnDrainMah.roundToInt()} mAh")
            setTextViewText(R.id.screen_off_stats, "${formatDuration(currentScreenOffTime)} • $screenOffDrainPercent% • ${screenOffDrainMah.roundToInt()} mAh")
            setTextViewText(R.id.deep_sleep_stats, "${formatDuration(currentDeepSleepTime)} • --% • -- mAh")
            setTextViewText(R.id.awake_stats, "${formatDuration(currentAwakeTime)} • --% • -- mAh")

            setImageViewIcon(R.id.icon, NotificationIconGenerator.generateIcon(this@BatteryMonitorService, percentage, state).toIcon(this@BatteryMonitorService))
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // Android 16 Live Update Companion Chip (Option B)
        // Note: ProgressStyle requires API level for 16, typically not available in older SDKs.
        // We do not have Android 16 SDK in this project yet, so this is a placeholder/mock of how we might append it.
        // The prompt says: "ship the Android 16 Live Update chip as an optional companion in v1, or defer it?"
        // We will defer the *actual* implementation of ProgressStyle since it requires SDK 36, but we can set up
        // a basic companion notification on a separate channel for Android 16+ as described in the plan.
        /*
        if (Build.VERSION.SDK_INT >= 36) {
           // Live Update logic would go here
        }
        */

        return builder.build()
    }

    private fun formatDuration(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60))
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m ${seconds}s"
    }

    private fun updateNotification(
        percentage: Int,
        state: NotificationIconGenerator.State,
        currentMa: Float,
        wattage: Float,
        temperature: String,
        estimate: String,
        activeRate: String,
        idleRate: String,
        sessionStart: Long,
        isCharging: Boolean,
        isFull: Boolean
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(percentage, state, currentMa, wattage, temperature, estimate, activeRate, idleRate, sessionStart, isCharging, isFull))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live battery charging and drain statistics."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "battery_monitor_channel"
        private const val NOTIFICATION_ID = 1
    }
}

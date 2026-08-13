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
import androidx.core.app.NotificationCompat
import dev.sayanthrock.batteryrock.db.BatteryDatabase
import dev.sayanthrock.batteryrock.db.BatteryHistory
import dev.sayanthrock.batteryrock.db.ChargingSession
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

    override fun onCreate() {
        super.onCreate()
        db = BatteryDatabase.getDatabase(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Monitoring battery...", "Starting..."),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0)
        registerBatteryReceiver()
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
        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperature = String.format("%.1f°C", tempRaw / 10f)
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val voltage = if (voltageMv > 0) "${voltageMv} mV" else "Unknown"

        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = if (currentUa != Int.MIN_VALUE) currentUa / 1000f else 0f

        // Calculate wattage
        val voltageV = if (voltageMv > 0) voltageMv / 1000f else 0f
        // Current is often negative when discharging, positive when charging (or vice versa depending on OEM).
        // We take absolute value for charging power.
        val wattage = if (voltageV > 0 && currentMa != 0f) Math.abs(voltageV * (currentMa / 1000f)) else 0f
        val isFastCharging = wattage > 15f

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargingSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Unknown"
        }
        val chargingType = if (isFastCharging) "Fast Charging" else "Normal Charging"

        serviceScope.launch {
            val now = System.currentTimeMillis()

            // 1. Record History
            db.batteryDao().insertBatteryHistory(
                BatteryHistory(
                    timestamp = now,
                    percentage = percentage,
                    temperature = temperature,
                    voltage = voltage,
                    isCharging = isCharging
                )
            )

            // 2. Manage Charging Session
            val activeSession = db.batteryDao().getActiveChargingSession()
            if (isCharging) {
                if (activeSession == null) {
                    // Start new session
                    val newSession = ChargingSession(
                        startTime = now,
                        startPercentage = percentage,
                        startTemperature = temperature,
                        startVoltage = voltage,
                        startCurrent = "${currentMa} mA",
                        startWattage = String.format("%.1f W", wattage),
                        chargingSource = chargingSource,
                        chargingType = chargingType,
                        maxPowerWatts = wattage,
                        avgPowerWatts = wattage,
                        isFastCharging = isFastCharging
                    )
                    db.batteryDao().insertChargingSession(newSession)
                } else {
                    // Update active session (e.g. max power, max temp)
                    val updatedMaxPower = maxOf(activeSession.maxPowerWatts, wattage)
                    activeSession.copy(
                        maxPowerWatts = updatedMaxPower,
                        // Simplistic avg update
                        avgPowerWatts = (activeSession.avgPowerWatts + wattage) / 2
                    ).let { db.batteryDao().updateChargingSession(it) }
                }
            } else {
                if (activeSession != null) {
                    // End session
                    val endedSession = activeSession.copy(
                        endTime = now,
                        endPercentage = percentage,
                        endTemperature = temperature
                    )
                    db.batteryDao().updateChargingSession(endedSession)
                }
            }

            // 3. Update Notification
            val title = if (isCharging) "⚡ Charging: $percentage%" else "Battery: $percentage%"
            val content = if (isCharging) {
                "$chargingType · ${String.format("%.1f", wattage)} W · $temperature"
            } else {
                "Discharging · $temperature"
            }
            updateNotification(title, content)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(title, content))
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

package dev.sayanthrock.batteryrock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sayanthrock.batteryrock.db.BatteryDatabase
import dev.sayanthrock.batteryrock.db.BatteryHistory
import dev.sayanthrock.batteryrock.db.ChargingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Enhanced MVVM state holder for Android battery data.
 */
class BatteryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<BatteryHistory>>(emptyList())
    val history: StateFlow<List<BatteryHistory>> = _history.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChargingSession>>(emptyList())
    val sessions: StateFlow<List<ChargingSession>> = _sessions.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null

    fun start(context: Context) {
        val appContext = context.applicationContext

        // Load history and sessions from DB
        val db = BatteryDatabase.getDatabase(appContext)
        viewModelScope.launch {
            db.batteryDao().getAllChargingSessions().collect { sessions ->
                _sessions.value = sessions
            }
        }
        viewModelScope.launch {
            // Get history for the last 24 hours
            val last24h = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            db.batteryDao().getBatteryHistorySince(last24h).collect { history ->
                _history.value = history
            }
        }

        if (batteryReceiver != null) return

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        appContext.registerReceiver(null, filter)?.let { stickyIntent ->
            _uiState.value = stickyIntent.toBatteryUiState(appContext)
        }

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    _uiState.value = intent.toBatteryUiState(appContext)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            appContext.registerReceiver(batteryReceiver, filter)
        }
    }

    fun stop(context: Context) {
        val receiver = batteryReceiver ?: return
        runCatching { context.applicationContext.unregisterReceiver(receiver) }
        batteryReceiver = null
    }

    override fun onCleared() {
        batteryReceiver = null
        super.onCleared()
    }
}

data class BatteryUiState(
    val percentage: Int = 0,
    val status: String = "Unknown",
    val health: String = "Unknown",
    val temperature: String = "Unknown",
    val technology: String = "Unknown",
    val voltage: String = "Unknown",
    val current: String = "Unknown",
    val wattage: String = "Unknown",
    val isCharging: Boolean = false,
    val timeToFullStr: String = "Calculating...",
    val timeToEmptyStr: String = "Calculating..."
)

private fun Intent.toBatteryUiState(context: Context): BatteryUiState {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val percentage = if (level >= 0 && scale > 0) {
        ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
    } else {
        0
    }

    val statusCode = getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
    val healthCode = getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
    val tempRaw = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
    val voltageMv = getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

    val currentMa = if (currentUa != Int.MIN_VALUE) currentUa / 1000f else 0f
    val currentStr = if (currentUa != Int.MIN_VALUE) "${String.format("%.0f", currentMa)} mA" else "Unknown"

    val voltageV = if (voltageMv > 0) voltageMv / 1000f else 0f
    val wattageValue = if (voltageV > 0 && currentMa != 0f) Math.abs(voltageV * (currentMa / 1000f)) else 0f
    val wattageStr = if (wattageValue > 0f) String.format("%.1f W", wattageValue) else "Unknown"

    val isCharging = statusCode == BatteryManager.BATTERY_STATUS_CHARGING || statusCode == BatteryManager.BATTERY_STATUS_FULL

    // Basic calculation for time remaining/time to full based on current wattage/drain (highly simplified)
    // Assume 4000mAh typical capacity for simple math if unknown
    val capacityPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val chargeCounterUaH = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    val totalCapacityMah = if (chargeCounterUaH > 0 && capacityPercent > 0) {
        (chargeCounterUaH / 1000f) * (100f / capacityPercent)
    } else {
        5000f // fallback assumed
    }

    var timeToFull = "Unknown"
    var timeToEmpty = "Unknown"

    if (currentMa != 0f) {
        if (isCharging && percentage < 100) {
            val remainingMah = totalCapacityMah * ((100 - percentage) / 100f)
            val hours = remainingMah / Math.abs(currentMa)
            if (hours in 0.01..24.0) {
                val mins = (hours * 60).roundToInt()
                timeToFull = "~${mins / 60}h ${mins % 60}m until 100%"
            }
        } else if (!isCharging && percentage > 0) {
            val availableMah = totalCapacityMah * (percentage / 100f)
            val hours = availableMah / Math.abs(currentMa)
            if (hours in 0.01..200.0) {
                val mins = (hours * 60).roundToInt()
                timeToEmpty = "~${mins / 60}h ${mins % 60}m remaining"
            }
        }
    }

    return BatteryUiState(
        percentage = percentage,
        status = statusCode.toBatteryStatus(),
        health = healthCode.toBatteryHealth(),
        temperature = if (tempRaw != Int.MIN_VALUE) String.format("%.1f°C", tempRaw / 10f) else "Unknown",
        technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() } ?: "Unknown",
        voltage = if (voltageMv > 0) "${voltageMv} mV" else "Unknown",
        current = currentStr,
        wattage = wattageStr,
        isCharging = isCharging,
        timeToFullStr = timeToFull,
        timeToEmptyStr = timeToEmpty
    )
}

private fun Int.toBatteryStatus(): String = when (this) {
    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
    BatteryManager.BATTERY_STATUS_FULL -> "Full"
    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
    else -> "Unknown"
}

private fun Int.toBatteryHealth(): String = when (this) {
    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
    BatteryManager.BATTERY_HEALTH_DEAD -> "Critical"
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
    else -> "Unknown"
}

package dev.sayanthrock.batteryrock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/**
 * Small MVVM state holder for Android battery data.
 * The Compose screen calls start() and stop() from DisposableEffect so the receiver is registered
 * only while the UI is visible, avoiding leaked receivers and pointless background work.
 */
class BatteryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null

    fun start(context: Context) {
        if (batteryReceiver != null) return

        val appContext = context.applicationContext
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        appContext.registerReceiver(null, filter)?.let { stickyIntent ->
            _uiState.value = stickyIntent.toBatteryUiState()
        }

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    _uiState.value = intent.toBatteryUiState()
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
    val isCharging: Boolean = false,
)

private fun Intent.toBatteryUiState(): BatteryUiState {
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

    return BatteryUiState(
        percentage = percentage,
        status = statusCode.toBatteryStatus(),
        health = healthCode.toBatteryHealth(),
        temperature = if (tempRaw != Int.MIN_VALUE) String.format("%.1f°C", tempRaw / 10f) else "Unknown",
        technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() } ?: "Unknown",
        voltage = if (voltageMv > 0) "${voltageMv} mV" else "Unknown",
        isCharging = statusCode == BatteryManager.BATTERY_STATUS_CHARGING || statusCode == BatteryManager.BATTERY_STATUS_FULL,
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

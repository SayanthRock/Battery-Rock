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
import kotlinx.coroutines.flow.update

/**
 * Small MVVM state holder for Android battery data.
 */
class BatteryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null

    fun start(context: Context) {
        if (batteryReceiver != null) return

        val appContext = context.applicationContext

        // Initial load of config and performance snapshot
        val config = BatteryRockConfigStore.read(appContext)
        val perfSnapshot = DeviceStatusReader.readPerformanceLevel(appContext)
        val initialBatteryHealth = DeviceStatusReader.readBatteryHealth(appContext)

        _uiState.update { currentState ->
            currentState.copy(
                config = config,
                performanceSnapshot = perfSnapshot,
                batteryHealthSnapshot = initialBatteryHealth
            )
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    // Re-read battery health because it depends on the broadcast intent.
                    // Wait, DeviceStatusReader.readBatteryHealth registers its own receiver to get sticky intent.
                    // To be efficient, let's just pass the intent to it, but it doesn't take an intent right now.
                    // Let's modify DeviceStatusReader to take an intent, or we can just call readBatteryHealth which fetches the sticky intent.
                    _uiState.update { currentState ->
                        currentState.copy(
                            batteryHealthSnapshot = DeviceStatusReader.readBatteryHealth(appContext, intent)
                        )
                    }
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

    fun updateConfig(context: Context, update: (BatteryRockConfig) -> BatteryRockConfig) {
        val appContext = context.applicationContext
        _uiState.update { currentState ->
            val newConfig = update(currentState.config)
            BatteryRockConfigStore.save(appContext, newConfig)
            currentState.copy(config = newConfig)
        }
    }

    override fun onCleared() {
        batteryReceiver = null
        super.onCleared()
    }
}

data class BatteryUiState(
    val batteryHealthSnapshot: BatteryHealthSnapshot = BatteryHealthSnapshot(
        levelPercent = 0,
        statusLabel = "Unknown",
        healthLabel = "Unknown",
        temperatureC = "Unknown",
        voltageMv = 0,
        capacityEstimate = "Unknown",
        powerSource = "Unknown",
        summary = "Loading..."
    ),
    val performanceSnapshot: DevicePerformanceSnapshot = DevicePerformanceSnapshot(
        levelLabel = "Unknown",
        score = 0,
        cores = 1,
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
        androidVersion = "Unknown",
        summary = "Loading..."
    ),
    val config: BatteryRockConfig = BatteryRockConfig()
)

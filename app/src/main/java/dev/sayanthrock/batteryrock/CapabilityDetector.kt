package dev.sayanthrock.batteryrock

import android.content.Context
import android.os.BatteryManager
import android.os.Build

enum class CapabilityStatus {
    SUPPORTED,
    PARTIALLY_SUPPORTED,
    ESTIMATED,
    UNSUPPORTED
}

data class DeviceCapabilities(
    val chargingPower: CapabilityStatus,
    val batteryTemperature: CapabilityStatus,
    val cycleCount: CapabilityStatus,
    val fastCharging: CapabilityStatus,
    val exactProtocol: CapabilityStatus,
    val chargeLimit: CapabilityStatus,
    val usageStats: CapabilityStatus
)

object CapabilityDetector {

    fun detectCapabilities(context: Context): DeviceCapabilities {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?

        // Charging Power (Current * Voltage)
        val hasCurrent = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) != Int.MIN_VALUE
        val chargingPowerStatus = if (hasCurrent) CapabilityStatus.SUPPORTED else CapabilityStatus.UNSUPPORTED

        // Battery Temperature is usually standard via broadcast
        val temperatureStatus = CapabilityStatus.SUPPORTED

        // Cycle Count
        val hasCycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) != Int.MIN_VALUE
        } else {
            false
        }
        val cycleCountStatus = if (hasCycleCount) CapabilityStatus.SUPPORTED else CapabilityStatus.UNSUPPORTED

        // Fast Charging
        val fastChargingStatus = CapabilityStatus.ESTIMATED // We estimate based on wattage

        // Exact Protocol
        val exactProtocolStatus = CapabilityStatus.ESTIMATED

        // Charge Limit
        val chargeLimitStatus = CapabilityStatus.UNSUPPORTED // OEM specific, typically unsupported without root

        // Usage Stats
        val usageStatsStatus = CapabilityStatus.SUPPORTED // Assuming permission is granted

        return DeviceCapabilities(
            chargingPower = chargingPowerStatus,
            batteryTemperature = temperatureStatus,
            cycleCount = cycleCountStatus,
            fastCharging = fastChargingStatus,
            exactProtocol = exactProtocolStatus,
            chargeLimit = chargeLimitStatus,
            usageStats = usageStatsStatus
        )
    }
}

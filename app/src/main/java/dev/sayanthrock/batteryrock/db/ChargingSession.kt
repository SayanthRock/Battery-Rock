package dev.sayanthrock.batteryrock.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_sessions")
data class ChargingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val startPercentage: Int,
    val endPercentage: Int? = null,
    val startTemperature: String,
    val endTemperature: String? = null,
    val startVoltage: String,
    val startCurrent: String? = null, // Will fetch from extra capability APIs if available
    val startWattage: String? = null,
    val chargingSource: String,
    val chargingType: String, // E.g. "Fast Charging", "USB-PD", "Normal"
    val maxPowerWatts: Float = 0f,
    val maxTemperature: String? = null,
    val avgPowerWatts: Float = 0f,
    val isFastCharging: Boolean = false
)

@Entity(tableName = "battery_history")
data class BatteryHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val percentage: Int,
    val temperature: String,
    val voltage: String,
    val isCharging: Boolean
)

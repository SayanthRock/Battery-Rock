package dev.sayanthrock.batteryrock

import android.content.Context

data class BatteryRockConfig(
    val batteryMode: String = BatteryRockConfigStore.DEFAULT_BATTERY_MODE,
    val performanceMode: String = BatteryRockConfigStore.DEFAULT_PERFORMANCE_MODE,
    val ramRomMode: String = BatteryRockConfigStore.DEFAULT_RAM_ROM_MODE,
    val refreshRateMode: String = BatteryRockConfigStore.DEFAULT_REFRESH_RATE_MODE,
    val batteryCare80: Boolean = false,
    val lastAppliedAtMillis: Long = 0L,
)

object BatteryRockConfigStore {

    const val DEFAULT_BATTERY_MODE = "Balanced"
    const val DEFAULT_PERFORMANCE_MODE = "Standard"
    const val DEFAULT_RAM_ROM_MODE = "Balanced daily profile"
    const val DEFAULT_REFRESH_RATE_MODE = "Auto-select"

    private const val PREFS = "battery_rock_controls"
    private const val KEY_BATTERY_MODE = "battery_mode"
    private const val KEY_PERFORMANCE_MODE = "performance_mode"
    private const val KEY_RAM_ROM_MODE = "ram_rom_mode"
    private const val KEY_REFRESH_RATE_MODE = "refresh_rate_mode"
    private const val KEY_BATTERY_CARE_80 = "battery_care_80"
    private const val KEY_LAST_APPLIED_AT = "last_applied_at"

    fun read(context: Context): BatteryRockConfig {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return BatteryRockConfig(
            batteryMode = prefs.getString(KEY_BATTERY_MODE, DEFAULT_BATTERY_MODE) ?: DEFAULT_BATTERY_MODE,
            performanceMode = prefs.getString(KEY_PERFORMANCE_MODE, DEFAULT_PERFORMANCE_MODE) ?: DEFAULT_PERFORMANCE_MODE,
            ramRomMode = prefs.getString(KEY_RAM_ROM_MODE, DEFAULT_RAM_ROM_MODE) ?: DEFAULT_RAM_ROM_MODE,
            refreshRateMode = prefs.getString(KEY_REFRESH_RATE_MODE, DEFAULT_REFRESH_RATE_MODE) ?: DEFAULT_REFRESH_RATE_MODE,
            batteryCare80 = prefs.getBoolean(KEY_BATTERY_CARE_80, false),
            lastAppliedAtMillis = prefs.getLong(KEY_LAST_APPLIED_AT, 0L),
        )
    }

    fun save(context: Context, config: BatteryRockConfig) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BATTERY_MODE, config.batteryMode)
            .putString(KEY_PERFORMANCE_MODE, config.performanceMode)
            .putString(KEY_RAM_ROM_MODE, config.ramRomMode)
            .putString(KEY_REFRESH_RATE_MODE, config.refreshRateMode)
            .putBoolean(KEY_BATTERY_CARE_80, config.batteryCare80)
            .putLong(KEY_LAST_APPLIED_AT, config.lastAppliedAtMillis)
            .apply()
    }

    fun reset(context: Context): BatteryRockConfig {
        val config = BatteryRockConfig()
        save(context, config)
        return config
    }
}

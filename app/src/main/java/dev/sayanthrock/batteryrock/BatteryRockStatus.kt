package dev.sayanthrock.batteryrock

/**
 * APK-safe module status bridge.
 *
 * This class intentionally has no Xposed imports. The normal launcher process
 * can load it safely even when the Xposed API is not present on the app classpath.
 */
object BatteryRockStatus {
    @JvmStatic
    fun isModuleActive(): Boolean = false
}

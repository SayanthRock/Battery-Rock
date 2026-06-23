package dev.sayanthrock.batteryrock.hooks

import android.os.Build

object RomAdaptiveEngine {

    enum class RomType {
        AOSP,
        OXYGEN_OS,
        COLOR_OS,
        REALME_UI,
        MIUI,
        ONE_UI,
        UNKNOWN
    }

    data class RomProfile(
        val romType: RomType,
        val allowAggressiveHooks: Boolean,
        val allowTelemetryHooks: Boolean,
        val allowFrameworkHooks: Boolean,
        val safeMode: Boolean
    )

    fun detect(): RomProfile {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()

        return when {
            manufacturer.contains("oneplus") || brand.contains("oneplus") -> RomProfile(
                RomType.OXYGEN_OS,
                allowAggressiveHooks = true,
                allowTelemetryHooks = true,
                allowFrameworkHooks = true,
                safeMode = false
            )

            manufacturer.contains("oppo") || brand.contains("coloros") -> RomProfile(
                RomType.COLOR_OS,
                allowAggressiveHooks = true,
                allowTelemetryHooks = true,
                allowFrameworkHooks = true,
                safeMode = true
            )

            manufacturer.contains("realme") -> RomProfile(
                RomType.REALME_UI,
                allowAggressiveHooks = true,
                allowTelemetryHooks = true,
                allowFrameworkHooks = true,
                safeMode = true
            )

            manufacturer.contains("xiaomi") -> RomProfile(
                RomType.MIUI,
                allowAggressiveHooks = false,
                allowTelemetryHooks = true,
                allowFrameworkHooks = false,
                safeMode = true
            )

            manufacturer.contains("samsung") -> RomProfile(
                RomType.ONE_UI,
                allowAggressiveHooks = false,
                allowTelemetryHooks = false,
                allowFrameworkHooks = false,
                safeMode = true
            )

            else -> RomProfile(
                RomType.AOSP,
                allowAggressiveHooks = true,
                allowTelemetryHooks = true,
                allowFrameworkHooks = true,
                safeMode = false
            )
        }
    }
}
package dev.sayanthrock.batteryrock

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.hooks.FrameworkHook
import dev.sayanthrock.batteryrock.hooks.TelemetryKiller
import dev.sayanthrock.batteryrock.hooks.WakelockGuard

/**
 * Battery-Rock LSPosed Module
 *
 * Entry point declared in assets/xposed_init.
 * Routed to [FrameworkHook], [TelemetryKiller], and [WakelockGuard]
 * depending on which process is loading.
 *
 * Author : sayanthrock
 * Target : OPPO / Realme / OnePlus (ColorOS 12–16, OxygenOS 12–16)
 */
class BatteryRockInit : IXposedHookLoadPackage {

    companion object {
        const val TAG = "BatteryRock"

        /** Packages Battery-Rock actively hooks to suppress telemetry / drain. */
        val TELEMETRY_PACKAGES = setOf(
            // OPLUS core telemetry
            "com.oplus.onetrace",
            "com.oplus.appsense",
            "com.oplus.powermonitor",
            "com.oplus.logkit",
            "com.oplus.olc",
            "com.debug.loggerui",
            // OPLUS system services
            "com.oplus.sau",
            "com.oplus.romupdate",
            "com.nearme.instant.platform",
            "com.oplus.appplatform",
            "com.oplus.ocrservice",
            "com.coloros.ocrservice",
            // Realme
            "com.realme.systemservice",
            "com.realme.statisticsservice",
            // OnePlus / OxygenOS
            "com.oneplus.statistics",
        )

        /**
         * Hooked at runtime to return [true] when the module is active.
         * The UI reads this to show the "Active" badge.
         */
        @JvmStatic
        fun isModuleActive(): Boolean = false // replaced → true by hook below
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            when {
                // ── Self-check hook (UI can verify module is loaded) ─────────
                lpparam.packageName == BuildConfig.APPLICATION_ID -> {
                    XposedHelpers.findAndHookMethod(
                        BatteryRockInit::class.java.name,
                        lpparam.classLoader,
                        "isModuleActive",
                        object : XC_MethodReplacement() {
                            override fun replaceHookedMethod(param: MethodHookParam) = true
                        }
                    )
                }

                // ── System Server (android process) ─────────────────────────
                lpparam.packageName == "android" -> {
                    XposedBridge.log("$TAG: Loading FrameworkHook in system_server")
                    FrameworkHook.hook(lpparam)
                }

                // ── Telemetry / Drain packages ───────────────────────────────
                lpparam.packageName in TELEMETRY_PACKAGES -> {
                    XposedBridge.log("$TAG: Loading TelemetryKiller in ${lpparam.packageName}")
                    TelemetryKiller.hook(lpparam)
                    WakelockGuard.hook(lpparam)
                }

                // ── SystemUI – cap wakelock abuse ────────────────────────────
                lpparam.packageName == "com.android.systemui" -> {
                    XposedBridge.log("$TAG: Loading WakelockGuard in SystemUI")
                    WakelockGuard.hook(lpparam)
                }
            }
        } catch (t: Throwable) {
            XposedBridge.log("$TAG: Critical error in ${lpparam.packageName} – ${t.message}")
        }
    }
}

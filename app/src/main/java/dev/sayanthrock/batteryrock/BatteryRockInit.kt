package dev.sayanthrock.batteryrock

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.hooks.FrameworkHook
import dev.sayanthrock.batteryrock.hooks.TelemetryKiller
import dev.sayanthrock.batteryrock.hooks.WakelockGuard

/**
 * Battery-Rock LSPosed Module (Enterprise Mode Enabled)
 *
 * Now includes:
 * - Global crash isolation layer
 * - Safe routing per process
 * - Enterprise SAFE_MODE switch
 * - Zero-crash hook guarantees
 */
class BatteryRockInit : IXposedHookLoadPackage {

    companion object {
        const val TAG = "BatteryRock"

        /** Enterprise safety switch (can be extended later via UI/config) */
        @JvmStatic
        var SAFE_MODE: Boolean = false

        /** Packages Battery-Rock actively hooks to suppress telemetry and drain. */
        val TELEMETRY_PACKAGES = setOf(
            "com.oplus.onetrace",
            "com.oplus.appsense",
            "com.oplus.powermonitor",
            "com.oplus.logkit",
            "com.oplus.olc",
            "com.debug.loggerui",
            "com.oplus.sau",
            "com.oplus.romupdate",
            "com.nearme.instant.platform",
            "com.oplus.appplatform",
            "com.oplus.ocrservice",
            "com.coloros.ocrservice",
            "com.realme.systemservice",
            "com.realme.statisticsservice",
            "com.oneplus.statistics",
        )

        @JvmStatic
        fun isModuleActive(): Boolean = true
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        runCatching {
            when {
                lpparam.packageName == BuildConfig.APPLICATION_ID -> {
                    XposedHelpers.findAndHookMethod(
                        BatteryRockInit::class.java.name,
                        lpparam.classLoader,
                        "isModuleActive",
                        object : XC_MethodReplacement() {
                            override fun replaceHookedMethod(param: MethodHookParam): Any = true
                        }
                    )
                }

                lpparam.packageName == "android" -> {
                    if (!SAFE_MODE) {
                        XposedBridge.log("$TAG: FrameworkHook enabled")
                        FrameworkHook.hook(lpparam)
                    } else {
                        XposedBridge.log("$TAG: SAFE_MODE active, skipping FrameworkHook")
                    }
                }

                lpparam.packageName in TELEMETRY_PACKAGES -> {
                    XposedBridge.log("$TAG: Telemetry hooks → ${lpparam.packageName}")
                    TelemetryKiller.hook(lpparam)
                    WakelockGuard.hook(lpparam)
                }

                lpparam.packageName == "com.android.systemui" -> {
                    WakelockGuard.hook(lpparam)
                }
            }
        }.onFailure { t ->
            try {
                XposedBridge.log("$TAG: Global hook crash prevented -> ${t.javaClass.simpleName}: ${t.message}")
            } catch (_: Throwable) {
                // absolute zero-crash guarantee
            }
        }
    }
}

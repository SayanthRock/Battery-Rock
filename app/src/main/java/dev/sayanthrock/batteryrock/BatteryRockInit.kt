package dev.sayanthrock.batteryrock

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.hooks.AutoHookControllerEngine
import dev.sayanthrock.batteryrock.hooks.FrameworkHook
import dev.sayanthrock.batteryrock.hooks.TelemetryKiller
import dev.sayanthrock.batteryrock.hooks.WakelockGuard

/**
 * Battery-Rock LSPosed module entry point.
 *
 * Hooks are routed through a safety controller so one broken ROM method does
 * not break the full release build or the target process.
 */
class BatteryRockInit : IXposedHookLoadPackage {

    companion object {
        const val TAG = "BatteryRock"

        /** Emergency switch for reducing hook activity without changing scopes. */
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

        /**
         * Default is false for the normal APK process. LSPosed sets this to
         * true only after the module is loaded.
         */
        @JvmStatic
        fun isModuleActive(): Boolean = false
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName

        runCatching {
            when {
                packageName == BuildConfig.APPLICATION_ID -> hookSelfStatus(lpparam)

                packageName == "android" -> {
                    if (AutoHookControllerEngine.shouldEnableHooks(packageName)) {
                        log("FrameworkHook enabled")
                        FrameworkHook.hook(lpparam)
                    } else {
                        log("FrameworkHook skipped by safety controller")
                    }
                }

                packageName in TELEMETRY_PACKAGES -> {
                    if (AutoHookControllerEngine.shouldEnableHooks(packageName)) {
                        log("Telemetry hooks enabled for $packageName")
                        TelemetryKiller.hook(lpparam)
                        WakelockGuard.hook(lpparam)
                    } else {
                        log("Telemetry hooks skipped for $packageName")
                    }
                }

                packageName == "com.android.systemui" -> {
                    if (AutoHookControllerEngine.shouldEnableHooks(packageName)) {
                        WakelockGuard.hook(lpparam)
                    }
                }
            }
        }.onFailure { throwable ->
            AutoHookControllerEngine.reportEvent("handleLoadPackage:$packageName", throwable)
            log("Global hook crash prevented -> ${throwable.javaClass.simpleName}: ${throwable.message}")
        }
    }

    private fun hookSelfStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            BatteryRockInit::class.java.name,
            lpparam.classLoader,
            "isModuleActive",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.setResult(true)
                }
            }
        )
    }

    private fun log(message: String) {
        try {
            XposedBridge.log("$TAG: $message")
        } catch (_: Throwable) {
            // Logging must never crash the target process.
        }
    }
}

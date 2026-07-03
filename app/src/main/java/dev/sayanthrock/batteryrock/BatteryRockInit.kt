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
 * Battery-Rock module entry point.
 *
 * The launcher UI must not directly load this class because it depends on the
 * module API. MainActivity uses BatteryRockStatus instead, which is safe in the
 * normal APK process.
 */
class BatteryRockInit : IXposedHookLoadPackage {

    companion object {
        const val TAG = "BatteryRock"

        @JvmStatic
        var SAFE_MODE: Boolean = false

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
            BatteryRockStatus::class.java.name,
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

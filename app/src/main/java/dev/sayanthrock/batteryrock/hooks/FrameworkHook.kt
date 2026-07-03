package dev.sayanthrock.batteryrock.hooks

import android.app.job.JobScheduler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * FrameworkHook provides production-hardened LSPosed hooks.
 *
 * Every hook is isolated so a ROM method change skips only that hook instead of
 * crashing system_server, because apparently one tiny method name can ruin an
 * entire day.
 */
object FrameworkHook {

    private val TAG = "${BatteryRockInit.TAG}/Framework"
    private const val MIN_ALARM_INTERVAL_MS = 30 * 60 * 1_000L

    private val lastAlarmTime = mutableMapOf<String, Long>()

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        tryHook("JobSchedulerService init") {
            hookJobScheduler(lpparam.classLoader)
        }

        tryHook("AlarmManagerService init") {
            hookAlarmManager(lpparam.classLoader)
        }
    }

    private fun hookJobScheduler(classLoader: ClassLoader) {
        val serviceClass = XposedHelpers.findClass(
            "com.android.server.job.JobSchedulerService",
            classLoader
        )

        XposedBridge.hookAllMethods(
            serviceClass,
            "scheduleAsPackage",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        val packageName = param.args.findTelemetryPackage() ?: return
                        XposedBridge.log("$TAG: Dropped job for $packageName")
                        param.setResult(JobScheduler.RESULT_FAILURE)
                    }.onFailure { AutoHookControllerEngine.reportEvent("scheduleAsPackage", it) }
                }
            }
        )

        XposedBridge.hookAllMethods(
            serviceClass,
            "schedule",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        val packageName = param.args.findTelemetryPackage() ?: return
                        XposedBridge.log("$TAG: Dropped fallback job for $packageName")
                        param.setResult(JobScheduler.RESULT_FAILURE)
                    }.onFailure { AutoHookControllerEngine.reportEvent("schedule", it) }
                }
            }
        )
    }

    private fun hookAlarmManager(classLoader: ClassLoader) {
        val serviceClass = XposedHelpers.findClass(
            "com.android.server.alarm.AlarmManagerService",
            classLoader
        )

        XposedBridge.hookAllMethods(
            serviceClass,
            "setImpl",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        val packageName = param.args.findTelemetryPackage() ?: return
                        throttleAlarm(param, packageName)
                    }.onFailure { AutoHookControllerEngine.reportEvent("setImpl", it) }
                }
            }
        )
    }

    private fun throttleAlarm(param: MethodHookParam, packageName: String) {
        val now = System.currentTimeMillis()
        val last = synchronized(lastAlarmTime) { lastAlarmTime[packageName] ?: 0L }

        if (now - last < MIN_ALARM_INTERVAL_MS) {
            XposedBridge.log("$TAG: Throttled alarm for $packageName")
            param.setResult(null)
        } else {
            synchronized(lastAlarmTime) { lastAlarmTime[packageName] = now }
        }
    }

    private fun Array<*>.findTelemetryPackage(): String? {
        return firstOrNull { value ->
            value is String && value in BatteryRockInit.TELEMETRY_PACKAGES
        } as? String
    }

    private inline fun tryHook(label: String, block: () -> Unit) {
        try {
            block()
        } catch (throwable: Throwable) {
            AutoHookControllerEngine.reportEvent(label, throwable)
            log("skipped $label -> ${throwable.javaClass.simpleName}: ${throwable.message}")
        }
    }

    private fun log(message: String) {
        try {
            XposedBridge.log("$TAG: $message")
        } catch (_: Throwable) {
            // Never crash system_server from logging.
        }
    }
}

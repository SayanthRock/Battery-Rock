package dev.sayanthrock.batteryrock.hooks

import android.app.job.JobScheduler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * FrameworkHook — production hardened LSPosed hooks.
 * Goal: ZERO CRASH mode (never break system_server if hooks fail).
 */
object FrameworkHook {

    private val TAG = "${BatteryRockInit.TAG}/Framework"

    private const val MIN_ALARM_INTERVAL_MS = 30 * 60 * 1_000L
    private val lastAlarmTime = mutableMapOf<String, Long>()

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        runCatching {
            hookJobScheduler(lpparam.classLoader)
        }.onFailure {
            logError("JobScheduler init failed", it)
        }

        runCatching {
            hookAlarmManager(lpparam.classLoader)
        }.onFailure {
            logError("AlarmManager init failed", it)
        }
    }

    private fun hookJobScheduler(classLoader: ClassLoader) {
        runCatching {
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
                            val pkg = param.args.findTelemetryPackage() ?: return
                            XposedBridge.log("$TAG: Dropped job for $pkg")
                            param.result = JobScheduler.RESULT_FAILURE
                        }.onFailure { logError("scheduleAsPackage hook failed", it) }
                    }
                }
            )

            XposedBridge.hookAllMethods(
                serviceClass,
                "schedule",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val pkg = param.args.findTelemetryPackage() ?: return
                            XposedBridge.log("$TAG: Dropped fallback job for $pkg")
                            param.result = JobScheduler.RESULT_FAILURE
                        }.onFailure { logError("schedule hook failed", it) }
                    }
                }
            )

        }.onFailure {
            logError("JobSchedulerService hook failed", it)
        }
    }

    private fun hookAlarmManager(classLoader: ClassLoader) {
        runCatching {
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
                            val pkg = param.args.findTelemetryPackage() ?: return
                            throttleAlarm(param, pkg)
                        }.onFailure { logError("setImpl hook failed", it) }
                    }
                }
            )

        }.onFailure {
            logError("AlarmManagerService hook failed", it)
        }
    }

    private fun throttleAlarm(param: MethodHookParam, pkg: String) {
        val now = System.currentTimeMillis()
        val last = synchronized(lastAlarmTime) { lastAlarmTime[pkg] ?: 0L }

        if (now - last < MIN_ALARM_INTERVAL_MS) {
            XposedBridge.log("$TAG: Throttled alarm for $pkg")
            param.result = null
        } else {
            synchronized(lastAlarmTime) { lastAlarmTime[pkg] = now }
        }
    }

    private fun Array<*>.findTelemetryPackage(): String? {
        return firstOrNull { it is String && BatteryRockInit.TELEMETRY_PACKAGES.contains(it) } as? String
    }

    private fun logError(tag: String, t: Throwable) {
        try {
            XposedBridge.log("$TAG: ERROR $tag -> ${t.javaClass.simpleName}: ${t.message}")
        } catch (_: Throwable) {
            // absolute safety: never crash system_server
        }
    }
}
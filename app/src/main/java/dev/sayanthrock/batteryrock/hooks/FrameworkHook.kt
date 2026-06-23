package dev.sayanthrock.batteryrock.hooks

import android.app.job.JobScheduler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * FrameworkHook — loaded inside the Android framework / system_server process.
 *
 * The hook code intentionally uses hookAllMethods for OEM framework methods.
 * OPPO, Realme, OnePlus, and Android releases often change hidden method
 * signatures, so exact signatures can fail at build time or runtime.
 */
object FrameworkHook {

    private val TAG = "${BatteryRockInit.TAG}/Framework"

    /** Minimum alarm interval enforced for telemetry packages: 30 minutes. */
    private const val MIN_ALARM_INTERVAL_MS = 30 * 60 * 1_000L

    /** Per-package last alarm timestamp used for throttling. */
    private val lastAlarmTime = mutableMapOf<String, Long>()

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookJobScheduler(lpparam.classLoader)
        hookAlarmManager(lpparam.classLoader)
    }

    // ─── Job Scheduler ────────────────────────────────────────────────────────

    private fun hookJobScheduler(classLoader: ClassLoader) {
        tryHook("JobSchedulerService.scheduleAsPackage") {
            val serviceClass = XposedHelpers.findClass(
                "com.android.server.job.JobSchedulerService",
                classLoader
            )

            XposedBridge.hookAllMethods(
                serviceClass,
                "scheduleAsPackage",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val pkg = param.args.findTelemetryPackage() ?: return
                        XposedBridge.log("$TAG: Dropped job for $pkg")
                        param.result = JobScheduler.RESULT_FAILURE
                    }
                }
            )
        }

        tryHook("JobSchedulerService.schedule") {
            val serviceClass = XposedHelpers.findClass(
                "com.android.server.job.JobSchedulerService",
                classLoader
            )

            XposedBridge.hookAllMethods(
                serviceClass,
                "schedule",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val pkg = param.args.findTelemetryPackage() ?: return
                        XposedBridge.log("$TAG: Dropped fallback job for $pkg")
                        param.result = JobScheduler.RESULT_FAILURE
                    }
                }
            )
        }
    }

    // ─── Alarm Manager ────────────────────────────────────────────────────────

    private fun hookAlarmManager(classLoader: ClassLoader) {
        tryHook("AlarmManagerService.setImpl") {
            val serviceClass = XposedHelpers.findClass(
                "com.android.server.alarm.AlarmManagerService",
                classLoader
            )

            XposedBridge.hookAllMethods(
                serviceClass,
                "setImpl",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val pkg = param.args.findTelemetryPackage() ?: return
                        throttleAlarm(param, pkg)
                    }
                }
            )
        }
    }

    private fun throttleAlarm(param: MethodHookParam, pkg: String) {
        val now = System.currentTimeMillis()
        val last = synchronized(lastAlarmTime) { lastAlarmTime[pkg] ?: 0L }

        if (now - last < MIN_ALARM_INTERVAL_MS) {
            XposedBridge.log(
                "$TAG: Throttled alarm for $pkg " +
                    "interval < ${MIN_ALARM_INTERVAL_MS / 60_000}m"
            )
            param.result = null
        } else {
            synchronized(lastAlarmTime) { lastAlarmTime[pkg] = now }
        }
    }

    private fun Array<Any?>.findTelemetryPackage(): String? =
        firstOrNull { arg ->
            arg is String && arg in BatteryRockInit.TELEMETRY_PACKAGES
        } as? String

    // ─── Utility ──────────────────────────────────────────────────────────────

    private inline fun tryHook(label: String, block: () -> Unit) {
        try {
            block()
            XposedBridge.log("$TAG: ✓ Hooked $label")
        } catch (t: Throwable) {
            XposedBridge.log("$TAG: ✗ $label – ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}

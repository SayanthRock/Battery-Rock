package dev.sayanthrock.batteryrock.hooks

import android.app.job.JobScheduler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * FrameworkHook — loaded inside the **android** (system_server) process.
 *
 * Responsibilities
 * ────────────────
 * 1. Block background [JobScheduler] jobs submitted by OPLUS telemetry packages.
 * 2. Throttle excessive [AlarmManager] alarms from telemetry callers.
 *
 * Both hooks are wrapped in broad try-catch blocks so a failed hook never
 * crashes system_server (which would bootloop the device).
 */
object FrameworkHook {

    private val TAG = "${BatteryRockInit.TAG}/Framework"

    /** Minimum alarm interval enforced for telemetry packages (30 minutes). */
    private const val MIN_ALARM_INTERVAL_MS = 30 * 60 * 1_000L

    /** Per-package last alarm timestamp – used for throttling. */
    private val lastAlarmTime = mutableMapOf<String, Long>()

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookJobScheduler(lpparam.classLoader)
        hookAlarmManager(lpparam.classLoader)
    }

    // ─── Job Scheduler ────────────────────────────────────────────────────────

    /**
     * Hooks [com.android.server.job.JobSchedulerService.scheduleAsPackage] which
     * is the internal path for all job scheduling in Android 12+.
     * If the calling package is in our telemetry list, the job is silently dropped.
     */
    private fun hookJobScheduler(classLoader: ClassLoader) {
        // Primary hook – Android 12-16 signature
        tryHook("JobSchedulerService.scheduleAsPackage") {
            XposedHelpers.findAndHookMethod(
                "com.android.server.job.JobSchedulerService",
                classLoader,
                "scheduleAsPackage",
                android.app.job.JobInfo::class.java, // jobInfo
                String::class.java,                  // packageName
                Int::class.java,                     // userId
                String::class.java,                  // tag
                Int::class.java,                     // uidBias
                String::class.java,                  // namespace
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val pkg = param.args[1] as? String ?: return
                        if (pkg in BatteryRockInit.TELEMETRY_PACKAGES) {
                            XposedBridge.log("$TAG: Dropped job for $pkg")
                            param.result = JobScheduler.RESULT_FAILURE
                        }
                    }
                }
            )
        }

        // Fallback – older AOSP / some OEM variants
        tryHook("JobSchedulerService.schedule (2-arg)") {
            XposedHelpers.findAndHookMethod(
                "com.android.server.job.JobSchedulerService",
                classLoader,
                "schedule",
                android.app.job.JobInfo::class.java,
                Int::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // We can only access jobInfo here; caller identity is not
                        // easily available without Binder IPC – best-effort only.
                    }
                }
            )
        }
    }

    // ─── Alarm Manager ────────────────────────────────────────────────────────

    /**
     * Hooks `AlarmManagerService.setImpl` to rate-limit alarms from telemetry
     * packages. Alarms within [MIN_ALARM_INTERVAL_MS] of the last one are dropped.
     *
     * The method signature varies by Android version; we try two known signatures.
     */
    private fun hookAlarmManager(classLoader: ClassLoader) {
        // Android 14 / 15 / 16 signature
        tryHook("AlarmManagerService.setImpl (Android 14+)") {
            XposedHelpers.findAndHookMethod(
                "com.android.server.alarm.AlarmManagerService",
                classLoader,
                "setImpl",
                Int::class.java,       // type
                Long::class.java,      // triggerAtTime
                Long::class.java,      // triggerElapsed
                Long::class.java,      // windowLength
                Long::class.java,      // maxWhen
                Int::class.java,       // flags
                android.app.PendingIntent::class.java, // operation
                android.app.IAlarmListener::class.java, // directReceiver
                String::class.java,    // listenerTag
                android.os.WorkSource::class.java, // workSource
                Int::class.java,       // callingUid
                String::class.java,    // callingPackage
                Int::class.java,       // callingUserId
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        throttleAlarm(param, pkgArgIndex = 11)
                    }
                }
            )
        }

        // Android 12 / 13 signature (fewer args)
        tryHook("AlarmManagerService.setImpl (Android 12-13)") {
            XposedHelpers.findAndHookMethod(
                "com.android.server.alarm.AlarmManagerService",
                classLoader,
                "setImpl",
                Int::class.java,
                Long::class.java,
                Long::class.java,
                Long::class.java,
                Int::class.java,
                android.app.PendingIntent::class.java,
                android.app.IAlarmListener::class.java,
                String::class.java,
                android.os.WorkSource::class.java,
                Int::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        throttleAlarm(param, pkgArgIndex = 10)
                    }
                }
            )
        }
    }

    private fun throttleAlarm(param: XC_MethodHook.MethodHookParam, pkgArgIndex: Int) {
        val pkg = param.args.getOrNull(pkgArgIndex) as? String ?: return
        if (pkg !in BatteryRockInit.TELEMETRY_PACKAGES) return

        val now = System.currentTimeMillis()
        val last = synchronized(lastAlarmTime) { lastAlarmTime[pkg] ?: 0L }
        if (now - last < MIN_ALARM_INTERVAL_MS) {
            XposedBridge.log("$TAG: Throttled alarm for $pkg (interval < ${MIN_ALARM_INTERVAL_MS / 60_000}m)")
            param.result = null // cancel alarm
        } else {
            synchronized(lastAlarmTime) { lastAlarmTime[pkg] = now }
        }
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    private inline fun tryHook(label: String, block: () -> Unit) {
        try {
            block()
            XposedBridge.log("$TAG: ✓ Hooked $label")
        } catch (t: Throwable) {
            // Not a crash – method may not exist on this Android/OEM version
            XposedBridge.log("$TAG: ✗ $label – ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}

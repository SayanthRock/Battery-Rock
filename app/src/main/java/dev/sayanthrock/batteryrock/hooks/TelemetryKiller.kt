package dev.sayanthrock.batteryrock.hooks

import android.app.Service
import android.content.Intent
import android.net.Uri
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * TelemetryKiller — loaded inside each OPLUS **telemetry / drain package** process.
 *
 * Hook surfaces
 * ─────────────
 * • [Service.onStartCommand] → returns [Service.START_NOT_STICKY] immediately.
 *   Services are technically started but perform no work and are GC-able quickly.
 * • [JobScheduler.schedule]  → returns RESULT_FAILURE inside the app process
 *   (belt-and-suspenders alongside [FrameworkHook]).
 * • [java.net.URL.openConnection] → throws [java.io.IOException], cutting off
 *   outbound telemetry/HTTP traffic.
 * • [android.content.ContentResolver.insert] → drops telemetry inserts into
 *   OPLUS content providers.
 *
 * Design notes
 * ────────────
 * We deliberately **do not** hook [android.app.Service.onCreate] / [android.app.Application.onCreate]
 * with a full replacement — doing so would leave the object in an uninitialised state
 * and cause [NullPointerException] chains. Blocking [onStartCommand] is sufficient
 * to prevent any useful work.
 */
object TelemetryKiller {

    private val TAG = "${BatteryRockInit.TAG}/TelemetryKiller"

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val pkg = lpparam.packageName
        blockServiceStarts(pkg, lpparam.classLoader)
        blockJobScheduling(pkg, lpparam.classLoader)
        blockNetworkAccess(pkg, lpparam.classLoader)
        blockContentProviderInserts(pkg, lpparam.classLoader)
    }

    // ─── Service ──────────────────────────────────────────────────────────────

    /**
     * Intercepts [Service.onStartCommand] so the service returns immediately
     * without executing its payload.
     */
    private fun blockServiceStarts(pkg: String, classLoader: ClassLoader) {
        tryHook("Service.onStartCommand ($pkg)") {
            XposedHelpers.findAndHookMethod(
                "android.app.Service",
                classLoader,
                "onStartCommand",
                Intent::class.java,
                Int::class.java,
                Int::class.java,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any {
                        XposedBridge.log(
                            "$TAG: Blocked Service.onStartCommand " +
                                    "[${param.thisObject::class.java.simpleName}] in $pkg"
                        )
                        return Service.START_NOT_STICKY
                    }
                }
            )
        }
    }

    // ─── JobScheduler ─────────────────────────────────────────────────────────

    /**
     * Blocks the app-side [JobScheduler] proxy so no background jobs are scheduled.
     * Works in tandem with [FrameworkHook] which blocks at the system_server level.
     */
    private fun blockJobScheduling(pkg: String, classLoader: ClassLoader) {
        tryHook("JobSchedulerImpl.schedule ($pkg)") {
            XposedHelpers.findAndHookMethod(
                "android.app.JobSchedulerImpl",
                classLoader,
                "schedule",
                android.app.job.JobInfo::class.java,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any {
                        XposedBridge.log("$TAG: Blocked job scheduling in $pkg")
                        return android.app.job.JobScheduler.RESULT_FAILURE
                    }
                }
            )
        }
    }

    // ─── Network Access ───────────────────────────────────────────────────────

    /**
     * Blocks all HTTP/HTTPS connections originating from this process by
     * throwing an [IOException] from [java.net.URL.openConnection].
     *
     * This cuts telemetry beacons, analytics pings, crash-log uploads, etc.
     * without needing a firewall rule.
     */
    private fun blockNetworkAccess(pkg: String, classLoader: ClassLoader) {
        // java.net.URL – standard JVM HTTP
        tryHook("URL.openConnection ($pkg)") {
            XposedHelpers.findAndHookMethod(
                "java.net.URL",
                classLoader,
                "openConnection",
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Nothing {
                        XposedBridge.log("$TAG: Blocked HTTP openConnection in $pkg")
                        throw java.io.IOException("Battery-Rock: network blocked for $pkg")
                    }
                }
            )
        }

        // OkHttp (commonly bundled in OPLUS apps) – hook newCall entry point
        tryHook("OkHttpClient.newCall ($pkg)") {
            XposedHelpers.findAndHookMethod(
                "okhttp3.OkHttpClient",
                classLoader,
                "newCall",
                XposedHelpers.findClass("okhttp3.Request", classLoader),
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Nothing {
                        XposedBridge.log("$TAG: Blocked OkHttp call in $pkg")
                        throw java.io.IOException("Battery-Rock: network blocked for $pkg")
                    }
                }
            )
        }
    }

    // ─── ContentProvider ──────────────────────────────────────────────────────

    /**
     * Drops [ContentResolver.insert] calls targeting OPLUS telemetry providers.
     * Telemetry data is often funnelled through content providers before being
     * batched and transmitted.
     */
    private fun blockContentProviderInserts(pkg: String, classLoader: ClassLoader) {
        tryHook("ContentResolver.insert ($pkg)") {
            XposedHelpers.findAndHookMethod(
                "android.content.ContentResolver",
                classLoader,
                "insert",
                Uri::class.java,
                android.content.ContentValues::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val uri = param.args[0] as? Uri ?: return
                        val host = uri.host ?: return
                        if (isTelemetryUri(host)) {
                            XposedBridge.log("$TAG: Blocked CP insert [$uri] in $pkg")
                            param.result = null
                        }
                    }
                }
            )
        }
    }

    private fun isTelemetryUri(host: String): Boolean =
        host.contains("onetrace", ignoreCase = true)
                || host.contains("appsense", ignoreCase = true)
                || host.contains("oplus.log", ignoreCase = true)
                || host.contains("powermonitor", ignoreCase = true)

    // ─── Utility ──────────────────────────────────────────────────────────────

    private inline fun tryHook(label: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            // Silently skip – class/method may not exist in this ROM version
            XposedBridge.log("$TAG: ✗ $label – ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}

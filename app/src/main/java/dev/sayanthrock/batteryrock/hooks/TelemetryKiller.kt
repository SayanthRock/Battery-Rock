package dev.sayanthrock.batteryrock.hooks

import android.app.Service
import android.content.Intent
import android.net.Uri
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * TelemetryKiller — loaded inside selected OPLUS, Realme, and OnePlus
 * telemetry or drain package processes.
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

    private fun blockNetworkAccess(pkg: String, classLoader: ClassLoader) {
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
        host.contains("onetrace", ignoreCase = true) ||
            host.contains("appsense", ignoreCase = true) ||
            host.contains("oplus.log", ignoreCase = true) ||
            host.contains("powermonitor", ignoreCase = true)

    // ─── Utility ──────────────────────────────────────────────────────────────

    private inline fun tryHook(label: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            XposedBridge.log("$TAG: ✗ $label - ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}

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
import java.io.IOException

/**
 * TelemetryKiller is loaded inside selected OPLUS, Realme, and OnePlus
 * telemetry or drain package processes.
 */
object TelemetryKiller {

    private val TAG = "${BatteryRockInit.TAG}/TelemetryKiller"

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        val classLoader = lpparam.classLoader

        blockServiceStarts(packageName, classLoader)
        blockJobScheduling(packageName, classLoader)
        blockNetworkAccess(packageName, classLoader)
        blockContentProviderInserts(packageName, classLoader)
    }

    private fun blockServiceStarts(packageName: String, classLoader: ClassLoader) {
        tryHook("Service.onStartCommand ($packageName)") {
            XposedHelpers.findAndHookMethod(
                "android.app.Service",
                classLoader,
                "onStartCommand",
                Intent::class.java,
                Int::class.java,
                Int::class.java,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        XposedBridge.log(
                            "$TAG: Blocked Service.onStartCommand " +
                                "[${param.thisObject.safeClassName()}] in $packageName"
                        )
                        return Service.START_NOT_STICKY
                    }
                }
            )
        }
    }

    private fun blockJobScheduling(packageName: String, classLoader: ClassLoader) {
        tryHook("JobSchedulerImpl.schedule ($packageName)") {
            XposedHelpers.findAndHookMethod(
                "android.app.JobSchedulerImpl",
                classLoader,
                "schedule",
                android.app.job.JobInfo::class.java,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        XposedBridge.log("$TAG: Blocked job scheduling in $packageName")
                        return android.app.job.JobScheduler.RESULT_FAILURE
                    }
                }
            )
        }
    }

    private fun blockNetworkAccess(packageName: String, classLoader: ClassLoader) {
        tryHook("URL.openConnection ($packageName)") {
            XposedHelpers.findAndHookMethod(
                "java.net.URL",
                classLoader,
                "openConnection",
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        XposedBridge.log("$TAG: Blocked HTTP openConnection in $packageName")
                        throw IOException("Battery-Rock: network blocked for $packageName")
                    }
                }
            )
        }

        tryHook("OkHttpClient.newCall ($packageName)") {
            val requestClass = XposedHelpers.findClass("okhttp3.Request", classLoader)
            XposedHelpers.findAndHookMethod(
                "okhttp3.OkHttpClient",
                classLoader,
                "newCall",
                requestClass,
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        XposedBridge.log("$TAG: Blocked OkHttp call in $packageName")
                        throw IOException("Battery-Rock: network blocked for $packageName")
                    }
                }
            )
        }
    }

    private fun blockContentProviderInserts(packageName: String, classLoader: ClassLoader) {
        tryHook("ContentResolver.insert ($packageName)") {
            XposedHelpers.findAndHookMethod(
                "android.content.ContentResolver",
                classLoader,
                "insert",
                Uri::class.java,
                android.content.ContentValues::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val uri = param.args.firstOrNull() as? Uri ?: return
                        val host = uri.host ?: return
                        if (isTelemetryUri(host)) {
                            XposedBridge.log("$TAG: Blocked provider insert [$uri] in $packageName")
                            param.setResult(null)
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

    private fun Any?.safeClassName(): String = this?.javaClass?.simpleName ?: "unknown"

    private inline fun tryHook(label: String, block: () -> Unit) {
        try {
            block()
        } catch (throwable: Throwable) {
            AutoHookControllerEngine.reportEvent(label, throwable)
            XposedBridge.log("$TAG: skipped $label - ${throwable.javaClass.simpleName}: ${throwable.message}")
        }
    }
}

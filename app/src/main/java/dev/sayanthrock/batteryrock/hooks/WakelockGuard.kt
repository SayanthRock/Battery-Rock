package dev.sayanthrock.batteryrock.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * WakelockGuard is loaded in telemetry packages and SystemUI.
 *
 * It caps long or indefinite PowerManager.WakeLock acquisitions to reduce
 * runaway idle drain without permanently changing system files.
 */
object WakelockGuard {

    private val TAG = "${BatteryRockInit.TAG}/WakelockGuard"
    private const val MAX_WAKELOCK_MS = 30_000L

    private val inHook: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookTimedAcquire(lpparam.packageName, lpparam.classLoader)
        hookIndefiniteAcquire(lpparam.packageName, lpparam.classLoader)
    }

    private fun hookTimedAcquire(packageName: String, classLoader: ClassLoader) {
        tryHook("WakeLock.acquire(long) ($packageName)") {
            XposedHelpers.findAndHookMethod(
                "android.os.PowerManager\$WakeLock",
                classLoader,
                "acquire",
                Long::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (inHook.get() == true) return

                        val requested = param.args.firstOrNull() as? Long ?: return
                        if (requested == 0L || requested > MAX_WAKELOCK_MS) {
                            XposedBridge.log(
                                "$TAG: Capped wakelock ${requested}ms to ${MAX_WAKELOCK_MS}ms " +
                                    "[${param.thisObject.safeClassName()}] in $packageName"
                            )
                            param.args[0] = MAX_WAKELOCK_MS
                        }
                    }
                }
            )
        }
    }

    private fun hookIndefiniteAcquire(packageName: String, classLoader: ClassLoader) {
        tryHook("WakeLock.acquire() ($packageName)") {
            XposedHelpers.findAndHookMethod(
                "android.os.PowerManager\$WakeLock",
                classLoader,
                "acquire",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (inHook.get() == true) return

                        inHook.set(true)
                        try {
                            XposedBridge.log(
                                "$TAG: Converted indefinite wakelock to ${MAX_WAKELOCK_MS}ms " +
                                    "[${param.thisObject.safeClassName()}] in $packageName"
                            )
                            XposedHelpers.callMethod(param.thisObject, "acquire", MAX_WAKELOCK_MS)
                            param.setResult(null)
                        } finally {
                            inHook.set(false)
                        }
                    }
                }
            )
        }
    }

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

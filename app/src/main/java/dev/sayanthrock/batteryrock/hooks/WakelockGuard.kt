package dev.sayanthrock.batteryrock.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * WakelockGuard — loaded in telemetry packages and SystemUI.
 *
 * It caps long or indefinite PowerManager.WakeLock acquisitions to reduce
 * runaway idle drain without permanently changing system files.
 */
object WakelockGuard {

    private val TAG = "${BatteryRockInit.TAG}/WakelockGuard"

    /** Maximum wakelock duration Battery-Rock allows: 30 seconds. */
    private const val MAX_WAKELOCK_MS = 30_000L

    /** Re-entrancy guard per thread. */
    private val inHook: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookTimedAcquire(lpparam.packageName, lpparam.classLoader)
        hookIndefiniteAcquire(lpparam.packageName, lpparam.classLoader)
    }

    // ─── acquire(long timeout) ────────────────────────────────────────────────

    private fun hookTimedAcquire(pkg: String, classLoader: ClassLoader) {
        tryHook("WakeLock.acquire(long) ($pkg)") {
            XposedHelpers.findAndHookMethod(
                "android.os.PowerManager\$WakeLock",
                classLoader,
                "acquire",
                Long::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (inHook.get() == true) return

                        val requested = param.args[0] as Long
                        if (requested == 0L || requested > MAX_WAKELOCK_MS) {
                            XposedBridge.log(
                                "$TAG: Capped wakelock ${requested}ms to ${MAX_WAKELOCK_MS}ms " +
                                    "[${param.thisObject::class.java.simpleName}] in $pkg"
                            )
                            param.args[0] = MAX_WAKELOCK_MS
                        }
                    }
                }
            )
        }
    }

    // ─── acquire() ────────────────────────────────────────────────────────────

    private fun hookIndefiniteAcquire(pkg: String, classLoader: ClassLoader) {
        tryHook("WakeLock.acquire() ($pkg)") {
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
                                    "[${param.thisObject::class.java.simpleName}] in $pkg"
                            )
                            XposedHelpers.callMethod(param.thisObject, "acquire", MAX_WAKELOCK_MS)
                            param.result = null
                        } finally {
                            inHook.set(false)
                        }
                    }
                }
            )
        }
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    private inline fun tryHook(label: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            XposedBridge.log("$TAG: ✗ $label - ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}

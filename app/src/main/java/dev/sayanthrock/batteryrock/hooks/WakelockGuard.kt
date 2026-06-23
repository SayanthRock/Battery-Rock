package dev.sayanthrock.batteryrock.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * WakelockGuard — loaded in **telemetry packages** and **SystemUI**.
 *
 * Problem
 * ───────
 * OPLUS background services routinely acquire indefinite [PowerManager.WakeLock]s
 * and forget to release them, keeping the CPU core active during deep-sleep.
 * This prevents the SoC from entering its lowest power state.
 *
 * Fix
 * ───
 * 1. **Timed acquire(long)** — clamp any timeout above [MAX_WAKELOCK_MS] to [MAX_WAKELOCK_MS].
 * 2. **Indefinite acquire()** — convert to a capped timed acquire using [MAX_WAKELOCK_MS].
 *
 * A [ThreadLocal] gate prevents re-entrancy when the indefinite hook internally
 * calls the timed variant.
 */
object WakelockGuard {

    private val TAG = "${BatteryRockInit.TAG}/WakelockGuard"

    /** Maximum wakelock duration Battery-Rock allows — 30 seconds. */
    private const val MAX_WAKELOCK_MS = 30_000L

    /** Re-entrancy guard per thread. */
    private val inHook: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookTimedAcquire(lpparam.packageName, lpparam.classLoader)
        hookIndefiniteAcquire(lpparam.packageName, lpparam.classLoader)
    }

    // ─── acquire(long timeout) ────────────────────────────────────────────────

    /**
     * Clamps a timed wakelock to [MAX_WAKELOCK_MS].
     * A timeout of `0` means "no timeout" in the AOSP implementation — treated the
     * same as indefinite and replaced with [MAX_WAKELOCK_MS].
     */
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
                                "$TAG: Capped wakelock ${requested}ms → ${MAX_WAKELOCK_MS}ms" +
                                        " [${param.thisObject::class.java.simpleName}] in $pkg"
                            )
                            param.args[0] = MAX_WAKELOCK_MS
                        }
                    }
                }
            )
        }
    }

    // ─── acquire() ────────────────────────────────────────────────────────────

    /**
     * Converts an indefinite [acquire()] into a capped [acquire(long)].
     * The re-entrancy guard prevents the internal call from triggering this hook
     * again.
     */
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
                                "$TAG: Converted indefinite wakelock → ${MAX_WAKELOCK_MS}ms" +
                                        " [${param.thisObject::class.java.simpleName}] in $pkg"
                            )
                            // Replace with timed variant; the timed hook will see a value
                            // already ≤ MAX and will not modify it further.
                            XposedHelpers.callMethod(param.thisObject, "acquire", MAX_WAKELOCK_MS)
                            param.result = null // skip original indefinite acquire
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
            XposedBridge.log("$TAG: ✗ $label – ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}

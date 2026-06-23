package dev.sayanthrock.batteryrock.hooks

import android.os.Build
import de.robv.android.xposed.XposedBridge
import dev.sayanthrock.batteryrock.BatteryRockInit

/**
 * Auto Hook Controller Engine (Enterprise Adaptive Mode)
 *
 * Provides runtime control for hook activation based on:
 * - ROM profile
 * - SAFE_MODE flag
 * - runtime stability signals
 *
 * Goal: stable and adaptive hook execution across devices
 */
object AutoHookControllerEngine {

    private val TAG = "${BatteryRockInit.TAG}/AutoController"

    @Volatile
    private var eventCounter: Int = 0

    @Volatile
    private var lastEventTime: Long = 0L

    private const val EVENT_THRESHOLD = 3
    private const val EVENT_WINDOW_MS = 60_000L

    /**
     * Decide whether hooks should run for a given package
     */
    fun shouldEnableHooks(packageName: String): Boolean {

        val rom = RomAdaptiveEngine.detect()

        // SAFE MODE override
        if (BatteryRockInit.SAFE_MODE) {
            log("SAFE_MODE enabled, skipping hooks for $packageName")
            return false
        }

        // ROM-level restriction
        if (!rom.allowFrameworkHooks && packageName == "android") {
            log("ROM restriction active, skipping framework hooks")
            return false
        }

        // Stability check
        if (isUnstable()) {
            log("Temporary instability detected, reducing hook activity")
            return false
        }

        return true
    }

    /**
     * Report a runtime issue in hook execution
     */
    fun reportEvent(source: String, throwable: Throwable) {
        val now = System.currentTimeMillis()

        if (now - lastEventTime > EVENT_WINDOW_MS) {
            eventCounter = 0
        }

        lastEventTime = now
        eventCounter++

        try {
            XposedBridge.log("$TAG: Issue reported from $source → ${throwable.javaClass.simpleName}")
        } catch (_: Throwable) {
            // safe fallback
        }
    }

    /**
     * Detect unstable state
     */
    private fun isUnstable(): Boolean {
        return eventCounter >= EVENT_THRESHOLD
    }

    /**
     * Reset controller state
     */
    fun reset() {
        eventCounter = 0
        lastEventTime = 0L
        log("Controller state reset")
    }

    private fun log(msg: String) {
        try {
            XposedBridge.log("$TAG: $msg")
        } catch (_: Throwable) {
        }
    }
}
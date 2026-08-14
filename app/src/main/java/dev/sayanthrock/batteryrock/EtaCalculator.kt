package dev.sayanthrock.batteryrock

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.abs
import kotlin.math.sqrt

data class BatterySample(
    val timestamp: Long,
    val currentMa: Float,
    val isCharging: Boolean,
    val percentage: Int
)

sealed class EtaResult {
    object InsufficientData : EtaResult()
    object Unstable : EtaResult()
    data class Calculated(val timeToFullStr: String, val timeToEmptyStr: String) : EtaResult()
}

class EtaCalculator(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("eta_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val samples = mutableListOf<BatterySample>()

    init {
        loadSamples()
    }

    private fun loadSamples() {
        val json = prefs.getString("samples", null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<BatterySample>>() {}.type
                val loaded: List<BatterySample> = gson.fromJson(json, type)
                samples.addAll(loaded)
            } catch (e: Exception) {
                Log.e("EtaCalculator", "Failed to load samples", e)
            }
        }
    }

    private fun saveSamples() {
        prefs.edit().putString("samples", gson.toJson(samples)).apply()
    }

    fun addSample(currentMa: Float, isCharging: Boolean, percentage: Int) {
        val now = System.currentTimeMillis()

        // Reset if charging state changed
        if (samples.isNotEmpty() && samples.last().isCharging != isCharging) {
            samples.clear()
        }

        samples.add(BatterySample(now, currentMa, isCharging, percentage))

        // Keep last 30 minutes of samples max, or max 100 samples
        val thirtyMinsAgo = now - 30 * 60 * 1000L
        samples.removeAll { it.timestamp < thirtyMinsAgo }
        while (samples.size > 100) {
            samples.removeAt(0)
        }

        saveSamples()
    }

    fun calculateEta(totalCapacityMah: Float): EtaResult {
        if (samples.size < 5) {
            // Need at least 5 samples to have a decent baseline
            return EtaResult.InsufficientData
        }

        // Check time span, require at least 30 seconds of data
        val timeSpan = samples.last().timestamp - samples.first().timestamp
        if (timeSpan < 30_000) {
            return EtaResult.InsufficientData
        }

        val meanCurrent = samples.map { it.currentMa }.average().toFloat()

        // If mean is extremely low, might be unstable or fully charged
        if (abs(meanCurrent) < 10f) {
            return EtaResult.Unstable
        }

        val variance = samples.map { (it.currentMa - meanCurrent) * (it.currentMa - meanCurrent) }.average()
        val stdDev = sqrt(variance).toFloat()

        // If standard deviation is greater than 80% of the mean, it's too unstable
        if (stdDev > abs(meanCurrent) * 0.8f) {
            return EtaResult.Unstable
        }

        val lastSample = samples.last()
        var timeToFull = "Unknown"
        var timeToEmpty = "Unknown"

        if (lastSample.isCharging && lastSample.percentage < 100) {
            val remainingMah = totalCapacityMah * ((100 - lastSample.percentage) / 100f)
            val hours = remainingMah / abs(meanCurrent)
            if (hours in 0.01..24.0) {
                val mins = (hours * 60).toInt()
                timeToFull = "~${mins / 60}h ${mins % 60}m until 100%"
            }
        } else if (!lastSample.isCharging && lastSample.percentage > 0) {
            val availableMah = totalCapacityMah * (lastSample.percentage / 100f)
            val hours = availableMah / abs(meanCurrent)
            if (hours in 0.01..200.0) {
                val mins = (hours * 60).toInt()
                timeToEmpty = "~${mins / 60}h ${mins % 60}m remaining"
            }
        }

        return EtaResult.Calculated(timeToFull, timeToEmpty)
    }
}

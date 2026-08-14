package dev.sayanthrock.batteryrock.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

object BatteryRingGenerator {

    fun generateRingBitmap(config: WidgetConfig, percentage: Int): Bitmap {
        val sizePx = (config.ringSizeDp * 2.5f).toInt() // Rough dp to px scaling for typical density
        val thicknessPx = config.ringThicknessDp * 2.5f
        val startAngle = config.ringStartAngle

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = thicknessPx
            strokeCap = if (config.ringRoundedCaps) Paint.Cap.ROUND else Paint.Cap.BUTT
        }

        val padding = thicknessPx / 2f
        val rect = RectF(padding, padding, sizePx - padding, sizePx - padding)

        // Draw background ring
        if (config.ringEnabled) {
            paint.color = config.ringBackgroundColor.toInt()
            canvas.drawArc(rect, 0f, 360f, false, paint)
        }

        // Draw progress ring
        val sweepAngle = (percentage / 100f) * 360f
        val actualSweep = if (config.ringDirectionClockwise) sweepAngle else -sweepAngle

        paint.color = config.ringProgressColor.toInt()
        canvas.drawArc(rect, startAngle, actualSweep, false, paint)

        return bitmap
    }
}

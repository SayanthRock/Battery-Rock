package dev.sayanthrock.batteryrock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import kotlin.math.roundToInt

object NotificationIconGenerator {

    enum class State {
        DISCHARGING,
        CHARGING,
        LOW,
        FULL,
        IDLE
    }

    fun generateIcon(context: Context, percentage: Int, state: State): IconCompat {
        val size = 96 // Base size for drawing, will be scaled by system
        val strokeWidth = 8f
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Setup Paints
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = size * 0.45f
            typeface = Typeface.DEFAULT_BOLD
        }

        // 2. Determine colors based on state
        val primaryColor = when (state) {
            State.DISCHARGING -> Color.parseColor("#818CF8") // Indigo
            State.CHARGING -> Color.parseColor("#4F46E5") // Violet
            State.LOW -> Color.parseColor("#F59E0B") // Amber
            State.FULL -> Color.parseColor("#22C55E") // Green
            State.IDLE -> Color.parseColor("#9CA3AF") // Gray
        }

        // Background ring (faint)
        ringPaint.color = Color.argb(40, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))
        val bounds = RectF(
            strokeWidth / 2f + 4f,
            strokeWidth / 2f + 4f,
            size - strokeWidth / 2f - 4f,
            size - strokeWidth / 2f - 4f
        )
        canvas.drawArc(bounds, 0f, 360f, false, ringPaint)

        // Foreground ring (actual progress)
        ringPaint.color = primaryColor
        val sweepAngle = (percentage / 100f) * 360f
        // Start at top (-90 degrees)
        canvas.drawArc(bounds, -90f, sweepAngle, false, ringPaint)

        // 3. Draw text (percentage)
        val text = "$percentage"
        val yPos = (size / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(text, size / 2f, yPos, textPaint)

        // 4. Draw mini-glyph (bolt or check) if needed
        if (state == State.CHARGING || state == State.FULL) {
            val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryColor
                style = Paint.Style.FILL
            }
            // Small circle background for glyph at bottom right
            val glyphRadius = size * 0.15f
            val glyphCx = size - glyphRadius - 4f
            val glyphCy = size - glyphRadius - 4f

            val glyphBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F0F0F") // Glass bg
                style = Paint.Style.FILL
            }
            canvas.drawCircle(glyphCx, glyphCy, glyphRadius + 2f, glyphBgPaint)
            canvas.drawCircle(glyphCx, glyphCy, glyphRadius, glyphPaint)

            // Draw icon inside the mini circle
            val miniTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = glyphRadius * 1.2f
                typeface = Typeface.DEFAULT_BOLD
            }
            val glyphText = if (state == State.FULL) "✓" else "⚡"
            val glyphY = glyphCy - ((miniTextPaint.descent() + miniTextPaint.ascent()) / 2)
            canvas.drawText(glyphText, glyphCx, glyphY, miniTextPaint)
        }

        return IconCompat.createWithBitmap(bitmap)
    }
}

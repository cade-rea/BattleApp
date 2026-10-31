package com.caderea.battleapp.view.battle.queue

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/**
 * One queued action: a block in the technique's color, labeled with its name and the ticks
 * until it lands. A bar along the bottom fills up as the action winds up.
 *
 * @param windUpDone how much of the wind-up has passed, 0 to 1
 */
@SuppressLint("ViewConstructor")
internal class QueueDrawBlock(
    context: Context,
    private val label: String,
    color: Int,
    private val windUpDone: Float,
) : View(context) {

    private val backgroundPaint = Paint().apply { this.color = color }
    // dark technique colors get light text so the label stays readable
    private val textPaint = Paint().apply {
        this.color = if (Color.luminance(color) > 0.2f) Color.BLACK else Color.WHITE
    }

    private val windUpPaint = Paint().apply { this.color = textPaint.color }

    private var blockLeft = 0
    private var blockTop = 0
    private var blockRight = 0
    private var blockBottom = 0

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(
            blockLeft.toFloat(), blockTop.toFloat(), blockRight.toFloat(), blockBottom.toFloat(),
            backgroundPaint,
        )
        canvas.drawText(label, blockLeft + 5f, blockBottom - WIND_UP_BAR_HEIGHT - 5f, textPaint)

        if (windUpDone > 0f) {
            val barRight = blockLeft + (blockRight - blockLeft) * windUpDone
            canvas.drawRect(
                blockLeft.toFloat(), blockBottom - WIND_UP_BAR_HEIGHT, barRight, blockBottom.toFloat(),
                windUpPaint,
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        blockLeft = left
        blockTop = top
        blockRight = right
        blockBottom = bottom

        textPaint.textSize = (height / 3).toFloat()
    }

    private companion object {
        const val WIND_UP_BAR_HEIGHT = 12f
    }
}

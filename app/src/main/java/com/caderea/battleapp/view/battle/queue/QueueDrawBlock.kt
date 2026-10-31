package com.caderea.battleapp.view.battle.queue

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

@SuppressLint("ViewConstructor")
internal class QueueDrawBlock(
    context: Context,
    private val label: String,
    color: Int,
) : View(context) {

    private val backgroundPaint = Paint().apply { this.color = color }
    // dark technique colors get light text so the label stays readable
    private val textPaint = Paint().apply {
        this.color = if (Color.luminance(color) > 0.2f) Color.BLACK else Color.WHITE
    }

    private var blockLeft = 0
    private var blockTop = 0
    private var blockRight = 0
    private var blockBottom = 0

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(
            blockLeft.toFloat(), blockTop.toFloat(), blockRight.toFloat(), blockBottom.toFloat(),
            backgroundPaint,
        )
        canvas.drawText(label, blockLeft + 5f, blockBottom.toFloat(), textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        blockLeft = left
        blockTop = top
        blockRight = right
        blockBottom = bottom

        textPaint.textSize = (height shr 1).toFloat()
    }
}

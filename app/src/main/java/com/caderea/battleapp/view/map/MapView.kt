package com.caderea.battleapp.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt

@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class MapView(context: Context) : View(context) {

    var radius = 100f

    var paint = Paint().apply {
        textSize = 100f
        color = "#ff0000ff".toColorInt()
    }

    private var circleX = 0f
    private val lastTouch = PointF()
    private var zones: List<BattleRect> = emptyList()
    private var message = "Hello!"

    init {
        setOnClickListener { it.invalidate() }

        setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                lastTouch.set(event.x, event.y)

                zones.lastOrNull { it.rect.contains(event.x.toInt(), event.y.toInt()) }?.let {
                    message = "" + it.paint.color
                }
            }
            false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        zones = listOf(
            zone(0, 0, w / 2, h / 2, "#88ff0000"),
            zone(w / 2, 0, w, h / 2, "#8800ff00"),
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawText(message, height / 3f, width / 3f, paint)
        canvas.drawCircle(lastTouch.x, lastTouch.y, radius, paint)

        for (zone in zones) {
            canvas.drawRect(zone.rect, zone.paint)
        }
    }

    fun moveCircle() {
        circleX += 50
        invalidate()
    }

    private fun zone(left: Int, top: Int, right: Int, bottom: Int, colorString: String) =
        BattleRect(Rect(left, top, right, bottom), Paint().apply { color = colorString.toColorInt() })

    private data class BattleRect(val rect: Rect, val paint: Paint)
}

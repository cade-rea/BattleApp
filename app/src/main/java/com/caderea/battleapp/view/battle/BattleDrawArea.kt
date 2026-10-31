package com.caderea.battleapp.view.battle

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.caderea.battleapp.R

/**
 * Middle of the battle screen. For now it holds the fighters' status (health, energy and
 * guard) across the top, and the battle log in the rest of the area.
 */
class BattleDrawArea @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)

        val status = findViewById<View>(R.id.battleStatus)
        status.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST))

        val restHeightMeasureSpec =
            MeasureSpec.makeMeasureSpec(height - status.measuredHeight, MeasureSpec.EXACTLY)
        children.filter { it !== status }.forEach { it.measure(childWidthMeasureSpec, restHeightMeasureSpec) }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val status = findViewById<View>(R.id.battleStatus)
        status.layout(0, 0, r - l, status.measuredHeight)

        children.filter { it !== status }.forEach { it.layout(0, status.measuredHeight, r - l, b - t) }
    }
}

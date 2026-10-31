package com.caderea.battleapp.view.battle

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children

/**
 * Middle of the battle screen. For now it holds the battle log, which fills the whole area.
 */
class BattleDrawArea @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)

        children.forEach { it.measure(childWidthMeasureSpec, childHeightMeasureSpec) }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        children.forEach { it.layout(0, 0, r - l, b - t) }
    }
}

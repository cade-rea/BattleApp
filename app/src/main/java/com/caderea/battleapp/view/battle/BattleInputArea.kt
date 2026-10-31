package com.caderea.battleapp.view.battle

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children
import com.caderea.battleapp.R

class BattleInputArea @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private val bottomRowHeight = 100

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val buttonAreaHeight = height - bottomRowHeight

        val quadrantWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width / 2, MeasureSpec.EXACTLY)
        val quadrantHeightMeasureSpec = MeasureSpec.makeMeasureSpec(buttonAreaHeight / 2, MeasureSpec.EXACTLY)

        val bottomRowWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width / 4, MeasureSpec.EXACTLY)
        val bottomRowHeightMeasureSpec = MeasureSpec.makeMeasureSpec(bottomRowHeight, MeasureSpec.EXACTLY)

        for (child in children) {
            if (child.id == R.id.buttonStop) {
                child.measure(bottomRowWidthMeasureSpec, bottomRowHeightMeasureSpec)
            } else {
                child.measure(quadrantWidthMeasureSpec, quadrantHeightMeasureSpec)
            }
        }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t

        val buttonAreaBottom = height - bottomRowHeight

        val halfWidth = width / 2
        val halfHeight = buttonAreaBottom / 2

        for (child in children) {
            when (child.id) {
                R.id.button1 -> child.layout(0, 0, halfWidth, halfHeight)
                R.id.button2 -> child.layout(halfWidth, 0, r, halfHeight)
                R.id.button3 -> child.layout(0, halfHeight, halfWidth, buttonAreaBottom)
                R.id.button4 -> child.layout(halfWidth, halfHeight, r, buttonAreaBottom)
                R.id.buttonStop -> child.layout(0, buttonAreaBottom, width / 4, b)
                else -> child.layout(0, 0, 0, 0)
            }
        }
    }
}

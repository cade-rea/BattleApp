package com.caderea.battleapp.view.battle.queue

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.ViewGroup
import com.caderea.battleapp.core.BattleQueue
import com.caderea.battleapp.core.QueuedAction

/**
 * Draws one fighter's queue as a column of blocks in each technique's own color, front of
 * the queue on top.
 */
class QueueDrawer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private val queueDrawBlocks = arrayOfNulls<QueueDrawBlock>(BattleQueue.MAX_SIZE)
    private var shownActions: List<QueuedAction> = emptyList()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val blockHeight = height / BattleQueue.MAX_SIZE

        val blockHeightMeasureSpec = MeasureSpec.makeMeasureSpec(blockHeight, MeasureSpec.EXACTLY)
        val blockWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)

        queueDrawBlocks.forEach { it?.measure(blockWidthMeasureSpec, blockHeightMeasureSpec) }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val blockHeight = height / BattleQueue.MAX_SIZE
        var blockTop = 0
        for (queueDrawBlock in queueDrawBlocks) {
            if (queueDrawBlock != null) {
                queueDrawBlock.layout(0, blockTop, right, blockTop + blockHeight)
                blockTop += blockHeight
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        queueDrawBlocks.forEach { it?.draw(canvas) }
    }

    /**
     * Shows the given queued actions, front first. Does nothing if they haven't changed.
     */
    fun showActions(actions: List<QueuedAction>) {
        if (actions == shownActions) return
        shownActions = actions

        for (i in queueDrawBlocks.indices) {
            queueDrawBlocks[i]?.let { removeView(it) }
            queueDrawBlocks[i] = actions.getOrNull(i)?.let { queuedAction ->
                QueueDrawBlock(context, queuedAction.action.name, queuedAction.action.color.argb).also { addView(it) }
            }
        }

        invalidate()
    }
}

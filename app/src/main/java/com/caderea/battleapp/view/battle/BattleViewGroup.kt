package com.caderea.battleapp.view.battle

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ProgressBar
import com.caderea.battleapp.R
import com.caderea.battleapp.core.QueuedAction
import com.caderea.battleapp.view.battle.queue.QueueDrawer

class BattleViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private lateinit var p1queue: QueueDrawer
    private lateinit var p2queue: QueueDrawer
    private lateinit var tickProgress: ProgressBar
    private lateinit var battleDrawArea: BattleDrawArea
    private lateinit var battleInputArea: BattleInputArea

    private val topBarHeight = 150
    private val inputAreaHeight = 550

    override fun onFinishInflate() {
        super.onFinishInflate()

        p1queue = findViewById(R.id.player1QDrawer)
        p2queue = findViewById(R.id.player2QDrawer)
        tickProgress = findViewById(R.id.tickProgressBar)
        battleDrawArea = findViewById(R.id.battleDrawArea)
        battleInputArea = findViewById(R.id.battle_buttons)
    }

    fun updateTickProgress(progress: Int) {
        tickProgress.progress = progress
    }

    fun showQueues(playerQueue: List<QueuedAction>, enemyQueue: List<QueuedAction>) {
        p1queue.showActions(playerQueue)
        p2queue.showActions(enemyQueue)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val totalHeight = MeasureSpec.getSize(heightMeasureSpec)

        val oneFifthWidth = totalWidth / 5
        val queueHeight = totalHeight - topBarHeight - inputAreaHeight
        val battleDrawWidth = totalWidth - (2 * oneFifthWidth)

        p1queue.measure(exactly(oneFifthWidth), exactly(queueHeight))
        p2queue.measure(exactly(oneFifthWidth), exactly(queueHeight))
        tickProgress.measure(exactly(totalWidth), exactly(topBarHeight))
        battleDrawArea.measure(exactly(battleDrawWidth), exactly(queueHeight))
        battleInputArea.measure(exactly(totalWidth), exactly(inputAreaHeight))

        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top

        val oneFifthWidth = width / 5

        tickProgress.layout(0, 0, width, topBarHeight)

        val inputAreaTop = height - inputAreaHeight
        battleInputArea.layout(left, inputAreaTop, right, height)

        // queues 20% of each side
        val q1Right = oneFifthWidth
        p1queue.layout(0, topBarHeight, q1Right, inputAreaTop)

        val q2Left = right - oneFifthWidth
        p2queue.layout(q2Left, topBarHeight, right, inputAreaTop)

        battleDrawArea.layout(q1Right + 1, topBarHeight, q2Left - 1, inputAreaTop)
    }

    private fun exactly(size: Int) = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
}

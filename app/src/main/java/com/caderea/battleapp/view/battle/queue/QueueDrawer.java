package com.caderea.battleapp.view.battle.queue;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.caderea.battleapp.core.BattleQueue;
import com.caderea.battleapp.core.action.BattleAction;
import lombok.Setter;

/**
 * Created by Cade on 8/10/2014.
 */
public class QueueDrawer extends ViewGroup {

    private QueueItemDrawBlock[] queueItemDrawBlocks;

    @Setter
    private BattleQueue queue;

    public QueueDrawer(Context context) {
        this(context, null);
    }

    public QueueDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        queueItemDrawBlocks = new QueueItemDrawBlock[BattleQueue.getMaxQueueSize()];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int blockHeight = height / BattleQueue.getMaxQueueSize();

        int blockHeightMeasureSpec = MeasureSpec.makeMeasureSpec(blockHeight, MeasureSpec.EXACTLY);
        int blockWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

        for (QueueItemDrawBlock queueItemDrawBlock : queueItemDrawBlocks) {
            if (queueItemDrawBlock != null) {
                queueItemDrawBlock.measure(blockWidthMeasureSpec, blockHeightMeasureSpec);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int blockHeight = getHeight() / BattleQueue.getMaxQueueSize();
        int blockTop = 0;
        for (QueueItemDrawBlock queueItemDrawBlock : queueItemDrawBlocks) {
            if (queueItemDrawBlock != null) {
                queueItemDrawBlock.layout(0, blockTop, right, blockTop + blockHeight);
                blockTop += blockHeight;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for (QueueItemDrawBlock queueItemDrawBlock : queueItemDrawBlocks) {
            if (queueItemDrawBlock != null) {
                queueItemDrawBlock.draw(canvas);
            }
        }

    }

    public void update() {
        for (int i = 0; i < queueItemDrawBlocks.length; ++i) {
            removeExistingQueueDrawBlock(i);

            addQueueDrawBlock(i);
        }

        invalidate();
    }

    private void removeExistingQueueDrawBlock(int i) {
        QueueItemDrawBlock existingQueueItemDrawBlock = queueItemDrawBlocks[i];
        if (existingQueueItemDrawBlock != null) {
            removeView(queueItemDrawBlocks[i]);
            queueItemDrawBlocks[i] = null;
        }
    }

    private void addQueueDrawBlock(int i) {
        BattleAction battleAction = queue.get(i);
        if (battleAction != null) {
            QueueItemDrawBlock queueItemDrawBlock = new QueueItemDrawBlock(this.getContext(), battleAction);
            queueItemDrawBlocks[i] = queueItemDrawBlock;
            addView(queueItemDrawBlocks[i]);
        }
    }

}

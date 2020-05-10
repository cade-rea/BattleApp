package com.caderea.battleapp.view.battle.queue;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.caderea.battleapp.core.BattleQueue;
import lombok.Setter;

/**
 * Created by Cade on 8/10/2014.
 */
public class QueueDrawer extends ViewGroup {

    private QueueDrawBlock[] queueDrawBlocks;

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
        queueDrawBlocks = new QueueDrawBlock[BattleQueue.getMaxQueueSize()];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int blockHeight = height / BattleQueue.getMaxQueueSize();

        int blockHeightMeasureSpec = MeasureSpec.makeMeasureSpec(blockHeight, MeasureSpec.EXACTLY);
        int blockWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

        for (QueueDrawBlock queueDrawBlock : queueDrawBlocks) {
            if (queueDrawBlock != null) {
                queueDrawBlock.measure(blockWidthMeasureSpec, blockHeightMeasureSpec);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int blockHeight = getHeight() / BattleQueue.getMaxQueueSize();
        int blockTop = 0;
        for (QueueDrawBlock queueDrawBlock : queueDrawBlocks) {
            if (queueDrawBlock != null) {
                queueDrawBlock.layout(0, blockTop, right, blockTop + blockHeight);
                blockTop += blockHeight;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for (QueueDrawBlock queueDrawBlock : queueDrawBlocks) {
            if (queueDrawBlock != null) {
                queueDrawBlock.draw(canvas);
            }
        }

    }

    public void update() {
        for (int i = 0; i < queueDrawBlocks.length; ++i) {
            removeExistingQueueDrawBlock(i);

            addQueueDrawBlock(i);
        }

        invalidate();
    }

    private void removeExistingQueueDrawBlock(int i) {
        QueueDrawBlock existingQueueDrawBlock = queueDrawBlocks[i];
        if (existingQueueDrawBlock != null) {
            removeView(queueDrawBlocks[i]);
            queueDrawBlocks[i] = null;
        }
    }

    private void addQueueDrawBlock(int i) {
        QueueAction queueAction = queue.get(i);
        if (queueAction != null) {
            QueueDrawBlock queueDrawBlock = new QueueDrawBlock(this.getContext(), queueAction);
            queueDrawBlocks[i] = queueDrawBlock;
            addView(queueDrawBlocks[i]);
        }
    }

}

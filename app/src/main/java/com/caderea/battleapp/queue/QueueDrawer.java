package com.caderea.battleapp.queue;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import lombok.Setter;

/**
 * Created by Cade on 8/10/2014.
 */
public class QueueDrawer extends ViewGroup {

    private DrawBlock[] drawBlocks;

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
        drawBlocks = new DrawBlock[BattleQueue.getMaxQueueSize()];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int blockHeight = height / BattleQueue.getMaxQueueSize();

        int blockHeightMeasureSpec = MeasureSpec.makeMeasureSpec(blockHeight, MeasureSpec.EXACTLY);
        int blockWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

        for (DrawBlock drawBlock : drawBlocks) {
            if (drawBlock != null) {
                drawBlock.measure(blockWidthMeasureSpec, blockHeightMeasureSpec);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int blockHeight = getHeight() / BattleQueue.getMaxQueueSize();
        int blockTop = top;
        for (DrawBlock drawBlock : drawBlocks) {
            if (drawBlock != null) {
                blockTop += blockHeight;
                drawBlock.layout(0, blockTop, right, blockTop + blockHeight);
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for (DrawBlock drawBlock : drawBlocks) {
            if (drawBlock != null) {
                drawBlock.draw(canvas);
            }
        }

    }

    public void update() {
        for (int i = 0; i < drawBlocks.length; ++i) {
            QueueAction queueAction = queue.get(i);
            if (queueAction != null) {
                DrawBlock drawBlock = new DrawBlock(this.getContext(), queueAction);
                drawBlocks[i] = drawBlock;
                addView(drawBlocks[i]);
            }
        }

        invalidate();
    }

}

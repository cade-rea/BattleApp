package com.caderea.battleapp.queue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import org.apache.commons.collections4.iterators.LoopingIterator;

import java.util.Arrays;

/**
 * Created by Cade on 8/10/2014.
 */
public class QueueDrawer extends ViewGroup {
    private static final Integer[] colors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
    private static final int NUMBER_OF_TICKS_ON_SCREEN = 10;

    private LoopingIterator<Integer> colorIterator;
    private DrawBlock[] drawBlocks;
    private BattleQueue queue;

    public QueueDrawer(Context context) {
        this(context, null);
    }

    public QueueDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        colorIterator = new LoopingIterator<>(Arrays.asList(colors));
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
                drawBlock.layout(0, 0, right, blockHeight);
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

    private DrawBlock createDrawBlockFromAction(QueueAction queueAction) {
        DrawBlock drawBlock = new DrawBlock(this.getContext(), queueAction, colorIterator.next());
        return drawBlock;
    }

    public void setQueue(BattleQueue q) {
        queue = q;
    }

    public void update() {
        removeAllViewsInLayout();
        for (int i = 0; i < drawBlocks.length; ++i) {
            QueueAction queueAction = queue.get(i);
            if (queueAction != null) {
                drawBlocks[i] = createDrawBlockFromAction(queueAction);
                addView(drawBlocks[i]);
            }
        }

        for (DrawBlock drawBlock : drawBlocks) {
            if (drawBlock != null) {
                drawBlock.requestLayout();
                drawBlock.invalidate();
            }
        }

        requestLayout();
        invalidate();
    }

}

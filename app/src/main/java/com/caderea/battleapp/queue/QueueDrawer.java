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
        super(context);
        init();
    }

    public QueueDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        colorIterator = new LoopingIterator<>(Arrays.asList(colors));
        drawBlocks = new DrawBlock[BattleQueue.getMaxQueueSize() * 2];
    }

//    protected void NotonDraw(Canvas canvas) {
//        int blockHeight = 200;
//
//        Log.d("DRAWER","Queue block height: "+ blockHeight + ", width: "+ dimension.width + ", height: "+ dimension.height);
//
////        int row = 0;
////        while (row < dimension.height) {
////            canvas.drawLine(0, row, dimension.width, row, paint);
////            row += 10;
////        }
//
//
//        //check the main battle queue for items that are not in blocksToDraw
//        for(int i = 0; i < BattleQueue.getMaxQueueSize(); ++i) {
//            QueueAction queueAction = queue.get(i);
//            if (queueAction != null) {
//                boolean added = false;
//
////                float lengthOfLabel = dimension.width / (NUMBER_OF_TICKS_ON_SCREEN * queueAction.getBattleAction().getDuration());
////                float verticalStart = 0;
//
//                for (DrawBlock drawBlock : drawBlocks) {
//                    if (drawBlock != null && drawBlock.getQueueAction() == queueAction) {
//                        drawBlocks[i] = drawBlock;
//                        added = true;
//                        break;
//                    }
//                }
//                if(!added) {
//                    drawBlocks[i] = new DrawBlock(this, queueAction, getNextColor(), verticalStart, lengthOfLabel, canvas, blockHeight);
//                }
//            } else {
//                //queueAction == null
//                //clear rest of blockstodraw
//                //stop processing
//
//                for(int j = i; j < drawBlocks.length; j++) {
//                    drawBlocks[j] = null;
//                }
//                break;
//            }
//        }
//
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (DrawBlock drawBlock : drawBlocks) {
            drawBlock.measure(widthMeasureSpec, 200);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int blockTop = top;
        for (QueueAction queueAction : queue) {
            blockTop += 200;
            DrawBlock drawBlock = createDrawBlockFromAction(queueAction);
            drawBlock.layout(left, blockTop, right, bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (DrawBlock drawBlock : drawBlocks) {
            drawBlock.draw(canvas);
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
        invalidate();

    }

}

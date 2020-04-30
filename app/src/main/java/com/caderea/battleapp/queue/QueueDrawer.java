package com.caderea.battleapp.queue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by Cade on 8/10/2014.
 */
public class QueueDrawer extends AppCompatTextView {
    private Rect area;
    private Paint paint;
    private BattleQueue queue;
    private Paint rectPaint;

    private static int[] colors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
    private int currentColorPosition;

    private DrawBlock[] blocksToDraw;

    private static int NUMBER_OF_TICKS_ON_SCREEN = 10;

    public QueueDrawer(Context context) {
        super(context);
        init();
    }

    public QueueDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        area = new Rect();
        paint = new Paint();
        rectPaint = new Paint();
        queue = new BattleQueue(); //setup an empty BattleQueue until Battle sets the queue for each QueueDrawer
        currentColorPosition = 0;
        blocksToDraw = new DrawBlock[BattleQueue.getMaxQueueSize() * 2];
    }

    protected void onDraw(Canvas canvas) {
        int baseline = 200;

        Log.d("DRAWER","Queue size:"+ baseline + ", area.left:"+area.left + ", area.right"+area.right +
                ", area.top"+area.top + ", area.bottom:"+area.bottom );

        //horizontal line all the way across the bottom
        canvas.drawLine(area.left, baseline, area.right, baseline, paint);
        paint.setTextSize(baseline / 2);

        //check the main battle queue for items that are not in blocksToDraw
        for(int i = 0; i < BattleQueue.getMaxQueueSize(); ++i) {
            QueueAction queueAction = queue.get(i);
            if (queueAction != null) {
                boolean added = false;

                float lengthOfLabel = area.right / (NUMBER_OF_TICKS_ON_SCREEN * queueAction.getBattleAction().getDuration());
                float verticalStart = area.left + (i * lengthOfLabel);

                for (DrawBlock drawBlock : blocksToDraw) {
                    if (drawBlock != null && drawBlock.queueAction == queueAction) {
                        drawBlock.verticalStart = verticalStart;
                        blocksToDraw[i] = drawBlock;
                        added = true;
                        break;
                    }
                }
                if(!added) {
                    blocksToDraw[i] = new DrawBlock(queueAction, getNextColor(), verticalStart, lengthOfLabel, canvas, baseline);
                }
            } else {
                //queueAction == null
                //clear rest of blockstodraw
                //stop processing

                for(int j = i; j < blocksToDraw.length; j++) {
                    blocksToDraw[j] = null;
                }
                break;
            }
        }

        for(DrawBlock drawBlock : blocksToDraw) {
            if (drawBlock != null)
                drawBlock.drawblock(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        area.set(left, top, right, bottom);
    }

    public void setQueue(BattleQueue q) {
        queue = q;
    }

    public void update() {
        invalidate();

    }

    private int getNextColor() {
        if (currentColorPosition == colors.length - 1) {
            currentColorPosition = -1;
        }

        return colors[++currentColorPosition];
    }

    private class DrawBlock {
        int color;
        QueueAction queueAction;
        float verticalStart;
        float length;
        Canvas canvas;
        int baseline;

        public DrawBlock(QueueAction queueAction, int color, float verticalStart, float length, Canvas canvas, int baseline) {
            this.queueAction = queueAction;
            this.color = color;
            this.verticalStart = verticalStart;
            this.length = length;
            this.canvas = canvas;
            this.baseline = baseline;
        }

        public void drawblock(Canvas canvas) {
            //canvas.drawLine(startX, startY, stopX, stopY, paint);
            rectPaint.setColor(color);
            canvas.drawLine(verticalStart, baseline, verticalStart, area.top, paint);
            canvas.drawRect(verticalStart, area.top, verticalStart + (queueAction.getBattleAction().getDuration() * length ), baseline, rectPaint);
            canvas.drawText(queueAction.getBattleAction().getName(), verticalStart + 5, baseline - 1, paint);
        }
    }
}

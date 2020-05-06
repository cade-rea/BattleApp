package com.caderea.battleapp.queue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import lombok.Getter;
import lombok.Setter;

class DrawBlock extends View {
    private final Paint backgroundPaint;
    private final Paint textPaint;
    @Getter private final QueueAction queueAction;
    @Setter private float topLine;
//    @Setter private float height;

    private int left;
    private int top;
    private int right;
    private int bottom;

    private boolean measured = false;
    private boolean layedout = false;

    public DrawBlock(Context context, QueueAction queueAction, int backgroundColor) {
        super(context);
        this.queueAction = queueAction;

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(getHeight() >> 1);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        float top = topLine;
//        float left = 0;
//        int right = getRight();
//        float bottom = top + getHeight();
        canvas.drawRect(left, top, right, bottom, backgroundPaint);
        canvas.drawText(queueAction.getBattleAction().getName(), left + 5, bottom, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
        measured = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        layedout = true;
    }

}

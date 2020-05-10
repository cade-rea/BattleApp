package com.caderea.battleapp.view.battle.queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

@SuppressLint("ViewConstructor")
class QueueDrawBlock extends View {
    private final Paint backgroundPaint;
    private final Paint textPaint;
    private final QueueAction queueAction;

    private int left;
    private int top;
    private int right;
    private int bottom;

    public QueueDrawBlock(Context context, QueueAction queueAction) {
        super(context);
        this.queueAction = queueAction;

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(queueAction.getColor());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawRect(left, top, right, bottom, backgroundPaint);
        canvas.drawText(queueAction.getBattleAction().getName(), left + 5, bottom, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        textPaint.setTextSize(getHeight() >> 1);
    }

}

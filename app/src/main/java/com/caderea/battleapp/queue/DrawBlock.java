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
    @Setter private float height;

    public DrawBlock(Context context, QueueAction queueAction, int backgroundColor) {
        super(context);
        this.queueAction = queueAction;

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(height / 2);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float top = topLine;
        float left = 0;
        int right = getRight();
        float bottom = top + height;
        canvas.drawRect(left, top, right, bottom, backgroundPaint);
        canvas.drawText(queueAction.getBattleAction().getName(), left + 5, bottom, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension(width, height);
    }
}

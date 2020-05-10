package com.caderea.battleapp.view.battle.queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.caderea.battleapp.core.action.BattleAction;
import org.apache.commons.collections4.iterators.LoopingIterator;

import java.util.Arrays;

@SuppressLint("ViewConstructor")
class QueueItemDrawBlock extends View {
    private static final LoopingIterator<Integer> colorIterator =
            new LoopingIterator<>(Arrays.asList(Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN));

    private final BattleAction battleAction;
    private final Paint backgroundPaint;
    private final Paint textPaint;

    private int left;
    private int top;
    private int right;
    private int bottom;

    public QueueItemDrawBlock(Context context, BattleAction battleAction) {
        super(context);
        this.battleAction = battleAction;

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(colorIterator.next());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawRect(left, top, right, bottom, backgroundPaint);
        canvas.drawText(battleAction.getName(), left + 5, bottom, textPaint);
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

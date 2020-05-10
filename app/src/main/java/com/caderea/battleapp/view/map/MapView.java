package com.caderea.battleapp.view.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

public class MapView extends View {

    private float x, y, radius;
    private Paint paint;

    private PointF lastTouch;

    public MapView(Context context) {
        super(context);
        x=200;
        y=200;
        radius=100;
        paint = new Paint();
        lastTouch = new PointF();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                paint.setColor(Color.parseColor("#ff0000ff"));
                v.invalidate();
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouch.set(event.getX(), event.getY());
                }

                return false;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText("Hello!", x + 100, y + 100, paint);
        canvas.drawCircle(lastTouch.x, lastTouch.y, radius, paint);
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
        invalidate();
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }
}

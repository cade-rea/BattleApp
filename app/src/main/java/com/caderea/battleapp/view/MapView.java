package com.caderea.battleapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class MapView extends View {

    private float x, y, radius;
    private Paint paint;

    public MapView(Context context) {
        super(context);
        x=200;
        y=200;
        radius=100;
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(x,y,radius,paint);
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

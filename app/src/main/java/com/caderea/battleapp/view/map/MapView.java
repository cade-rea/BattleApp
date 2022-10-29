package com.caderea.battleapp.view.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import lombok.AllArgsConstructor;
import lombok.Data;

public class MapView extends View {

    private float x, y, radius=100;
    private Paint paint;

    private PointF lastTouch;
    private int height;
    private int width;
    private BattleRect[] zones;
    private String message;

    public MapView(Context context) {
        super(context);
        paint = new Paint();
        paint.setTextSize(100);
        lastTouch = new PointF();
        message = "Hello!";
        paint.setColor(Color.parseColor("#ff0000ff"));

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.invalidate();
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouch.set(event.getX(), event.getY());

                    for (BattleRect zone : zones) {
                        if (zone.getRect().contains((int) event.getX(), (int) event.getY())) {
                            message = "" + zone.getPaint().getColor();
                        }
                    }

                }

                return false;
            }
        });
    }

    private void init() {
        height = getHeight();
        width = getWidth();

        zones = new BattleRect[]{
                getRect(0, 0, width / 2, height / 2, "#88ff0000"),
                getRect(width / 2, 0, width, height / 2, "#8800ff00")};
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (height == 0 && width == 0) {
            init();
        }

        canvas.drawText(message, height / 3, width / 3, paint);
        canvas.drawCircle(lastTouch.x, lastTouch.y, radius, paint);

        for (BattleRect zone : zones) {
            canvas.drawRect(zone.getRect(), zone.getPaint());
        }
    }

    private BattleRect getRect(int left, int top, int right, int bottom, String colorString) {

        Rect rect = new Rect(left, top, right, bottom);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor(colorString));

        return new BattleRect(rect, paint);
    }

    @Data
    @AllArgsConstructor
    private static class BattleRect {
        Rect rect;
        Paint paint;
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

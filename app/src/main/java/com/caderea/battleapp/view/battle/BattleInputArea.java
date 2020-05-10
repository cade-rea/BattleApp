package com.caderea.battleapp.view.battle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.caderea.battleapp.R;

public class BattleInputArea extends ViewGroup {

    private Button[] battleButtons = new Button[5];
    private int bottomRowHeight = 100;

    public BattleInputArea(Context context) {
        super(context);
    }

    public BattleInputArea(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int buttonAreaHeight = height - bottomRowHeight;

        int quadrantWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width / 2, MeasureSpec.EXACTLY);
        int quadrantHeightMeasureSpec = MeasureSpec.makeMeasureSpec(buttonAreaHeight / 2, MeasureSpec.EXACTLY);

        int bottomRowHeightMeasureSpec = MeasureSpec.makeMeasureSpec(bottomRowHeight, MeasureSpec.EXACTLY);
        int bottomRowWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width / 4, MeasureSpec.EXACTLY);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);

            if (child.getId() == R.id.buttonStop) {
                child.measure(bottomRowWidthMeasureSpec, bottomRowHeightMeasureSpec);
            } else {
                child.measure(quadrantWidthMeasureSpec, quadrantHeightMeasureSpec);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();

        int width = r - l;
        int height = b - t;

        int buttonAreaBottom = height - bottomRowHeight;

        int halfWidth = width / 2;
        int halfHeight = buttonAreaBottom / 2;

        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);

            int buttonLeft = 0;
            int buttonTop = 0;
            int buttonRight = 0;
            int buttonBottom = 0;

            switch(child.getId()) {
                case R.id.button1:
                    buttonRight = halfWidth;
                    buttonBottom = halfHeight;
                    break;
                case R.id.button2:
                    buttonLeft = halfWidth;
                    buttonRight = r;
                    buttonBottom = halfHeight;
                    break;
                case R.id.button3:
                    buttonTop = halfHeight;
                    buttonRight = halfWidth;
                    buttonBottom = buttonAreaBottom;
                    break;
                case R.id.button4:
                    buttonLeft = halfWidth;
                    buttonTop = halfHeight;
                    buttonRight = r;
                    buttonBottom = buttonAreaBottom;
                    break;
                case R.id.buttonStop:
                    buttonTop = buttonAreaBottom;
                    buttonRight = width / 4;
                    buttonBottom = b;
                    break;
            }

            child.layout(buttonLeft, buttonTop, buttonRight, buttonBottom);
        }
    }
}

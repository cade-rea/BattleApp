package com.caderea.battleapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.caderea.battleapp.R;

public class BattleInputArea extends ViewGroup {

    private Button[] battleButtons = new Button[5];

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

        int quadrantWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width / 2, MeasureSpec.EXACTLY);
        int quadrantHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height / 2, MeasureSpec.EXACTLY);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            getChildAt(i).measure(quadrantWidthMeasureSpec, quadrantHeightMeasureSpec);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();

        int width = r - l;
        int height = b - t;

        int halfWidth = width / 2;
        int halfHeight = height / 2;

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
                    buttonBottom = b;
                    break;
                case R.id.button4:
                    buttonLeft = halfWidth;
                    buttonTop = halfHeight;
                    buttonRight = r;
                    buttonBottom = b;
                    break;
                case R.id.buttonStop:
                    buttonLeft = halfWidth / 2;
                    buttonTop = halfHeight / 2;
                    buttonRight = halfWidth + buttonLeft;
                    buttonBottom = halfHeight + buttonTop;
                    break;
            }

            child.layout(buttonLeft, buttonTop, buttonRight, buttonBottom);
        }
    }
}

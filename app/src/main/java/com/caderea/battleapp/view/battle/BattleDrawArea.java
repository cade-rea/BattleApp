package com.caderea.battleapp.view.battle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Middle of the battle screen. For now it holds the battle log, which fills the whole area.
 */
public class BattleDrawArea extends ViewGroup {

    public BattleDrawArea(Context context) {
        super(context);
    }

    public BattleDrawArea(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            child.layout(0, 0, r - l, b - t);
        }
    }
}

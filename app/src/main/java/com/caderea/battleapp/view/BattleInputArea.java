package com.caderea.battleapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;

public class BattleInputArea extends ViewGroup {

    private Button[] battleButtons = new Button[5];

    public BattleInputArea(Context context) {
        super(context);
    }

    public BattleInputArea(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}

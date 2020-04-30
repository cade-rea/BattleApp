package com.caderea.battleapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import com.caderea.battleapp.R;
import com.caderea.battleapp.queue.BattleQueue;
import com.caderea.battleapp.queue.QueueDrawer;

public class NewBattleView extends ViewGroup {

    QueueDrawer p1q, p2q;
    ProgressBar tickProgress;

    public NewBattleView(Context context) {
        super(context);
    }

    public NewBattleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NewBattleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NewBattleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initQueues() {
        p1q = (QueueDrawer)findViewById(R.id.player1QDrawer2);
        p2q = (QueueDrawer)findViewById(R.id.player2QDrawer2);
        tickProgress = (ProgressBar)findViewById(R.id.tickProgressBar2);
    }

    public void updateTickProgress(int progress) {
        tickProgress.setProgress(progress);
    }

    public void setQueues(BattleQueue q1, BattleQueue q2) {
        initQueues();
        p1q.setQueue(q1);
        p2q.setQueue(q2);
    }

    public void refreshQueues() {
        p1q.update();
        p2q.update();
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        p1q.draw(canvas);
//        p2q.draw(canvas);
//        Log.d("onDraw", "onDraw: ");
//    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        p1q.layout(l, t, r, b);
        p2q.layout(l+100, t+200, r, b);
    }
}

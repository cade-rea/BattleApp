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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;

        // queues 20% of each side
        int q1Left = 0;
        int q1Top = 100;
        int q1Right = q1Left + width / 5;
        int q1Bottom = height - 100;
        p1q.layout(q1Left, q1Top, q1Right, q1Bottom);

        int q2Left = right - width / 5;
        int q2Top = 100;
        int q2Right = right;
        int q2Bottom = height - 100;
        p2q.layout(q2Left, q2Top, q2Right, q2Bottom);
    }
}

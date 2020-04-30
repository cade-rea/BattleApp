package com.caderea.battleapp.queue;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.caderea.battleapp.R;

/**
 * Created by Cade on 8/16/2014.
 */
public class QueueLayout extends LinearLayout {
    QueueDrawer p1q, p2q;
    ProgressBar tickProgress;

    public QueueLayout(Context context) {
        super(context);
    }

    public QueueLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

//    public void initQueues() {
//        p1q = (QueueDrawer)findViewById(R.id.player1QDrawer);
//        p2q = (QueueDrawer)findViewById(R.id.player2QDrawer);
//        tickProgress = (ProgressBar)findViewById(R.id.tickProgressBar);
//    }
//
//    public void setQueues(BattleQueue q1, BattleQueue q2) {
//        initQueues();
//        p1q.setQueue(q1);
//        p2q.setQueue(q2);
//    }
//
//    public void updateTickProgress(int progress) {
//        tickProgress.setProgress(progress);
//    }
//
//    public void refreshQueues() {
//        p1q.update();
//        p2q.update();
//    }
}

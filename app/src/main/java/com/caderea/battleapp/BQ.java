package battle.battle;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * Created by Cade on 8/16/2014.
 */
public class BQ extends LinearLayout {
    QueueDrawer p1q, p2q;
    ProgressBar tickProgress;

    public BQ(Context context){
        super(context);

        init();
    }

    public BQ(Context context, AttributeSet attributeSet){
        super(context, attributeSet);

        init();
    }

    private void init(){
        p1q = (QueueDrawer)findViewById(R.id.player1QDrawer);
        p2q = (QueueDrawer)findViewById(R.id.player2QDrawer);
        tickProgress = (ProgressBar)findViewById(R.id.tickProgressBar);
    }

    public void setQueues(BattleQueue q1, BattleQueue q2){
        p1q.setQueue(q1);
        p2q.setQueue(q2);
    }

    public void updateTickProgress(int progress){
        tickProgress.setProgress(progress);
    }
}

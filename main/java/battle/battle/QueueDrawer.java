package battle.battle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Cade on 8/10/2014.
 */
public class QueueDrawer extends TextView {
        Rect area;
        Paint paint;
        BattleQueue queue;

    public QueueDrawer(Context context){
        super(context);
        init();
    }

    public QueueDrawer(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init(){
        area = new Rect();
        paint = new Paint();
        queue = new BattleQueue(); //setup an empty BattleQueue until Battle sets the queue for each QueueDrawer
    }

    protected void onDraw(Canvas canvas){

            int baseline = getLineBounds(0, area);
            paint.setTextSize(baseline - 5);

            Log.d("DRAWER","Baseline:"+baseline + ", area.left:"+area.left + ", area.right"+area.right + ", area.top"+area.top + ", area.bottom:"+area.bottom );
            //canvas.drawText("Text",area.left+5 , baseline, paint);

            //canvas.drawLine(startX, startY, stopX, stopY, paint);

            //horizontal line all the way accross the bottom
            canvas.drawLine(area.left, baseline, area.right, baseline, paint);
            float len = area.right/queue.size(); //length of each queue element
            for(int i = 0; i < queue.size();++i){
                float vertical = area.left + len * i;

                canvas.drawText(queue.getName(i), vertical + 5, baseline - 1, paint);
                canvas.drawLine(vertical, baseline, vertical, area.top, paint);
            }
    }

    public void setQueue(BattleQueue q){
        queue = q;
    }

    public void forceDraw(){
        invalidate();
    }
}

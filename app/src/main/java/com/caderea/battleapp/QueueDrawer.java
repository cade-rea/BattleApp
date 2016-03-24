package battle.battle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
        Paint rectPaint;

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
        rectPaint = new Paint();
        rectPaint.setColor(Color.YELLOW);
        queue = new BattleQueue(); //setup an empty BattleQueue until Battle sets the queue for each QueueDrawer
    }

    protected void onDraw(Canvas canvas){

            int baseline = getLineBounds(0, area);
            paint.setTextSize(baseline - 5);

            Log.d("DRAWER","Queue size:"+baseline + ", area.left:"+area.left + ", area.right"+area.right + ", area.top"+area.top + ", area.bottom:"+area.bottom );

            //canvas.drawLine(startX, startY, stopX, stopY, paint);

            //horizontal line all the way across the bottom
            canvas.drawLine(area.left, baseline, area.right, baseline, paint);
            //float len = area.right/queue.getMaxSize(); //length of each queue element
            float len = area.right/10; //see 10 ticks into the future

            BattleAction ba;

            for(int i = 0; i < queue.getMaxSize();++i){
                float vertical = area.left + len * i;

                ba = queue.get(i);

                if(ba != null) {
                    canvas.drawText(ba.toString(), vertical + 5, baseline - 1, paint);
                    canvas.drawLine(vertical, baseline, vertical, area.top, paint);
                    canvas.drawRect(vertical, area.top, vertical + (ba.getTime()*5), baseline, rectPaint);
                }
            }
    }

    public void setQueue(BattleQueue q){
        queue = q;
    }

    public void forceDraw(){
        invalidate();
    }
}

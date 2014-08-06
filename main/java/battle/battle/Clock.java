package battle.battle;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Cade on 7/31/2014.
 */
public class Clock implements Runnable {
    long time;
    int tick;
    int subTick;
    int factor;
    boolean going;

    public Clock(){
        time = 0;
        tick = 0;
        subTick = 0;
        factor = 5000; // uptimeMillis/factor
    }

    @Override
    public void run() {
        going = true;
        tick = 1;
        time = SystemClock.uptimeMillis()/factor;

        while(going){
            long rightNow = SystemClock.uptimeMillis();

            subTick = (int)((( (double)rightNow % (double)factor) / (double)factor) * 100); //percentage of tick for progress bar
            long nowish = rightNow / factor;

            if(time < nowish){
                time = nowish;
                ++tick;
            }
            else {
                SystemClock.sleep(factor/20);
            }
        }
    }

    public int getTick() {
        return tick;
    }

    public void stop(){
        going = false;
    }

    public int getProgress(){
        Log.d("CLOCK","Progress:" + (((double)SystemClock.uptimeMillis() % (double)factor) / (double)factor)*100);
        return subTick;
    }
}

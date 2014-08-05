package battle.battle;

import android.os.SystemClock;

/**
 * Created by Cade on 7/31/2014.
 */
public class Clock implements Runnable {
    long time;
    int tick;
    boolean going;
    int factor = 500; // uptimeMillis/factor

    public Clock(){
        time = 0;
        tick = 0;
    }

    @Override
    public void run() {
        going = true;
        tick = 1;
        time = SystemClock.uptimeMillis()/factor;

        while(going){
            long rightNow = SystemClock.uptimeMillis()/factor;
            if(time < rightNow){
                time = rightNow;
                ++tick;
            }
            else {
                SystemClock.sleep(50);
            }
        }
    }

    public int getTick() {
        return tick;
    }

    public void stop(){
        going = false;
    }
}

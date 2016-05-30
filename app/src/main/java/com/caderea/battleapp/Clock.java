package com.caderea.battleapp;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Cade on 7/31/2014.
 */
public class Clock implements Runnable {

    private static long SUBTICKS_PER_TICK = 4;
    private static long MILLISECONDS_PER_TICK = 1000;

    long time;
    int tick;
    long subTick;
    boolean going;

    public Clock(){
        time = 0;
        tick = 0;
        subTick = 0;
    }

    @Override
    public void run() {
        going = true;
        tick = 1;
        time = SystemClock.uptimeMillis() / MILLISECONDS_PER_TICK;

        while(going){
            long rightNow = SystemClock.uptimeMillis();

            long msSinceLastTick = rightNow % MILLISECONDS_PER_TICK;
            double tickFraction = (double) msSinceLastTick / (double) MILLISECONDS_PER_TICK;

            subTick = (int) (tickFraction * SUBTICKS_PER_TICK);

            long nowish = rightNow / MILLISECONDS_PER_TICK;

            if(time < nowish){
                time = nowish;
                ++tick;
            }
            else {
                SystemClock.sleep(MILLISECONDS_PER_TICK / (SUBTICKS_PER_TICK * 5));
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
        Log.d("CLOCK","Progress:" + SystemClock.uptimeMillis() % (double) SUBTICKS_PER_TICK / (double) SUBTICKS_PER_TICK * 100);
        return (int)( (double) subTick / (double) SUBTICKS_PER_TICK * 100);
    }
}

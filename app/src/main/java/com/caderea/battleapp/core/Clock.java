package com.caderea.battleapp.core;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Cade on 7/31/2014.
 */
public class Clock implements Runnable {
    private long time;
    private int tick;
    private long subTick;
    private boolean going;

    private long millisecondsPerTick;
    private long subticksPerTick;

    public Clock(long millisecondsPerTick, long subticksPerTick) {
        time = 0;
        tick = 0;
        subTick = 0;
        this.millisecondsPerTick = millisecondsPerTick;
        this.subticksPerTick = subticksPerTick;
    }

    @Override
    public void run() {
        going = true;
        tick = 1;
        time = SystemClock.uptimeMillis() / millisecondsPerTick;

        while(going) {
            long rightNow = SystemClock.uptimeMillis();

            long msSinceLastTick = rightNow % millisecondsPerTick;
            double tickFraction = (double) msSinceLastTick / (double) millisecondsPerTick;

            subTick = (int) (tickFraction * subticksPerTick);

            long nowish = rightNow / millisecondsPerTick;

            if(time < nowish) {
                time = nowish;
                ++tick;
            } else {
                SystemClock.sleep(millisecondsPerTick / (subticksPerTick * 5));
            }
        }
    }

    public int getTick() {
        return tick;
    }

    public void stop() {
        going = false;
    }

    public int getProgress() {
        Log.d("CLOCK","Progress:" + SystemClock.uptimeMillis() % (double) subticksPerTick / (double) subticksPerTick * 100);
        return (int)( (double) subTick / (double) subticksPerTick * 100);
    }
}

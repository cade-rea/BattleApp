package com.caderea.battleapp.core;

import android.os.SystemClock;

/**
 * Created by Cade on 7/31/2014.
 *
 * Runs on its own thread; other threads read tick and progress, so the shared fields are volatile.
 */
public class Clock implements Runnable {
    private long time;
    private volatile int tick;
    private volatile long subTick;
    private volatile boolean going;

    private long millisecondsPerTick;
    private long subticksPerTick;

    public Clock(long millisecondsPerTick, long subticksPerTick) {
        time = 0;
        tick = 0;
        subTick = 0;
        going = true;
        this.millisecondsPerTick = millisecondsPerTick;
        this.subticksPerTick = subticksPerTick;
    }

    @Override
    public void run() {
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
                SystemClock.sleep(Math.max(1, millisecondsPerTick / (subticksPerTick * 5)));
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
        return (int)( (double) subTick / (double) subticksPerTick * 100);
    }
}

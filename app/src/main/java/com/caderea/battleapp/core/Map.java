package com.caderea.battleapp.core;

import com.caderea.battleapp.activity.MapActivity;

import android.os.SystemClock;
import android.util.Log;

public class Map implements Runnable {

    private int gameTick;
    private Clock mapClock;
    private boolean going;

    private MapActivity mapActivity;

    private static final String TAG = "MAP";

    public Map(MapActivity mapActivity) {
        this.mapActivity = mapActivity;
        mapClock = new Clock(5000, 10);
        going = true;
    }

    @Override
    public void run() {
        initialize();
        mainLoop();
        complete();
    }

    private void complete() {
        mapClock.stop();
        notifyDone();
    }

    private void initialize() {
        Log.d(TAG,"running");
        //set this thread to background priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //start clock
        gameTick = 1;
        Thread clockThread = new Thread(mapClock);
        clockThread.start();
    }

    private void mainLoop() {
        //main game loop
        Log.d(TAG, "starting loop");
        while (going) {
            if (gameTick < mapClock.getTick()) {
                ++gameTick;
                doTick();
            } else {
                doNoTick();
            }
        }
    }

    private void doTick() {
        moveCircle();
    }

    private void doNoTick() {
        SystemClock.sleep(500);
    }

    private void moveCircle() {
        mapActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                mapActivity.moveCircle();
            }
        });
    }

    private void notifyDone() {
        mapActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                mapActivity.notifyDone();
            }
        });
    }

}

package com.caderea.battleapp.core;

import android.os.SystemClock;
import android.util.Log;
import com.caderea.battleapp.activity.MapActivity;

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
        Log.d(TAG,"running");
        //set this thread to background priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //start clock
        gameTick = 1;
        Thread clockThread = new Thread(mapClock);
        clockThread.start();

        //main game loop
        Log.d(TAG, "starting loop");
        mainLoop:
        while (going) {
            if (gameTick < mapClock.getTick()) {
                ++gameTick;
                moveCircle();
            } else {
                SystemClock.sleep(500);
            }
        }

        mapClock.stop();
        notifyDone();
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

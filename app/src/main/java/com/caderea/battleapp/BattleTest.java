package com.caderea.battleapp;

import android.os.Handler;

/**
 * Created by Cade on 8/3/2014.
 */
public class BattleTest implements Runnable {
    private BattleActivity screen;
    private Handler batHandler;
    private boolean buttonClicked;

    public BattleTest(BattleActivity scr, Handler bh) {
        screen = scr;
        batHandler = bh;

        buttonClicked = false;
    }

    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        for(int i = 0; i < 1000000; ++i) {
            if (i%1000 == 0) {

                final String s = "running " + i + " " + buttonClicked;

                batHandler.post(new Runnable() {
                    public void run() {
                        screen.update(s);
                    }
                });

                //Message msg = new Message();
                //msg.obj = s;
                //batHandler.sendMessage(msg);
            }
        }
    }

    public void setButtonClicked() {
        buttonClicked = true;
    }

}

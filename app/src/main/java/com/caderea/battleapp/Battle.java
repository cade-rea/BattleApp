package com.caderea.battleapp;

import android.util.Log;

/**
 * Created by Cade on 7/31/2014.
 */
public class Battle implements Runnable {
    private Fighter fighter1;
    private EnemyFighter fighter2;
    private Environment environment;
    private Clock battleClock;
    private int gameTick;
    private BattleQueue p1Actions, p2Actions;
    private BattleActivity battleActivity;
    private String status;
    private boolean going;

    private static final String TAG = "BATTLE";

    public Battle(BattleActivity battleActivity) {
        this.battleActivity = battleActivity;
        status = "";

        fighter1 = loadFighter();
        fighter2 = new EnemyFighter();

        p1Actions = new BattleQueue();
        p2Actions = new BattleQueue();

        this.battleActivity.setQueues(p1Actions, p2Actions);

        environment = new Environment();

        battleClock = new Clock();

        going = true;
        gameTick = 0;
    }

    public void run() {
        Log.d(TAG,"running");
        //set this thread to background priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //setup user actions
        updateButtons();

        //start clock
        gameTick = 1;
        Thread clockThread = new Thread(battleClock);
        clockThread.start();

        //main logic loop
        Log.d(TAG, "starting loop");
        while (going) {
            refreshQueues();

            if (gameTick < battleClock.getTick()) {
                ++gameTick;

                report("Tick:"+ gameTick + " " + fighter1.getName() + ":" + fighter1.getHealth() + " :: " +
                        fighter2.getName() + ":" + fighter2.getHealth());

                if (p1Actions.isNotEmpty()) {
                    Log.d(TAG, "p1 has action:" + p1Actions.toString());
                    BattleAction ba = p1Actions.poll();
                    ba.performAction(fighter2);
                    report("Player 1 uses " + ba.getName());
                }
                if (p2Actions.isNotEmpty()) {
                    Log.d(TAG, "p2 has action:" + p2Actions.toString());
                    BattleAction ba = p2Actions.poll();
                    ba.performAction(fighter1);
                    report("Player 2 uses " + ba.getName());
                }

                String s = "";

                if (fighter1.getHealth() <= 0) {
                    s += fighter1.getName() + " dies. ";
                    going = false;
                }
                if (fighter2.getHealth() <= 0) {
                    s += fighter2.getName() + " dies. ";
                    going = false;
                }

                report(s);

                //Bot Logic
                if (gameTick % 2 == 0) {
                    fighter2.doAction(p2Actions);
                }
            } else {
                //if a tick has not happened
                int st = battleClock.getProgress();
                Log.d(TAG,"No tick. Subtick:" + st);
                battleActivity.updateTickProgress(st);

                //sleep maybe?
            }
        }

        battleClock.stop();
        notifyDone();
    }

    /***
    Screen Reporting
     To call methods in BattleActivity, use:

     battleActivity.uiHandler.post(new Runnable() {
        @Override
        public void run() {
            battleActivity.METHOD_TO_CALL(r);
        }
        });


     These methods update the screen by calling corresponding methods in BattleActivity
     */


    private void report(final String r) {
        battleActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleActivity.update(r);
            }
        });
    }

    private void notifyDone() {
        battleActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleActivity.notifyDone();
            }
        });
    }

    private void updateTickProgress(final int progress) {
        battleActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleActivity.updateTickProgress(progress);
            }
        });
    }

    public void updateStatus(int a) {
        queueAction(a);
    }

    private void updateButtons() {
        battleActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleActivity.updateButtons(fighter1.getActions());
            }
        });
    }

    private void queueAction(int a) {
        Log.d(TAG,"queing action" + a +"::" + fighter1.getActions()[a]);
        p1Actions.offer(fighter1.getActions()[a]);
    }

    private void refreshQueues() {
        battleActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleActivity.refreshQueues();
            }
        });
    }

    public BattleQueue getQueue() {
        return p1Actions;
    }

    public void stop() {
        going = false;
    }

    private Fighter loadFighter() {
        return new Fighter();
    }
}

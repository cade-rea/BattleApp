package com.caderea.battleapp.core;

import android.os.SystemClock;
import android.util.Log;
import com.caderea.battleapp.activity.BattleActivity;
import com.caderea.battleapp.core.environment.Environment;
import com.caderea.battleapp.core.fighter.EnemyFighter;
import com.caderea.battleapp.core.fighter.Fighter;

/**
 * Created by Cade on 7/31/2014.
 */
public class Battle implements Runnable {
    private static final int MAGIC_TICK_FACTOR = 5;
    private Fighter fighter1;
    private EnemyFighter fighter2;
    private Environment environment;
    private Clock battleClock;
    private int gameTick;
    private BattleActivity battleActivity;
    private String status;
    private boolean going;

    private static final String TAG = "BATTLE";

    private static long MILLISECONDS_PER_TICK = 1000;
    private static long SUBTICKS_PER_TICK = 4;
    private static final long TICK_SLEEP_DURATION = MILLISECONDS_PER_TICK / (SUBTICKS_PER_TICK * MAGIC_TICK_FACTOR);

    public Battle(BattleActivity battleActivity) {
        this.battleActivity = battleActivity;
        status = "";

        fighter1 = loadFighter();
        fighter2 = new EnemyFighter();

        fighter1.setTarget(fighter2);
        fighter2.setTarget(fighter1);

        this.battleActivity.setQueues(fighter1.getQueue(), fighter2.getQueue());

        environment = new Environment();

        battleClock = new Clock(MILLISECONDS_PER_TICK, SUBTICKS_PER_TICK);

        going = true;
        gameTick = 0;
    }

    public void run() {

        initialize();

        mainLoop();

        endBattle();
    }

    private void initialize() {
        Log.d(TAG,"running");
        //set this thread to background priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //setup user actions
        updateButtons();

        //start clock
        gameTick = 1;
        Thread clockThread = new Thread(battleClock);
        clockThread.start();
    }

    private void mainLoop() {
        //main game loop
        Log.d(TAG, "starting loop");
        while (going) {
            refreshQueues();

            if (gameTick < battleClock.getTick()) {
                ++gameTick;

                doBattleTick();

            } else {
                doNoTick();
            }
        }
    }

    private void endBattle() {
        battleClock.stop();
        notifyDone();
    }

    private void doBattleTick() {
        report("Tick:"+ gameTick + " " + fighter1.getName() + ":" + fighter1.getHealth() + " :: " +
                fighter2.getName() + ":" + fighter2.getHealth());

        doFighterLoop();

        botLogic();
    }

    private void doFighterLoop() {
        Fighter[] fighters = {fighter1, fighter2};

        for (Fighter fighter: fighters) {
            if (fighter.getHealth() <= 0) {
                report(fighter.getName() + " has died.");
                going = false;
                return;
            }

            if (fighter.getQueue().isNotEmpty()) {
                report(fighter.performNextAction());
            }
        }
    }

    private void botLogic() {
        //Bot Logic
        if (gameTick % 2 == 0) {
            fighter2.addAttackToQueue();
        }
    }

    private void doNoTick() {
        //if a tick has not happened
        int st = battleClock.getProgress();
        Log.d(TAG,"No tick. Subtick:" + st);
        battleActivity.updateTickProgress(st);

        SystemClock.sleep(TICK_SLEEP_DURATION);
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

    private void updateButtons() {
        battleActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleActivity.updateButtons(fighter1.getActions());
            }
        });
    }

    public void queueAction(int action) {
        Log.d(TAG,"queing action" + action +"::" + fighter1.getActions()[action]);

            if (fighter1.getActions()[action] != null) {
                fighter1.getQueue().offer(new QueueAction(fighter1.getActions()[action]));
            }
    }

    private void refreshQueues() {
        battleActivity.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleActivity.refreshQueues();
            }
        });
    }

    public void stop() {
        going = false;
    }

    private Fighter loadFighter() {
        return new Fighter();
    }
}

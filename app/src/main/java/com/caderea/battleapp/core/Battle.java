package com.caderea.battleapp.core;

import android.os.SystemClock;
import android.util.Log;
import com.caderea.battleapp.core.action.BattleAction;
import com.caderea.battleapp.core.environment.Environment;
import com.caderea.battleapp.core.fighter.EnemyFighter;
import com.caderea.battleapp.core.fighter.Fighter;
import com.caderea.battleapp.view.battle.queue.QueueAction;

/**
 * Created by Cade on 7/31/2014.
 */
public class Battle implements Runnable {
    public static final long DEFAULT_MILLISECONDS_PER_TICK = 1000;

    private static final long SUBTICKS_PER_TICK = 4;
    // how many times per subtick the loop checks the clock
    private static final long CHECKS_PER_SUBTICK = 5;

    private static final String TAG = "BATTLE";

    private final Fighter fighter1;
    private final EnemyFighter fighter2;
    private final Environment environment;
    private final Clock battleClock;
    private final BattleListener listener;
    private final long tickSleepDuration;

    private int gameTick;
    private volatile boolean going;

    public Battle(BattleListener listener) {
        this(listener, DEFAULT_MILLISECONDS_PER_TICK);
    }

    public Battle(BattleListener listener, long millisecondsPerTick) {
        this.listener = listener;

        fighter1 = loadFighter();
        fighter2 = new EnemyFighter();

        fighter1.setTarget(fighter2);
        fighter2.setTarget(fighter1);

        this.listener.setQueues(fighter1.getQueue(), fighter2.getQueue());

        environment = new Environment();

        battleClock = new Clock(millisecondsPerTick, SUBTICKS_PER_TICK);
        tickSleepDuration = Math.max(1, millisecondsPerTick / (SUBTICKS_PER_TICK * CHECKS_PER_SUBTICK));

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
        listener.updateButtons(fighter1.getActions());

        //start clock
        gameTick = 1;
        Thread clockThread = new Thread(battleClock);
        clockThread.start();
    }

    private void mainLoop() {
        //main game loop
        Log.d(TAG, "starting loop");
        while (going) {
            listener.refreshQueues();

            if (gameTick < battleClock.getTick()) {
                tick();
            } else {
                doNoTick();
            }
        }
    }

    private void endBattle() {
        battleClock.stop();
        listener.notifyDone();
    }

    /**
     * Advances the battle by one tick. The main loop calls this whenever the clock ticks.
     */
    void tick() {
        ++gameTick;

        report("Tick:"+ gameTick + " " + fighter1.getName() + ":" + fighter1.getHealth() + " :: " +
                fighter2.getName() + ":" + fighter2.getHealth());

        doFighterLoop();

        if (going) {
            botLogic();
        }
    }

    private void doFighterLoop() {
        Fighter[] fighters = {fighter1, fighter2};

        for (Fighter fighter: fighters) {
            if (fighter.getQueue().isNotEmpty()) {
                String result = fighter.performNextAction();
                if (result != null) {
                    report(result);
                }
            }

            // check right after each action so a fighter that just died doesn't get to act
            Fighter target = fighter.getTarget();
            if (target.getHealth() <= 0) {
                report(target.getName() + " has died.");
                going = false;
                return;
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
        listener.updateTickProgress(battleClock.getProgress());

        SystemClock.sleep(tickSleepDuration);
    }

    private void report(String message) {
        listener.update(message);
    }

    public void queueAction(int action) {
        BattleAction[] actions = fighter1.getActions();

        if (action >= 0 && action < actions.length && actions[action] != null) {
            fighter1.getQueue().offer(new QueueAction(actions[action]));
        }
    }

    public void stop() {
        going = false;
    }

    public boolean isOver() {
        return !going;
    }

    public Fighter getPlayer() {
        return fighter1;
    }

    public EnemyFighter getEnemy() {
        return fighter2;
    }

    private Fighter loadFighter() {
        return new Fighter();
    }
}

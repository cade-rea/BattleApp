package battle.battle;

import android.util.Log;

import java.util.Queue;

/**
 * Created by Cade on 7/31/2014.
 */
public class Battle implements Runnable {
    private Fighter fighter1, fighter2;
    private Environment enviro;
    private Clock battleClock;
    private int gameTick;
    private BattleQueue p1Actions, p2Actions;
    private BattleScreen battleScreen;
    private String status;
    private boolean going;

    private static final String TAG = "BATTLE";

    public Battle(BattleScreen scr){
        battleScreen = scr;
        status = "";

        fighter1 = loadFighter();
        fighter2 = loadFighter();

        p1Actions = new BattleQueue();
        p2Actions = new BattleQueue();

        enviro = new Environment();

        battleClock = new Clock();

        going = true;
        gameTick = 0;
    }

    public void run(){
        Log.d(TAG,"running");
        //set this thread to background priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //setup user actions
        battleScreen.updateButtons(fighter1.getActions());

        //start clock
        gameTick = 1;
        Thread clockThread = new Thread(battleClock);
        clockThread.start();

        //main logic loop
        Log.d(TAG, "starting loop");
        while(going) {
            Log.d(TAG, "going...");
            if(gameTick < battleClock.getTick()) {
                ++gameTick;


                String s = "Tick:"+ gameTick + " " + fighter1.getName() + ":" + fighter1.getHealth() + " :: " + fighter2.getName() + ":" + fighter2.getHealth();
                report(s);

                if(p1Actions.isNotEmpty()){
                    Log.d(TAG, "p1 has action:" + p1Actions.toString());
                    BattleAction ba = p1Actions.poll();
                    ba.performAction(fighter2);
                    report("Player 1 uses " + ba.getName());
                }
                if(p2Actions.isNotEmpty()){
                    Log.d(TAG, "p2 has action:" + p2Actions.toString());
                    BattleAction ba = p2Actions.poll();
                    ba.performAction(fighter1);
                    report("Player 1 uses " + ba.getName());
                }

                s = "";

                if (fighter1.getHealth() <= 0) {
                    s += fighter1.getName() + " dies. ";
                    going = false;
                }
                if (fighter2.getHealth() <= 0) {
                    s += fighter2.getName() + " dies. ";
                    going = false;
                }

                report(s);
            }
            else{
                //if a tick has not happened
                int st = battleClock.getProgress();
                Log.d(TAG,"No tick. Subtick:" + st);
                updateTickProgress(st);

                //sleep maybe?
            }
        }

        battleClock.stop();
        notifyDone();
    }

    /***
    Screen Reporting
     To call methods in BattleScreen, use:

     battleScreen.uiHandler.post(new Runnable() {
        @Override
        public void run() {
            battleScreen.METHOD_TO_CALL(r);
        }
        });


     These methods update the screen by calling corresponding methods in BattleScreen
     */


    private void report(final String r){
        battleScreen.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleScreen.update(r);
            }
        });
    }

    private void notifyDone(){
        battleScreen.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleScreen.notifyDone();
            }
        });
    }

    private void updateTickProgress(final int progress){
        battleScreen.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                battleScreen.updateTickProgress(progress);
            }
        });
    }

    public void updateStatus(int a){
        queueAction(a);
    }

    private void queueAction(int a){
        Log.d(TAG,"queing action" + a +"::" + fighter1.getActions()[a]);
        p1Actions.offer(fighter1.getActions()[a]);
    }

    public void stop(){
        going = false;
    }

    private Fighter loadFighter(){
        return new Fighter();
    }
}

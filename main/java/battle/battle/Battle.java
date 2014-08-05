package battle.battle;

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
        //set this thread to background priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //setup user actions
        battleScreen.updateButtons(fighter1.getActions());

        //start clock
        gameTick = 1;
        Thread clockThread = new Thread(battleClock);
        clockThread.start();

        //main logic loop
        while(going) {
            if(gameTick < battleClock.getTick()) {
                ++gameTick;

                String s = "Tick:"+ gameTick + " " + fighter1.getName() + ":" + fighter1.getHealth() + " :: " + fighter2.getName() + ":" + fighter2.getHealth();
                report(s);

                if(p1Actions.isNotEmpty()){
                    BattleAction ba = p1Actions.poll();
                    ba.performAction(fighter2);
                    report("Player 1 uses " + ba.getName());
                }
                if(p2Actions.isNotEmpty()){
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
                //sleep maybe?
            }
        }

        battleClock.stop();
        notifyDone();
    }

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

    public void updateStatus(int a){
        queueAction(a);
    }

    private void queueAction(int a){
        p1Actions.offer(fighter1.getActions()[a]);
    }

    public void stop(){
        going = false;
    }

    private Fighter loadFighter(){
        return new Fighter();
    }
}

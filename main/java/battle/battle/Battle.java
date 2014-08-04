package battle.battle;

import android.os.AsyncTask;
import android.os.Handler;

/**
 * Created by Cade on 7/31/2014.
 */
public class Battle implements Runnable {
    private Fighter fighter1, fighter2;
    private Environment enviro;
    private Clock battleClock;
    private int gameTick;

    private BattleScreen battleScreen;
    private String status;
    private boolean going;

    public Battle(BattleScreen scr){//}, Handler h){
        battleScreen = scr;
        status = "";

        fighter1 = loadFighter();
        fighter2 = loadFighter();

        enviro = new Environment();

        battleClock = new Clock();

        going = true;
        gameTick = 0;
    }

    public void run(){
        //set this thread to background priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        gameTick = 1;

        Thread clockThread = new Thread(battleClock);
        clockThread.start();

        //main logic loop
        while(going) {
            if(gameTick < battleClock.getTick()) {
                ++gameTick;

                String s = "Tick:"+ gameTick + " " + fighter1.getName() + ":" + fighter1.getHealth() + " :: " + fighter2.getName() + ":" + fighter2.getHealth();
                report(s);

                fighter1.health -= fighter2.getStr();
                fighter2.health -= fighter1.getStr();

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

    public void updateStatus(String s){
        status = s;
    }

    public void stop(){
        going = false;
    }

    private Fighter loadFighter(){
        return new Fighter();
    }
}

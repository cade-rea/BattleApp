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
    }

    public void run(){
        //set this thread to background priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //main logic loop
        for(int i = 0; going; ++i){
            if (i%1000 == 0){
                String s = i + " " + status + " " + going;

                report(s);
            }
        }

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

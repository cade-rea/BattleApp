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
    private Handler uiHandler;
    private String status;

    public Battle(BattleScreen scr, Handler h){
        battleScreen = scr;
        uiHandler = h;
        status = "";

        fighter1 = loadFighter();
        fighter2 = loadFighter();

        enviro = new Environment();

        battleClock = new Clock();
    }

    public void run(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        for(int i = 0; i < 1000000; ++i){
            if (i%1000 == 0){
                final String s = i + " " + status;

                uiHandler.post(new Runnable(){
                    public void run(){
                        battleScreen.update(s);
                    }
                });
            }
        }
    }

    private Fighter loadFighter(){
        return new Fighter();
    }
}

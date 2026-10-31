package com.caderea.battleapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.caderea.battleapp.R;
import com.caderea.battleapp.core.Battle;
import com.caderea.battleapp.core.BattleListener;
import com.caderea.battleapp.core.BattleQueue;
import com.caderea.battleapp.core.action.BattleAction;
import com.caderea.battleapp.view.battle.BattleViewGroup;

import java.util.ArrayList;

/**
 * Battle screen. The Battle runs on its own thread and reports back through the
 * BattleListener methods, which hand each update to the UI thread.
 */
public class BattleActivity extends Activity implements BattleListener {
    private static final int MAX_LOG_LINES = 10;

    private ArrayList<String> output;

    private Battle battle;
    private Button[] battleButtons;
    private BattleViewGroup battleViewGroup;
    private TextView battleLog;

    //refers to the UI thread, so other threads can post work to it
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_screen);

        output = new ArrayList<>(MAX_LOG_LINES);

        battleButtons = new Button[4];
        battleButtons[0] = findViewById(R.id.button1);
        battleButtons[1] = findViewById(R.id.button2);
        battleButtons[2] = findViewById(R.id.button3);
        battleButtons[3] = findViewById(R.id.button4);

        battleViewGroup = findViewById(R.id.battleViewGroup);
        battleLog = findViewById(R.id.battleLog);

        uiHandler = new Handler(Looper.getMainLooper());

        //create a new Battle and pass this activity
        long millisecondsPerTick = getResources().getInteger(R.integer.tick_length_ms);
        battle = new Battle(this, millisecondsPerTick);
        //start the battle
        (new Thread(battle)).start();
    }

    @Override
    protected void onDestroy() {
        //otherwise the battle thread keeps running after the screen is gone
        battle.stop();
        super.onDestroy();
    }

    /*
     * BattleListener: called from the battle thread
     */

    @Override
    public void update(final String msg) {
        uiHandler.post(() -> addLogLine(msg));
    }

    @Override
    public void notifyDone() {
        uiHandler.post(this::showDone);
    }

    @Override
    public void updateButtons(final BattleAction[] actions) {
        uiHandler.post(() -> showButtons(actions));
    }

    @Override
    public void updateTickProgress(final int progress) {
        uiHandler.post(() -> battleViewGroup.updateTickProgress(progress));
    }

    @Override
    public void refreshQueues() {
        uiHandler.post(() -> battleViewGroup.refreshQueues());
    }

    //called from the Battle constructor, which runs on the UI thread in onCreate
    @Override
    public void setQueues(BattleQueue q1, BattleQueue q2) {
        battleViewGroup.setQueues(q1, q2);
        battleViewGroup.refreshQueues();
    }

    /*
     * UI thread
     */

    private void addLogLine(String msg) {
        while(output.size() >= MAX_LOG_LINES) {
            output.remove(0);
        }

        output.add(msg);

        StringBuilder sb = new StringBuilder();
        for(String s:output) {
            sb.append(s).append("\n");
        }

        Log.d("BATTLE", "update: " + msg);
        battleLog.setText(sb.toString());
    }

    private void showDone() {
        addLogLine("Battle Finished");

        //change the stop button to return to the main menu
        Button btn = findViewById(R.id.buttonStop);
        btn.setText("Return");
        btn.setOnClickListener(view -> finish());
    }

    private void showButtons(BattleAction[] actions) {
        for (int i = 0; i < actions.length; ++i) {
            if (actions[i] != null) {
                battleButtons[i].setText(actions[i].getName());
            } else {
                battleButtons[i].setText("");
            }
        }
    }

    public void buttonPushed(View view) {
        switch(view.getId()) {
            case R.id.button1:
                battle.queueAction(0);
                break;
            case R.id.button2:
                battle.queueAction(1);
                break;
            case R.id.button3:
                battle.queueAction(2);
                break;
            case R.id.button4:
                battle.queueAction(3);
                break;
            case R.id.buttonStop:
                stopBattle();
                break;
            default:
                addLogLine("default");
                break;
        }
    }

    private void stopBattle() {
        battle.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.battle_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

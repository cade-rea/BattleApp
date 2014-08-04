package battle.battle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import battle.battle.R;

public class BattleScreen extends Activity {
    private ArrayList<String> output;
    private TextView battleText;
    private Battle battle;
    private BattleTest bt;

    private float dpHeight;
    private float dpWidth;

    private Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_screen);

        //get screen resolution in dp
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        //initialize the string to hold output and the textview to show it
        output = new ArrayList<String>(10);
        battleText = (TextView)findViewById(R.id.battle_display);
        battleText.setHeight((int)(dpHeight*3/4));

        //battle = new Battle(this);
        //battle.execute("Param");


        //this handler can be passed to other threads to refer to the UI thread
        UIHandler = new Handler(){
            //responds to UIHandler.sendMessage()
            public void handleMessage(Message msg){
                update((String)msg.obj);
            }
        };

        //create a new battleTest and pass this activity and the UIHandler
        //probably don't need to pass the activity, probably just the handler
        bt = new BattleTest(this, UIHandler);


        Thread thread = new Thread(bt);
        thread.start();



/*
        //this handler is created on the UI thread
        //any calls to this thread will be on UI thread
        final Handler myHandler = new Handler();

        //this creates a new thread
        (new Thread(
                new Runnable() { //executed on new thread
                    @Override
                    public void run() { //new thread
                        final int b = 1;
                        myHandler.post(new Runnable() { //this calls the handler on the UI thread
                            @Override
                            public void run() {//this is run on the UI thread
                                mImageView.setImageBitmap(b);
                            }
                        });
                    }
                })
        ).start();//this starts the execution of the new thread


*/
    }

    public void update(String msg){
        while(output.size() > 9){
            output.remove(0);
        }

        output.add(msg);

        String str = "";
        for(String s:output){
            str += s +"\n";
        }

        battleText.setText(str);
    }

    public void buttonPushed(View view){
        switch(view.getId()){
            case R.id.button1:
                update("button 1");
                break;
            case R.id.button2:
                update("button 2");
                bt.setButtonClicked();
                break;
            case R.id.button3:
                update("button 3");
                break;
            case R.id.button4:
                update("button 4");
                exitToMenu();
                break;
            default:
                update("default");
                break;
        }
    }

    private void exitToMenu(){
        //battle.cancel(false);
        startActivity(new Intent(this, main_menu.class));
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

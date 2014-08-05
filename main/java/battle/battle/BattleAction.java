package battle.battle;

import android.view.View;

/**
 * Created by Cade on 8/4/2014.
 */
public class BattleAction {
    private String name;
    private int power;
    private Fighter owner;

    private View.OnClickListener action;

    public BattleAction(Fighter f){
        name="Attack";
        power = 1;
        owner = f;

        action = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
    }

    public String getName(){
        return name;
    }

    public int getPower(){
        return power;
    }

    public void performAction(Fighter p2){
        p2.health -= owner.getStr();
    }
}

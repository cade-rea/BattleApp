package com.caderea.battleapp;

import android.view.View;

/**
 * Created by Cade on 8/4/2014.
 */
public class BattleAction {
    private String name;
    private int power;
    private Fighter owner;
    private int time;

    public BattleAction(Fighter f){
        name="Attack";
        power = 1;
        owner = f;
        time = 2;
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

    public String toString(){
        return "Action:" + name;
    }

    public int getTime(){
        return time;
    }
}

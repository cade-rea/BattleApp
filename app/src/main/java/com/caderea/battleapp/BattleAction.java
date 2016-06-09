package com.caderea.battleapp;

import android.view.View;

/**
 * Created by Cade on 8/4/2014.
 */
public class BattleAction {
    protected String name;
    protected int power;
    protected Fighter owner;
    protected int time;

    public BattleAction(Fighter f) {
        this.name = "Attack";
        this.power = 1;
        this.owner = f;
        this.time = 2;
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public void performAction(Fighter p2) {
        p2.health -= owner.getStr();
    }

    public String toString() {
        return "Action:" + name;
    }

    public int getTime() {
        return time;
    }
}

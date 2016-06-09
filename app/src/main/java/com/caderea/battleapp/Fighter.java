package com.caderea.battleapp;

/**
 * Created by Cade on 7/31/2014.
 */
public class Fighter {
    protected String name;
    protected int str;
    protected int health;

    protected BattleAction[] actions;

    public Fighter() {
        this("default",1,20);
    }

    public Fighter(String n, int s, int h) {
        name = n;
        str = s;
        health = h;

        actions = new BattleAction[4];

        for(int i = 0; i < 4; ++i)
            actions[i] = new BattleAction(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStr(int str) {
        this.str = str;
    }

    public int getStr() {
        return str;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public BattleAction[] getActions() {
        return actions;
    }

    public void setActions(BattleAction[] actions) {
        this.actions = actions;
    }
}

package com.caderea.battleapp;

/**
 * Created by Cade on 7/31/2014.
 */
public class Fighter {
    private String name;
    private int str;
    public int health;

    private BattleAction[] actions;

    public Fighter(){
        this("default",1,20);
    }

    public Fighter(String n, int s, int h){
        name = n;
        str = s;
        health = h;

        actions = new BattleAction[4];

        for(int i = 0; i < 4; ++i)
            actions[i] = new BattleAction(this);
    }

    public String getName(){
        return name;
    }

    public int getStr(){
        return str;
    }

    public int getHealth(){
        return health;
    }

    public BattleAction[] getActions(){
        return actions;
    }
}

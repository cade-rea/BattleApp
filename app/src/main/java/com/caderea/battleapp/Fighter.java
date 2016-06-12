package com.caderea.battleapp;

/**
 * Created by Cade on 7/31/2014.
 */
public class Fighter {
    protected String name;
    protected int power;
    protected int health;

    protected Fighter target;

    protected BattleAction[] actions;

    protected BattleQueue queue;

    public Fighter() {
        this("Default Fighter",1,20);
    }

    public Fighter(String name, int power, int health) {
        this.name = name;
        this.power = power;
        this.health = health;

        actions = new BattleAction[4];
        queue = new BattleQueue();

        for(int i = 0; i < 4; ++i)
            actions[i] = new Attack();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getPower() {
        return power;
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

    public BattleQueue getQueue() {
        return queue;
    }

    public Fighter getTarget() {
        return target;
    }

    public void setTarget(Fighter target) {
        this.target = target;
    }

    public String performNextAction() {
        return queue.poll().getBattleAction().performAction(this, target);
    }
}

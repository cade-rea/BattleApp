package com.caderea.battleapp.core.fighter;

import com.caderea.battleapp.core.action.Attack;
import com.caderea.battleapp.core.action.BattleAction;
import com.caderea.battleapp.core.action.Heal;
import com.caderea.battleapp.core.BattleQueue;

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
        this("Default Fighter", 1, 10);
    }

    public Fighter(String name, int power, int health) {
        this.name = name;
        this.power = power;
        this.health = health;

        queue = new BattleQueue();
        actions = new BattleAction[4];

        actions[0] = new Attack();
        actions[1] = new Heal();
        actions[2] = null;
        actions[3] = null;
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

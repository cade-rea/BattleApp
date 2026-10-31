package com.caderea.battleapp.core.fighter;

import com.caderea.battleapp.core.action.Attack;
import com.caderea.battleapp.core.action.BattleAction;
import com.caderea.battleapp.core.action.Heal;
import com.caderea.battleapp.core.BattleQueue;
import com.caderea.battleapp.view.battle.queue.QueueAction;

/**
 * Created by Cade on 7/31/2014.
 */
public class Fighter {
    protected String name;

    protected int power;
    protected int health;
    protected int maxHealth;

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
        this.maxHealth = health;

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

    public int getMaxHealth() {
        return maxHealth;
    }

    // health stays between 0 and maxHealth, so healing can't overflow
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
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

    /**
     * Performs the next queued action.
     * @return a description of what happened, or null if the queue was empty
     */
    public String performNextAction() {
        QueueAction next = queue.poll();
        if (next == null) {
            return null;
        }

        return next.getBattleAction().performAction(this, target);
    }
}

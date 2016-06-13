package com.caderea.battleapp.action;

import com.caderea.battleapp.fighter.Fighter;

/**
 * Created by Cade on 8/4/2014.
 */
public abstract class BattleAction {
    protected String name;
    protected int duration;

    public abstract String performAction(Fighter performer, Fighter target);

    protected BattleAction() {
        name = "";
        duration = 0;
    }

    protected BattleAction(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public String toString() {
        return "Action: " + name + ", Duration: " + duration;
    }

}

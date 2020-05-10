package com.caderea.battleapp.core.action;

import com.caderea.battleapp.core.fighter.Fighter;
import lombok.Getter;

/**
 * Created by Cade on 8/4/2014.
 */
@Getter
public abstract class BattleAction {
    protected String name;
    protected int duration;

    public abstract String performAction(Fighter performer, Fighter target);

    protected BattleAction() {
        this("", 0);
    }

    protected BattleAction(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String toString() {
        return "Action: " + name + ", Duration: " + duration;
    }

}

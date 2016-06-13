package com.caderea.battleapp.action;

import com.caderea.battleapp.fighter.Fighter;

/**
 * Created by cade-home on 6/11/16.
 */
public class Attack extends BattleAction {
    protected int power;

    public Attack() {
        super("Attack", 1);
        power = 1;
    }

    public int getPower() {
        return power;
    }

    public String performAction(Fighter performer, Fighter target) {
        int damage = this.power * performer.getPower();

        target.setHealth(
                target.getHealth() - damage
        );

        return performer.getName() + " attacks " + target.getName() + " for " + damage + " damage.";
    }
}

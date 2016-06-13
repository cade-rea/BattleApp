package com.caderea.battleapp.action;

import com.caderea.battleapp.fighter.Fighter;

/**
 * Created by cade-home on 6/12/16.
 */
public class Heal extends BattleAction {
    protected int power;

    public Heal() {
        super("Heal", 1);
        power = 1;
    }

    public int getPower() {
        return power;
    }

    @Override
    public String performAction(Fighter performer, Fighter target) {
        int heal = this.power * performer.getPower();

        performer.setHealth(
                performer.getHealth() + heal
        );

        return performer.getName() + " heals for " + heal + " health.";
    }
}

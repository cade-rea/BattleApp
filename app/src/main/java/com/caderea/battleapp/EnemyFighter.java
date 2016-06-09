package com.caderea.battleapp;

/**
 * Created by cade-home on 6/8/16.
 */
public class EnemyFighter extends Fighter {

    public void doAction(BattleQueue battleActions) {
        battleActions.add(this.actions[0]);
    }
}

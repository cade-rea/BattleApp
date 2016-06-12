package com.caderea.battleapp;

/**
 * Created by cade-home on 6/12/16.
 */
public class QueueAction {
    private BattleAction battleAction;

    public QueueAction(BattleAction battleAction) {
        this.battleAction = battleAction;
    }

    public BattleAction getBattleAction() {
        return battleAction;
    }
}

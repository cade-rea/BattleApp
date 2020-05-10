package com.caderea.battleapp.core.fighter;

import com.caderea.battleapp.view.battle.queue.QueueAction;

/**
 * Created by cade-home on 6/8/16.
 */
public class EnemyFighter extends Fighter {

    public EnemyFighter() {
        super("Enemy", 1, 10);
    }

    public void addAttackToQueue() {
        queue.offer(new QueueAction(actions[0]));
    }
}

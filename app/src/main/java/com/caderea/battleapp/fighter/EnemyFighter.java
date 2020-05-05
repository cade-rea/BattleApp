package com.caderea.battleapp.fighter;

import com.caderea.battleapp.queue.QueueAction;

/**
 * Created by cade-home on 6/8/16.
 */
public class EnemyFighter extends Fighter {

    public EnemyFighter() {
        super("Enemy", 1, 10);
    }

    public void addAttackToQueue() {
        queue.offer(new QueueAction(actions[0]));
        queue.offer(new QueueAction(actions[0]));
        queue.offer(new QueueAction(actions[0]));
    }
}

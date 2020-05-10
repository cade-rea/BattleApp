package com.caderea.battleapp.core.fighter;

/**
 * Created by cade-home on 6/8/16.
 */
public class EnemyFighter extends Fighter {

    public EnemyFighter() {
        super("Enemy", 1, 10);
    }

    public void addAttackToQueue() {
        queue.offer(actions[0]);
    }
}

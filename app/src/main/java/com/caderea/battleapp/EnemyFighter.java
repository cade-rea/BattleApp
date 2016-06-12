package com.caderea.battleapp;

/**
 * Created by cade-home on 6/8/16.
 */
public class EnemyFighter extends Fighter {

    public EnemyFighter() {
        super("Enemy", 1, 20);
    }

    public void addAttackToQueue() {
        queue.offer(new QueueAction(actions[0]));
    }
}

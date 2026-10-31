package com.caderea.battleapp.core.fighter

class EnemyFighter : Fighter("Enemy", 1, 10) {

    fun addAttackToQueue() {
        actions[0]?.let(queue::offer)
    }
}

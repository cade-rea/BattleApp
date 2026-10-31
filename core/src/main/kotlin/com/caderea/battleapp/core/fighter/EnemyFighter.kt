package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.color.RgbColor

class EnemyFighter(color: RgbColor = RgbColor.BLUE) : Fighter(
    name = "Enemy",
    color = color,
    actions = listOf(Attack("Strike", color)),
) {

    fun addAttackToQueue() {
        actions[0]?.let(queue::offer)
    }
}

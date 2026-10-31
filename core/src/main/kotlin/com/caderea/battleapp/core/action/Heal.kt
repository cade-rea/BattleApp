package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.fighter.Fighter

class Heal(val power: Int = 1) : BattleAction("Heal", 1) {

    override fun performAction(performer: Fighter, target: Fighter): String {
        val heal = power * performer.power
        performer.health += heal

        return "${performer.name} heals for $heal health."
    }
}

package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.fighter.Fighter

class Attack(val power: Int = 1) : BattleAction("Attack", 1) {

    override fun performAction(performer: Fighter, target: Fighter): String {
        val damage = power * performer.power
        target.health -= damage

        return "${performer.name} attacks ${target.name} for $damage damage."
    }
}

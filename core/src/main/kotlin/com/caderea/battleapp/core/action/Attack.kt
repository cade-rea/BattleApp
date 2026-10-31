package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.color.Affinity
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.color.TypeWheel
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import kotlin.math.roundToInt

class Attack(name: String = "Attack", color: RgbColor) : BattleAction(name, color) {

    /**
     * Damage against [target]: the color's power after type matchups against the target's
     * aura, times the color's strength trait, the performer's aura affinity and the light.
     */
    fun damage(performer: Fighter, target: Fighter, environment: Environment): Int =
        (TypeWheel.typedPower(color, target.color) *
            traits.strength *
            Affinity.multiplier(color, performer.color) *
            environment.lightMultiplier(color)).roundToInt()

    override fun performAction(performer: Fighter, target: Fighter, environment: Environment): String {
        val damage = damage(performer, target, environment)
        target.health -= damage

        return "${performer.name} attacks ${target.name} with $name for $damage damage."
    }
}

package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.color.Affinity
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import com.caderea.battleapp.core.technique.TechniqueRolls
import kotlin.math.roundToInt

class Heal(
    name: String = "Heal",
    color: RgbColor,
    rolls: TechniqueRolls = TechniqueRolls.NEUTRAL,
) : BattleAction(name, color, rolls) {

    /**
     * Health restored: a share of the color's power, times its support trait, the
     * technique's potency roll, the performer's aura affinity and the light. Heals have no
     * type matchup.
     */
    fun amount(performer: Fighter, environment: Environment): Int =
        (color.power * POWER_TO_HEALTH *
            traits.support *
            rolls.potency *
            Affinity.multiplier(color, performer.color) *
            environment.lightMultiplier(color)).roundToInt()

    override fun performAction(performer: Fighter, target: Fighter, environment: Environment): String {
        val heal = amount(performer, environment)
        performer.health += heal

        return "${performer.name} heals with $name for $heal health."
    }

    companion object {
        // heals restore less than an attack of the same power takes, so they can't stall a
        // fight forever; a starting value to playtest
        const val POWER_TO_HEALTH = 0.5
    }
}

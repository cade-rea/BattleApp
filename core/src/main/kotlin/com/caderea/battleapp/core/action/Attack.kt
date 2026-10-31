package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.color.Affinity
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.color.TypeWheel
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import com.caderea.battleapp.core.technique.TechniqueRolls
import kotlin.math.roundToInt

class Attack(
    name: String = "Attack",
    color: RgbColor,
    rolls: TechniqueRolls = TechniqueRolls.NEUTRAL,
) : BattleAction(name, color, rolls) {

    /**
     * Damage against [target] before guards: the color's power after type matchups against
     * the target's aura, times the color's strength trait, the technique's potency roll, the
     * performer's aura affinity and the light.
     */
    fun damage(performer: Fighter, target: Fighter, environment: Environment): Int =
        (TypeWheel.typedPower(color, target.color) *
            traits.strength *
            rolls.potency *
            Affinity.multiplier(color, performer.color) *
            environment.lightMultiplier(color)).roundToInt()

    override fun performAction(performer: Fighter, target: Fighter, environment: Environment): String {
        val damage = damage(performer, target, environment)
        val guard = target.activeGuard
        val blocked = guard?.blocked(damage, color) ?: 0
        target.health -= damage - blocked

        val hit = "${performer.name} attacks ${target.name} with $name for ${damage - blocked} damage"
        return if (guard != null && blocked > 0) "$hit ($blocked blocked by ${guard.guard.name})." else "$hit."
    }
}

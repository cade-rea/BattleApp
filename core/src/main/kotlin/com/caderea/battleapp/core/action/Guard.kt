package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.color.Affinity
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import kotlin.math.roundToInt

/**
 * A defense tinted with a color. Once it lands, it stays up for [Guard.DURATION_TICKS] ticks
 * and blocks part of every attack that shares its color. A guard in the wrong color blocks
 * nothing, so guarding is a read of the opponent's telegraph.
 */
class Guard(name: String = "Guard", color: RgbColor) : BattleAction(name, color) {

    /**
     * The share of a matching attack this guard blocks when [performer] raises it: its
     * support trait (green guards best), the performer's aura affinity and the light.
     */
    fun strength(performer: Fighter, environment: Environment): Double =
        BASE_BLOCK *
            traits.support *
            Affinity.multiplier(color, performer.color) *
            environment.lightMultiplier(color)

    override fun performAction(performer: Fighter, target: Fighter, environment: Environment): String {
        performer.activeGuard = ActiveGuard(this, strength(performer, environment))
        return "${performer.name} raises $name."
    }

    companion object {
        /** The share a guard blocks before traits and bonuses. A starting value to playtest. */
        const val BASE_BLOCK = 0.5
        /** No guard blocks more than this, so attacking into a guard still does something. */
        const val MAX_BLOCK = 0.9
        /** Ticks a guard stays up, counting the tick it lands on. */
        const val DURATION_TICKS = 2
    }
}

/**
 * A guard that has landed and is up for [ticksLeft] more ticks, counting the current one.
 */
data class ActiveGuard(
    val guard: Guard,
    val strength: Double,
    val ticksLeft: Int = Guard.DURATION_TICKS,
) {

    /**
     * How much of [damage] from an attack of [color] this guard blocks. It scales with how
     * much the colors overlap, and a guard dimmer than the attack blocks proportionally less.
     */
    fun blocked(damage: Int, color: RgbColor): Int {
        if (color.power == 0) return 0

        val brightness = minOf(1.0, guard.color.power.toDouble() / color.power)
        val share = (strength * guard.color.overlap(color) * brightness).coerceAtMost(Guard.MAX_BLOCK)
        return (damage * share).roundToInt()
    }
}

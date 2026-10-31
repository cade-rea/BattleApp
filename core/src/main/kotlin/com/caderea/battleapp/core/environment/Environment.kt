package com.caderea.battleapp.core.environment

import com.caderea.battleapp.core.color.RgbColor

/**
 * Where a battle takes place. Besides its light, an arena sets how many techniques each
 * fighter brings and can cap levels so a fight is even.
 *
 * @param loadoutSize techniques each fighter brings, from [MIN_LOADOUT] to [MAX_LOADOUT]
 * @param levelCap if set, every fighter fights as if no higher than this level: their
 * techniques are dimmed to that level's power budget
 * @param lights the arena's light. With more than one, the light turns to the next one
 * every [ticksPerLight] ticks and starts over after the last.
 */
open class Environment(
    val loadoutSize: Int = MIN_LOADOUT,
    val levelCap: Int? = null,
    val lights: List<Light> = listOf(Light.PLAIN),
    val ticksPerLight: Int = 0,
) {

    init {
        require(loadoutSize in MIN_LOADOUT..MAX_LOADOUT) { "loadout size must be $MIN_LOADOUT-$MAX_LOADOUT: $loadoutSize" }
        require(levelCap == null || levelCap >= 1) { "level cap must be at least 1: $levelCap" }
        require(lights.isNotEmpty()) { "an arena needs a light" }
        require(lights.size == 1 || ticksPerLight >= 1) { "a turning light needs ticks per light: $ticksPerLight" }
    }

    /** Whether the light turns during the battle. */
    val isTurning: Boolean
        get() = lights.size > 1

    /** The light while tick number [tick] resolves. The first tick of a battle is 1. */
    fun lightAt(tick: Int): Light =
        if (isTurning) lights[phase(tick) / ticksPerLight % lights.size] else lights.first()

    /**
     * How many ticks from [tick] until the light turns, counting [tick] itself, or null if it
     * never turns. 1 means [tick] is the last one in this light.
     */
    fun ticksLeftInLight(tick: Int): Int? =
        if (isTurning) ticksPerLight - phase(tick) % ticksPerLight else null

    /** The light after the current one at [tick], or null if it never turns. */
    fun nextLight(tick: Int): Light? =
        if (isTurning) lightAt(tick + ticksLeftInLight(tick)!!) else null

    /**
     * This environment as it stands while tick number [tick] resolves, with the light fixed
     * at that tick's. Actions only see this snapshot, so a turning light needs no clock in
     * the actions themselves.
     */
    open fun at(tick: Int): Environment =
        if (isTurning) Environment(loadoutSize, levelCap, listOf(lightAt(tick))) else this

    /**
     * The multiplier the arena's light gives a technique of [color]. For a turning light this
     * is its first light; use [at] for the light at a given tick.
     */
    open fun lightMultiplier(color: RgbColor): Double = lights.first().multiplier(color)

    // ticks are numbered from 1, so the first light covers ticks 1 to ticksPerLight; the
    // moment before the first tick counts as the first tick
    private fun phase(tick: Int) = (tick - 1).coerceAtLeast(0)

    companion object {
        const val MIN_LOADOUT = 4
        const val MAX_LOADOUT = 6
    }
}

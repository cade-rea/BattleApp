package com.caderea.battleapp.core.progression

import com.caderea.battleapp.core.color.RgbColor
import kotlin.math.sqrt

/**
 * How total aura XP turns into a level, and what a level gives: a bigger power budget for new
 * techniques and wider variance when they are rolled. All starting values to playtest.
 */
object Levels {

    const val MAX_LEVEL = 50
    /** XP a new fighter starts with, all in their base color. */
    const val STARTING_XP = 60
    /** Level n+1 takes n times this much more XP than level n did. */
    const val XP_STEP = 40
    /** Power budget gained per level, on top of [RgbColor.STARTING_POWER_BUDGET]. */
    const val BUDGET_PER_LEVEL = 10

    /** The total XP that reaches [level]: level 2 at 100, 3 at 180, 5 at 460, 10 at 1860. */
    fun xpFor(level: Int): Int {
        val n = level.coerceIn(1, MAX_LEVEL) - 1
        return STARTING_XP + XP_STEP * n * (n + 1) / 2
    }

    fun levelFor(totalXp: Int): Int {
        if (totalXp <= STARTING_XP) return 1
        // solve XP_STEP * n(n+1)/2 = xp for n, then step down past any rounding
        val xp = (totalXp - STARTING_XP).toDouble()
        var n = ((sqrt(1 + 8 * xp / XP_STEP) - 1) / 2).toInt()
        while (n > 0 && xpFor(n + 1) > totalXp) n--
        while (n + 2 <= MAX_LEVEL && xpFor(n + 2) <= totalXp) n++
        return (n + 1).coerceAtMost(MAX_LEVEL)
    }

    /**
     * The most total power a technique rolled at [level] can have. Each channel still stops
     * at 255, so the extra budget past 255 only goes to mixed colors.
     */
    fun powerBudget(level: Int): Int =
        RgbColor.STARTING_POWER_BUDGET + BUDGET_PER_LEVEL * (level.coerceIn(1, MAX_LEVEL) - 1)

    /** How far through [level] toward the next one [totalXp] is, from 0 to 1. */
    fun progress(totalXp: Int): Double {
        val level = levelFor(totalXp)
        if (level >= MAX_LEVEL) return 1.0
        val start = xpFor(level)
        return (totalXp - start).toDouble() / (xpFor(level + 1) - start)
    }
}

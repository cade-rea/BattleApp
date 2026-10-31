package com.caderea.battleapp.core.color

import kotlin.math.roundToInt

/**
 * How a technique's color pushes its stats. Each primary pulls one way, and a mix blends
 * them by its share of each channel:
 *
 * - red is fast but weaker
 * - blue is strong but slow
 * - green is efficient: cheaper, with stronger heals and guards
 *
 * All the swings are starting values to playtest. Each rolled technique adds its own random
 * spread on top of these (see TechniqueRolls).
 */
data class ColorTraits(
    /** Multiplies the base wind-up: below 1 is faster. */
    val windUpFactor: Double,
    /** Multiplies attack damage per point of power. */
    val strength: Double,
    /** Multiplies energy cost: below 1 is cheaper. */
    val costFactor: Double,
    /** Multiplies healing and guarding. */
    val support: Double,
) {

    /** Ticks spent winding up before the technique lands, from 1 to [MAX_WIND_UP_TICKS]. */
    val windUpTicks: Int
        get() = windUpTicks(roll = 1.0)

    /** Wind-up ticks for a technique whose own roll stretches (above 1) or shortens its wind-up. */
    fun windUpTicks(roll: Double): Int =
        (BASE_WIND_UP_TICKS * windUpFactor * roll).roundToInt().coerceIn(1, MAX_WIND_UP_TICKS)

    companion object {
        const val BASE_WIND_UP_TICKS = 2
        /** The slowest anything winds up, so the timeline always has room to show it. */
        const val MAX_WIND_UP_TICKS = 3
        const val SPEED_SWING = 0.3
        const val STRENGTH_SWING = 0.2
        const val COST_SWING = 0.3
        const val SUPPORT_SWING = 0.3

        fun of(color: RgbColor): ColorTraits {
            val red = color.share(Channel.RED)
            val green = color.share(Channel.GREEN)
            val blue = color.share(Channel.BLUE)

            return ColorTraits(
                windUpFactor = 1 - SPEED_SWING * red + SPEED_SWING * blue,
                strength = 1 - STRENGTH_SWING * red + STRENGTH_SWING * blue,
                costFactor = 1 - COST_SWING * green,
                support = 1 + SUPPORT_SWING * green,
            )
        }
    }
}

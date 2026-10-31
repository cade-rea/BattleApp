package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.color.ColorTraits
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import kotlin.math.roundToInt

/**
 * Something a fighter can do on their turn. Its [color] is its type and power.
 */
abstract class BattleAction(val name: String, val color: RgbColor) {

    val traits = ColorTraits.of(color)

    /**
     * Ticks spent winding up before the action lands, shown to the opponent as a telegraph:
     * red 1, green 2, blue 3.
     */
    val duration: Int
        get() = traits.windUpTicks

    /**
     * Energy spent when the action is queued: more for brighter colors, less for green.
     */
    val cost: Int = (BASE_COST * color.power / RgbColor.STARTING_POWER_BUDGET * traits.costFactor)
        .roundToInt()
        .coerceAtLeast(1)

    /**
     * Applies the action.
     * @return a description of what happened, for the battle log
     */
    abstract fun performAction(performer: Fighter, target: Fighter, environment: Environment): String

    override fun toString() = "Action: $name, Color: $color, Duration: $duration, Cost: $cost"

    companion object {
        /** The cost of a full-power technique with no green in it. A starting value to playtest. */
        const val BASE_COST = 10.0
    }
}

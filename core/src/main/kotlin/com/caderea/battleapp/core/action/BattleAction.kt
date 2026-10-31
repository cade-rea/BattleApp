package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.color.ColorTraits
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter

/**
 * Something a fighter can do on their turn. Its [color] is its type and power.
 */
abstract class BattleAction(val name: String, val color: RgbColor) {

    val traits = ColorTraits.of(color)

    // wind-up in ticks; the battle doesn't wait on it yet, that comes with telegraphs
    val duration: Int
        get() = traits.windUpTicks

    /**
     * Applies the action.
     * @return a description of what happened, for the battle log
     */
    abstract fun performAction(performer: Fighter, target: Fighter, environment: Environment): String

    override fun toString() = "Action: $name, Color: $color, Duration: $duration"
}

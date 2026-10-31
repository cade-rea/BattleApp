package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.fighter.Fighter

/**
 * Something a fighter can do on their turn.
 */
abstract class BattleAction(val name: String, val duration: Int) {

    /**
     * Applies the action.
     * @return a description of what happened, for the battle log
     */
    abstract fun performAction(performer: Fighter, target: Fighter): String

    override fun toString() = "Action: $name, Duration: $duration"
}

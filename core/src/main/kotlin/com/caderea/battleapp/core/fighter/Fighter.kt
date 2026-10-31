package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.BattleQueue
import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.BattleAction
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment

/**
 * @param color the fighter's aura: their defensive type, and the color their techniques
 * get an affinity bonus for matching
 * @param actions one entry per action button; null means the slot is empty
 */
open class Fighter(
    val name: String = "Default Fighter",
    val color: RgbColor = RgbColor.RED,
    val maxHealth: Int = DEFAULT_MAX_HEALTH,
    val actions: List<BattleAction?> = defaultActions(),
) {
    // health stays between 0 and maxHealth, so healing can't overflow
    var health: Int = maxHealth
        set(value) {
            field = value.coerceIn(0, maxHealth)
        }

    lateinit var target: Fighter

    val queue = BattleQueue()

    /**
     * Performs the next queued action.
     * @return a description of what happened, or null if the queue was empty
     */
    fun performNextAction(environment: Environment): String? =
        queue.poll()?.action?.performAction(this, target, environment)

    companion object {
        const val DEFAULT_MAX_HEALTH = 1000

        // a red fighter's starting kit: its own color, plus green to cover blue opponents
        fun defaultActions(): List<BattleAction?> = listOf(
            Attack("Red Strike", RgbColor.RED),
            Heal("Green Mend", RgbColor.GREEN),
            Attack("Green Strike", RgbColor.GREEN),
            null,
        )
    }
}

package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.BattleQueue
import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.BattleAction
import com.caderea.battleapp.core.action.Heal

open class Fighter(
    val name: String = "Default Fighter",
    var power: Int = 1,
    val maxHealth: Int = 10,
) {
    // health stays between 0 and maxHealth, so healing can't overflow
    var health: Int = maxHealth
        set(value) {
            field = value.coerceIn(0, maxHealth)
        }

    lateinit var target: Fighter

    // one entry per action button; null means the slot is empty
    val actions: List<BattleAction?> = listOf(Attack(), Heal(), null, null)

    val queue = BattleQueue()

    /**
     * Performs the next queued action.
     * @return a description of what happened, or null if the queue was empty
     */
    fun performNextAction(): String? =
        queue.poll()?.action?.performAction(this, target)
}

package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.BattleQueue
import com.caderea.battleapp.core.action.ActiveGuard
import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.BattleAction
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor
import kotlin.math.roundToInt

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

    // energy is spent when an action is queued, so tapping faster can't buy more actions
    var energy: Int = STARTING_ENERGY
        set(value) {
            field = value.coerceIn(0, MAX_ENERGY)
        }

    /** The guard currently up, if any. */
    var activeGuard: ActiveGuard? = null

    lateinit var target: Fighter

    val queue = BattleQueue()

    /** Whether the action in [slot] exists, fits in the queue and is affordable right now. */
    fun canQueue(slot: Int): Boolean {
        val action = actions.getOrNull(slot) ?: return false
        return !queue.isFull() && energy >= action.cost
    }

    /**
     * Queues the action in the given button slot and pays its energy cost.
     * @return false if the slot is empty or out of range, the queue is full, or there isn't
     * enough energy
     */
    fun queueAction(slot: Int): Boolean {
        if (!canQueue(slot)) return false

        val action = actions[slot]!!
        queue.offer(action)
        energy -= action.cost
        return true
    }

    /**
     * Takes back the action queued last, refunding part of its energy, even if it has
     * started winding up.
     * @return false if the queue was empty
     */
    fun cancelLast(): Boolean {
        val canceled = queue.removeLast() ?: return false
        energy += (canceled.action.cost * CANCEL_REFUND).roundToInt()
        return true
    }

    /**
     * Spends one tick winding up the action at the front of the queue.
     * @return the action if its wind-up finished and it lands this tick, otherwise null
     */
    fun windUp(): BattleAction? {
        val front = queue.peek() ?: return null
        if (front.ticksLeft > 1) {
            queue.replaceFront(front.copy(ticksLeft = front.ticksLeft - 1))
            return null
        }

        queue.poll()
        return front.action
    }

    /** End-of-tick upkeep: regain energy and count down the guard. */
    fun endTick() {
        energy += ENERGY_PER_TICK
        activeGuard = activeGuard?.let { guard ->
            guard.copy(ticksLeft = guard.ticksLeft - 1).takeIf { it.ticksLeft > 0 }
        }
    }

    companion object {
        const val DEFAULT_MAX_HEALTH = 2000

        // starting values to playtest: a full-power technique costs 10 (green 7), so a
        // fighter can afford about one action every two ticks
        const val MAX_ENERGY = 30
        const val STARTING_ENERGY = 20
        const val ENERGY_PER_TICK = 5
        /** The share of its cost a canceled action gives back. */
        const val CANCEL_REFUND = 0.5

        // a red fighter's starting kit: its own color, green to cover blue opponents, and a
        // blue guard against its predator
        fun defaultActions(): List<BattleAction?> = listOf(
            Attack("Red Strike", RgbColor.RED),
            Heal("Green Mend", RgbColor.GREEN),
            Attack("Green Strike", RgbColor.GREEN),
            Guard("Blue Guard", RgbColor.BLUE),
        )
    }
}

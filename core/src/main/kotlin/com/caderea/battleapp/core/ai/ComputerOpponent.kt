package com.caderea.battleapp.core.ai

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter

/**
 * Plays a fighter by reading the opponent's telegraphs: when an attack is winding up, it
 * raises the guard that best matches its color, if that guard can land in time. Otherwise it
 * heals when hurt and attacks with whatever hits hardest. It keeps its own queue short so it
 * stays free to react, and it follows the same queue and energy rules as a player.
 *
 * It never guards twice in a row, so it can't turtle forever: after a guard it commits to an
 * attack, and that wind-up is the player's opening.
 *
 * Deterministic, so battles against it can be tested tick by tick. One instance plays one
 * fighter in one battle.
 */
class ComputerOpponent {

    private var guardedLast = false

    /**
     * Called right before a tick, so the opponent sees everything queued since the last
     * tick, just as a player reacting at the last moment would. It only guards here; new
     * attacks wait for [plan], so the other side sees them with their whole wind-up.
     */
    fun react(me: Fighter, foe: Fighter) {
        guardAgainstIncoming(me, foe)
    }

    /**
     * Called right after a tick: guard against anything incoming, otherwise heal when hurt,
     * otherwise attack.
     */
    fun plan(me: Fighter, foe: Fighter, environment: Environment) {
        if (guardAgainstIncoming(me, foe)) return
        // anything already queued would delay a guard, so only commit to one action at a time
        if (!me.queue.isEmpty()) return

        if (me.health < me.maxHealth * HEAL_BELOW && queueBest<Heal>(me) { 0.0 }) return

        if (queueBest<Attack>(me) { attack -> expectedDamage(attack, me, foe, environment) }) {
            guardedLast = false
        }
    }

    /**
     * Queues a guard against the first attack in [foe]'s queue that isn't covered yet and
     * that a guard can still reach in time. Attacks too close to landing are let through.
     * @return whether a guard was queued
     */
    private fun guardAgainstIncoming(me: Fighter, foe: Fighter): Boolean {
        // a guard already on its way covers whatever comes next
        if (guardedLast || me.queue.any { it.action is Guard }) return false

        // guards resolve before attacks on the same tick, so landing together is in time
        val startsIn = me.queue.ticksUntilFree

        for ((index, queued) in foe.queue.withIndex()) {
            val attack = queued.action as? Attack ?: continue
            val landsIn = foe.queue.ticksUntilLands(index)
            if (isCovered(me, attack.color, landsIn)) continue

            val guarded = queueBest<Guard>(
                me,
                fits = { guard ->
                    startsIn + guard.duration <= landsIn && guard.color.overlap(attack.color) >= MIN_OVERLAP
                },
            ) { guard -> guard.color.overlap(attack.color) }
            if (guarded) {
                guardedLast = true
                return true
            }
        }
        return false
    }

    /** Whether the guard [me] has up will still be up, in a matching color, in [landsIn] ticks. */
    private fun isCovered(me: Fighter, color: RgbColor, landsIn: Int): Boolean {
        val guard = me.activeGuard ?: return false
        return guard.ticksLeft >= landsIn && guard.guard.color.overlap(color) >= MIN_OVERLAP
    }

    /** What [attack] would deal after any guard [foe] will still have up when it lands. */
    private fun expectedDamage(attack: Attack, me: Fighter, foe: Fighter, environment: Environment): Double {
        val damage = attack.damage(me, foe, environment)
        val landsIn = me.queue.ticksUntilFree + attack.duration
        val blocked = foe.activeGuard?.takeIf { it.ticksLeft >= landsIn }?.blocked(damage, attack.color) ?: 0
        return (damage - blocked).toDouble()
    }

    /**
     * Queues the affordable action of type [T] with the highest [score] among those that
     * [fits].
     * @return whether anything was queued
     */
    private inline fun <reified T> queueBest(
        me: Fighter,
        fits: (T) -> Boolean = { true },
        score: (T) -> Double,
    ): Boolean {
        val slot = me.actions.indices
            .filter { slot -> me.canQueue(slot) && (me.actions[slot] as? T)?.let(fits) == true }
            .maxByOrNull { slot -> score(me.actions[slot] as T) }
            ?: return false

        return me.queueAction(slot)
    }

    companion object {
        /** Heal when health drops below this share. A starting value to playtest. */
        const val HEAL_BELOW = 0.4
        /** Only guard with a color that shares at least this much of the attack's mix. */
        const val MIN_OVERLAP = 0.5
    }
}

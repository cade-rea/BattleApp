package com.caderea.battleapp.core

import com.caderea.battleapp.core.action.BattleAction
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.ai.ComputerOpponent
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.EnemyFighter
import com.caderea.battleapp.core.fighter.Fighter

/**
 * One fight between the player and the enemy.
 *
 * The battle has no clock or thread of its own: whoever owns it (the app's ViewModel, or a
 * server later on) calls [tick] once per game tick, always from the same thread.
 *
 * @param opponent plays the enemy; null when someone else queues the enemy's actions, as a
 * second player would
 */
class Battle(
    val player: Fighter = Fighter(),
    val enemy: Fighter = EnemyFighter(),
    val environment: Environment = Environment(),
    private val opponent: ComputerOpponent? = ComputerOpponent(),
) {
    var tickCount = 0
        private set

    var isOver = false
        private set

    /** The actions that landed on the last tick, in the order they landed. */
    var landed: List<Landing> = emptyList()
        private set

    /** Every action that has landed so far, oldest first. Progression counts XP from it. */
    val history: List<Landing>
        get() = _history

    private val _history = mutableListOf<Landing>()

    /** The fighter left standing, or null while the battle is on or if it was stopped. */
    val winner: Fighter?
        get() = when {
            !isOver -> null
            enemy.health <= 0 -> player
            player.health <= 0 -> enemy
            else -> null
        }

    private val fighters = listOf(player, enemy)

    /**
     * The environment as it stands for the next tick to resolve, with the light fixed at
     * that tick's. What the screen previews and the opponent plans with.
     */
    val conditions: Environment
        get() = environment.at(tickCount + 1)

    init {
        player.target = enemy
        enemy.target = player
        // the opponent opens before the first tick, so its first move is telegraphed in full
        opponent?.plan(enemy, player, environment, tickCount + 1)
    }

    /**
     * Advances the battle by one tick: every front action winds up by one tick, and the ones
     * that finish land. Guards land before anything else, so a guard as fast as the attack
     * it answers is in time.
     * @return what happened during the tick, in order, for the battle log
     */
    fun tick(): List<String> {
        if (isOver) return emptyList()

        opponent?.react(enemy, player)

        ++tickCount
        // actions see the light of the tick they land on
        val now = environment.at(tickCount)
        val log = mutableListOf<String>()
        val landedNow = mutableListOf<Landing>()
        landed = landedNow

        val landing: List<Pair<Fighter, BattleAction>> = fighters
            .mapNotNull { fighter -> fighter.windUp()?.let { fighter to it } }
            // a stable sort, so the player still goes first among equals
            .sortedBy { (_, action) -> action !is Guard }

        for ((fighter, action) in landing) {
            log += action.performAction(fighter, fighter.target, now)
            landedNow += Landing(fighter, action)
            _history += Landing(fighter, action)

            // check right after each action so a fighter that just died doesn't get to act
            val target = fighter.target
            if (target.health <= 0) {
                log += "${target.name} has died."
                isOver = true
                return log
            }
        }

        fighters.forEach(Fighter::endTick)
        opponent?.plan(enemy, player, environment, tickCount + 1)
        return log
    }

    /**
     * Queues the player's action in the given button slot.
     * @return false if the slot is empty or out of range, the queue is full, or the player
     * can't afford it, or the battle is over
     */
    fun queueAction(slot: Int): Boolean = !isOver && player.queueAction(slot)

    /**
     * Takes back the player's last queued action, refunding part of its energy.
     * @return false if the queue was empty or the battle is over
     */
    fun cancelLast(): Boolean = !isOver && player.cancelLast()

    /**
     * Queues the action in [slot] for either fighter, as the PvP server does for both
     * players.
     * @return false if [fighter] isn't in this battle, or for the same reasons as [queueAction]
     */
    fun queueAction(fighter: Fighter, slot: Int): Boolean =
        !isOver && fighter in fighters && fighter.queueAction(slot)

    /**
     * Takes back the last queued action of either fighter.
     * @return false if [fighter] isn't in this battle, or for the same reasons as [cancelLast]
     */
    fun cancelLast(fighter: Fighter): Boolean =
        !isOver && fighter in fighters && fighter.cancelLast()

    /** The actions [fighter] has landed so far, oldest first. */
    fun used(fighter: Fighter): List<BattleAction> =
        _history.filter { it.performer === fighter }.map { it.action }

    fun stop() {
        isOver = true
    }
}

/** An action that landed: [performer] did [action], at their target or to themselves. */
data class Landing(val performer: Fighter, val action: BattleAction)

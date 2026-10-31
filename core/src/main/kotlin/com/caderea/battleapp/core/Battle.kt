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

    private val fighters = listOf(player, enemy)

    init {
        player.target = enemy
        enemy.target = player
        // the opponent opens before the first tick, so its first move is telegraphed in full
        opponent?.plan(enemy, player, environment)
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
        val log = mutableListOf(
            "Tick:$tickCount ${player.name}:${player.health} :: ${enemy.name}:${enemy.health}"
        )

        val landing: List<Pair<Fighter, BattleAction>> = fighters
            .mapNotNull { fighter -> fighter.windUp()?.let { fighter to it } }
            // a stable sort, so the player still goes first among equals
            .sortedBy { (_, action) -> action !is Guard }

        for ((fighter, action) in landing) {
            log += action.performAction(fighter, fighter.target, environment)

            // check right after each action so a fighter that just died doesn't get to act
            val target = fighter.target
            if (target.health <= 0) {
                log += "${target.name} has died."
                isOver = true
                return log
            }
        }

        fighters.forEach(Fighter::endTick)
        opponent?.plan(enemy, player, environment)
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

    fun stop() {
        isOver = true
    }
}

package com.caderea.battleapp.core

import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.EnemyFighter
import com.caderea.battleapp.core.fighter.Fighter

/**
 * One fight between the player and the enemy.
 *
 * The battle has no clock or thread of its own: whoever owns it (the app's ViewModel, or a
 * server later on) calls [tick] once per game tick, always from the same thread.
 */
class Battle(
    val player: Fighter = Fighter(),
    val enemy: EnemyFighter = EnemyFighter(),
    val environment: Environment = Environment(),
) {
    var tickCount = 0
        private set

    var isOver = false
        private set

    init {
        player.target = enemy
        enemy.target = player
    }

    /**
     * Advances the battle by one tick.
     * @return what happened during the tick, in order, for the battle log
     */
    fun tick(): List<String> {
        if (isOver) return emptyList()

        ++tickCount
        val log = mutableListOf(
            "Tick:$tickCount ${player.name}:${player.health} :: ${enemy.name}:${enemy.health}"
        )

        for (fighter in listOf(player, enemy)) {
            fighter.performNextAction(environment)?.let(log::add)

            // check right after each action so a fighter that just died doesn't get to act
            val target = fighter.target
            if (target.health <= 0) {
                log += "${target.name} has died."
                isOver = true
                return log
            }
        }

        botLogic()
        return log
    }

    private fun botLogic() {
        if (tickCount % 2 == 0) {
            enemy.addAttackToQueue()
        }
    }

    /**
     * Queues the player's action in the given button slot.
     * @return false if the slot is empty or out of range, or the queue is full
     */
    fun queueAction(slot: Int): Boolean {
        val action = player.actions.getOrNull(slot) ?: return false
        return player.queue.offer(action)
    }

    fun stop() {
        isOver = true
    }
}

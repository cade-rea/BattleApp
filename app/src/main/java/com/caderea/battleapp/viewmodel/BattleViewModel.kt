package com.caderea.battleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caderea.battleapp.core.Battle
import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.BattleAction
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.TypeWheel
import com.caderea.battleapp.core.fighter.Fighter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Owns the battle and its tick loop. The loop runs in [viewModelScope], so it survives
 * screen rotation and is cancelled when the player leaves the battle screen.
 *
 * Everything here runs on the main thread, so the battle is only ever touched from one thread.
 *
 * @param newBattle makes the battle, again for each rematch
 */
class BattleViewModel(
    private val tickLengthMs: Long,
    private val newBattle: () -> Battle = { Battle() },
) : ViewModel() {

    private var battle = newBattle()
    private val log = ArrayDeque<LogEntry>(MAX_LOG_LINES)
    private val paused = MutableStateFlow(false)
    // health changes on the last tick
    private var playerHealthChange = 0
    private var enemyHealthChange = 0

    private val _uiState = MutableStateFlow(buildState(tickProgress = 0))
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    private var ticker: Job = startTicker()

    private fun startTicker() = viewModelScope.launch {
        val subtickLength = tickLengthMs / SUBTICKS_PER_TICK

        while (!battle.isOver) {
            for (subtick in 0 until SUBTICKS_PER_TICK) {
                // waits here while paused, so a paused tick picks up where it stopped
                paused.first { !it }
                publish(tickProgress = subtick * 100 / SUBTICKS_PER_TICK)
                delay(subtickLength)
            }

            val playerHealth = battle.player.health
            val enemyHealth = battle.enemy.health
            battle.tick().forEach { addLogLine(it) }
            playerHealthChange = battle.player.health - playerHealth
            enemyHealthChange = battle.enemy.health - enemyHealth
            publish(tickProgress = 0)
        }

        finish()
    }

    fun queueAction(slot: Int) {
        if (battle.queueAction(slot)) {
            publish()
        }
    }

    fun cancelLast() {
        if (battle.cancelLast()) {
            publish()
        }
    }

    /** Pauses the tick loop, or resumes it. Queuing and canceling still work while paused. */
    fun togglePause() {
        if (battle.isOver) return

        paused.value = !paused.value
        publish()
    }

    fun stop() {
        if (battle.isOver) return

        battle.stop()
        ticker.cancel()
        finish()
    }

    /** Starts a fresh battle against the same opponent. */
    fun rematch() {
        ticker.cancel()
        battle = newBattle()
        log.clear()
        playerHealthChange = 0
        enemyHealthChange = 0
        paused.value = false
        publish(tickProgress = 0)
        ticker = startTicker()
    }

    private fun finish() {
        addLogLine("Battle Finished")
        paused.value = false
        publish(tickProgress = 0)
    }

    private fun addLogLine(line: String) {
        if (log.size >= MAX_LOG_LINES) {
            log.removeFirst()
        }
        val side = when {
            line.startsWith(battle.player.name) -> Side.PLAYER
            line.startsWith(battle.enemy.name) -> Side.ENEMY
            else -> null
        }
        log.addLast(LogEntry(battle.tickCount, line, side))
    }

    private fun publish(tickProgress: Int = _uiState.value.tickProgress) {
        _uiState.value = buildState(tickProgress)
    }

    private fun buildState(tickProgress: Int) = BattleUiState(
        actions = battle.player.actions.mapIndexed { slot, action ->
            action?.toButton(enabled = !battle.isOver && battle.player.canQueue(slot)) ?: ActionButtonState()
        },
        player = battle.player.toState(playerHealthChange),
        enemy = battle.enemy.toState(enemyHealthChange),
        playerQueue = battle.player.queue.toList(),
        enemyQueue = battle.enemy.queue.toList(),
        tick = battle.tickCount,
        tickProgress = tickProgress,
        isPaused = paused.value,
        log = log.toList(),
        landed = battle.landed.map { landing ->
            val side = if (landing.performer === battle.player) Side.PLAYER else Side.ENEMY
            LandedAction(side, landing.action.kind, landing.action.color)
        },
        outcome = when {
            !battle.isOver -> null
            battle.enemy.health <= 0 -> Outcome.WON
            battle.player.health <= 0 -> Outcome.LOST
            else -> Outcome.STOPPED
        },
    )

    private fun BattleAction.toButton(enabled: Boolean): ActionButtonState {
        val player = battle.player
        val environment = battle.environment
        val button = ActionButtonState(
            name = name,
            cost = cost,
            enabled = enabled,
            kind = kind,
            color = color,
            windUpTicks = duration,
        )
        return when (this) {
            is Attack -> button.copy(
                effect = damage(player, player.target, environment),
                effectiveness = TypeWheel.effectiveness(color, player.target.color),
            )
            is Heal -> button.copy(effect = amount(player, environment))
            is Guard -> button.copy(
                effect = (strength(player, environment).coerceAtMost(Guard.MAX_BLOCK) * 100).roundToInt(),
            )
            else -> button
        }
    }

    private fun Fighter.toState(healthChange: Int) = FighterState(
        name = name,
        color = color,
        health = health,
        maxHealth = maxHealth,
        energy = energy,
        maxEnergy = Fighter.MAX_ENERGY,
        guard = activeGuard?.let { GuardState(it.guard.name, it.guard.color, it.ticksLeft) },
        healthChange = healthChange,
    )

    companion object {
        const val SUBTICKS_PER_TICK = 4
        const val MAX_LOG_LINES = 40

        fun factory(tickLengthMs: Long) = viewModelFactory {
            initializer { BattleViewModel(tickLengthMs) }
        }
    }
}

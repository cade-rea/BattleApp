package com.caderea.battleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caderea.battleapp.core.Battle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the battle and its tick loop. The loop runs in [viewModelScope], so it survives
 * screen rotation and is cancelled when the player leaves the battle screen.
 *
 * Everything here runs on the main thread, so the battle is only ever touched from one thread.
 */
class BattleViewModel(
    private val tickLengthMs: Long,
    private val battle: Battle = Battle(),
) : ViewModel() {

    private val log = ArrayDeque<String>(MAX_LOG_LINES)

    private val _uiState = MutableStateFlow(buildState(tickProgress = 0))
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    private val ticker = viewModelScope.launch {
        val subtickLength = tickLengthMs / SUBTICKS_PER_TICK

        while (!battle.isOver) {
            for (subtick in 0 until SUBTICKS_PER_TICK) {
                publish(tickProgress = subtick * 100 / SUBTICKS_PER_TICK)
                delay(subtickLength)
            }

            battle.tick().forEach(::addLogLine)
            publish(tickProgress = 0)
        }

        finish()
    }

    fun queueAction(slot: Int) {
        if (battle.queueAction(slot)) {
            publish()
        }
    }

    fun stop() {
        if (battle.isOver) return

        battle.stop()
        ticker.cancel()
        finish()
    }

    private fun finish() {
        addLogLine("Battle Finished")
        publish(tickProgress = 0)
    }

    private fun addLogLine(line: String) {
        if (log.size >= MAX_LOG_LINES) {
            log.removeFirst()
        }
        log.addLast(line)
    }

    private fun publish(tickProgress: Int = _uiState.value.tickProgress) {
        _uiState.value = buildState(tickProgress)
    }

    private fun buildState(tickProgress: Int) = BattleUiState(
        actionNames = battle.player.actions.map { it?.name.orEmpty() },
        playerQueue = battle.player.queue.toList(),
        enemyQueue = battle.enemy.queue.toList(),
        tickProgress = tickProgress,
        log = log.toList(),
        isOver = battle.isOver,
    )

    companion object {
        private const val SUBTICKS_PER_TICK = 4
        private const val MAX_LOG_LINES = 10

        fun factory(tickLengthMs: Long) = viewModelFactory {
            initializer { BattleViewModel(tickLengthMs) }
        }
    }
}

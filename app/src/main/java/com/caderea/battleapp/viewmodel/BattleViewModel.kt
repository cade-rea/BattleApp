package com.caderea.battleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caderea.battleapp.core.Battle
import com.caderea.battleapp.core.environment.Arena
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.fighter.Opponents
import com.caderea.battleapp.core.progression.BattleRewards
import com.caderea.battleapp.core.progression.Progression
import com.caderea.battleapp.core.technique.TechniqueRoller
import com.caderea.battleapp.data.ProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Owns the battle and its tick loop. The loop runs in [viewModelScope], so it survives
 * screen rotation and is cancelled when the player leaves the battle screen.
 *
 * Everything here runs on the main thread, so the battle is only ever touched from one thread.
 *
 * @param newBattle makes the battle, again for each rematch
 * @param onBattleOver called once when a battle ends, by a win, a loss or giving up, to hand
 * out XP and techniques; null means nothing is tracked
 */
class BattleViewModel(
    private val tickLengthMs: Long,
    private val newBattle: () -> Battle = { Battle() },
    private val onBattleOver: (Battle) -> BattleRewards? = { null },
) : ViewModel() {

    private var battle = newBattle()
    private val log = ArrayDeque<LogEntry>(MAX_LOG_LINES)
    private val paused = MutableStateFlow(false)
    // health changes on the last tick
    private var playerHealthChange = 0
    private var enemyHealthChange = 0
    private var rewards: RewardsState? = null
    private var rewarded = false

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
        rewards = null
        rewarded = false
        playerHealthChange = 0
        enemyHealthChange = 0
        paused.value = false
        publish(tickProgress = 0)
        ticker = startTicker()
    }

    private fun finish() {
        if (!rewarded) {
            rewarded = true
            rewards = onBattleOver(battle)?.toState()
        }
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
        actions = battle.player.toButtons(battle.conditions, battle.isOver),
        player = battle.player.toState(playerHealthChange),
        enemy = battle.enemy.toState(enemyHealthChange),
        playerQueue = battle.player.queue.toList(),
        enemyQueue = battle.enemy.queue.toList(),
        tick = battle.tickCount,
        tickProgress = tickProgress,
        isPaused = paused.value,
        log = log.toList(),
        landed = battle.landed.toLanded(battle.player),
        outcome = when {
            !battle.isOver -> null
            battle.enemy.health <= 0 -> Outcome.WON
            battle.player.health <= 0 -> Outcome.LOST
            else -> Outcome.STOPPED
        },
        rewards = rewards,
        arena = (battle.environment as? Arena)?.toHud(battle.tickCount + 1),
    )

    companion object {
        const val SUBTICKS_PER_TICK = 4
        const val MAX_LOG_LINES = 40

        /**
         * A battle in [arena] for the saved fighter against a computer opponent of their
         * level (or the arena's cap), with XP and new techniques saved when it ends.
         */
        fun factory(
            tickLengthMs: Long,
            profiles: ProfileRepository,
            arena: Arena = Arenas.DEFAULT,
            random: Random = Random.Default,
        ) = viewModelFactory {
            initializer {
                BattleViewModel(
                    tickLengthMs,
                    newBattle = {
                        val profile = profiles.requireActive()
                        Battle(
                            player = profile.toFighter(arena),
                            enemy = Opponents.forArena(profile.level, arena, random).toFighter(arena),
                            environment = arena,
                        )
                    },
                    onBattleOver = { battle ->
                        var rewards: BattleRewards? = null
                        profiles.update { profile ->
                            Progression.afterBattle(profile, battle, TechniqueRoller(random))
                                .also { rewards = it.second }
                                .first
                        }
                        rewards
                    },
                )
            }
        }
    }
}

package com.caderea.battleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caderea.battleapp.core.environment.Arena
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.ClientMessage
import com.caderea.battleapp.core.online.EndReason
import com.caderea.battleapp.core.online.MatchMirror
import com.caderea.battleapp.core.online.MatchResult
import com.caderea.battleapp.core.online.ServerMessage
import com.caderea.battleapp.online.OnlineConnection
import com.caderea.battleapp.online.OnlineConnector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Where an online match is at. */
enum class OnlinePhase { CONNECTING, WAITING, PLAYING, OVER, FAILED }

data class OnlineUiState(
    val phase: OnlinePhase = OnlinePhase.CONNECTING,
    val arenaName: String = "",
    val server: String = "",
    // the battle, once a match has started
    val battle: BattleUiState = BattleUiState(isOnline = true),
    val tickLengthMs: Long = DEFAULT_TICK_MS,
    // when phase is FAILED: what the server said no to, or null if the connection failed
    val problem: String? = null,
) {
    companion object {
        const val DEFAULT_TICK_MS = 1000L
    }
}

/**
 * An online match against another player. The server runs the battle: this connects,
 * joins matchmaking with the loaded character, passes taps on as inputs, and shows each
 * state the server sends through a [MatchMirror], with the same screen as an offline battle.
 *
 * Online matches award no XP yet, so nothing is saved.
 */
class OnlineBattleViewModel(
    private val connector: OnlineConnector,
    private val url: String,
    private val profile: FighterProfile,
    private val arena: Arena,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnlineUiState(arenaName = arena.name, server = url))
    val uiState: StateFlow<OnlineUiState> = _uiState.asStateFlow()

    private var connection: OnlineConnection? = null
    private var session: Job? = null
    private var mirror: MatchMirror? = null
    private var progress: Job? = null
    private var playerHealthChange = 0
    private var enemyHealthChange = 0
    private var outcome: Outcome? = null
    private var endNote: EndNote? = null

    init {
        start()
    }

    /** Queues the technique in [slot], if the fighter can afford it. The server has the last word. */
    fun queueAction(slot: Int) {
        val mirror = mirror ?: return
        if (outcome == null && mirror.you.canQueue(slot)) connection?.send(ClientMessage.Queue(slot))
    }

    fun cancelLast() {
        val mirror = mirror ?: return
        if (outcome == null && !mirror.you.queue.isEmpty()) connection?.send(ClientMessage.Cancel)
    }

    /** Gives up the match. To stop looking for one, just leave the screen: that hangs up. */
    fun giveUp() {
        if (mirror != null && outcome == null) connection?.send(ClientMessage.Leave)
    }

    /** Looks for a new match in the same arena, as a rematch or after a failure. */
    fun again() {
        start()
    }

    private fun start() {
        session?.cancel()
        progress?.cancel()
        progress = null
        connection?.close()
        connection = null
        mirror = null
        outcome = null
        endNote = null
        playerHealthChange = 0
        enemyHealthChange = 0
        _uiState.value = OnlineUiState(arenaName = arena.name, server = url)

        session = viewModelScope.launch {
            val connection = runCatching { connector.connect(url) }.getOrElse {
                fail(it.message ?: it.toString())
                return@launch
            }
            this@OnlineBattleViewModel.connection = connection
            connection.send(ClientMessage.Join(profile, arena.id))
            try {
                connection.messages.collect(::handle)
                // the server hung up without saying how the match ended
                if (outcome == null) fail(null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (outcome == null) fail(e.message)
            }
        }
    }

    private fun handle(message: ServerMessage) {
        when (message) {
            is ServerMessage.Waiting -> update { it.copy(phase = OnlinePhase.WAITING) }
            is ServerMessage.MatchStarted -> {
                mirror = MatchMirror(message)
                update { it.copy(phase = OnlinePhase.PLAYING, tickLengthMs = message.tickLengthMs) }
            }
            is ServerMessage.State -> onState(message)
            is ServerMessage.MatchOver -> onOver(message)
            is ServerMessage.Rejected ->
                // during a match it's an input that lost a race, such as a queue that filled up
                if (mirror == null) fail(message.reason)
        }
    }

    private fun onState(state: ServerMessage.State) {
        val mirror = mirror ?: return
        val newTick = state.tick != mirror.tick || mirror.tick == 0 && progress == null
        val playerHealth = mirror.you.health
        val enemyHealth = mirror.opponent.health
        mirror.apply(state)

        if (newTick) {
            playerHealthChange = mirror.you.health - playerHealth
            enemyHealthChange = mirror.opponent.health - enemyHealth
            restartProgress()
        } else {
            publish()
        }
    }

    private fun onOver(over: ServerMessage.MatchOver) {
        progress?.cancel()
        outcome = when (over.result) {
            MatchResult.WON -> Outcome.WON
            MatchResult.LOST -> Outcome.LOST
            MatchResult.DRAW -> Outcome.DRAW
        }
        val won = over.result == MatchResult.WON
        endNote = when (over.reason) {
            EndReason.KNOCKOUT -> null
            EndReason.FORFEIT -> if (won) EndNote.OPPONENT_GAVE_UP else EndNote.YOU_GAVE_UP
            EndReason.DISCONNECT -> if (won) EndNote.OPPONENT_DROPPED else EndNote.YOU_DROPPED
            EndReason.TIME -> EndNote.TIME
        }
        publish(tickProgress = 0, phase = OnlinePhase.OVER)
        connection?.close()
    }

    // the server only says when a tick ends, so the bar across the top counts the tick down
    // here, in the same steps as an offline battle
    private fun restartProgress() {
        progress?.cancel()
        progress = viewModelScope.launch {
            val step = _uiState.value.tickLengthMs / BattleViewModel.SUBTICKS_PER_TICK
            for (subtick in 0 until BattleViewModel.SUBTICKS_PER_TICK) {
                publish(tickProgress = subtick * 100 / BattleViewModel.SUBTICKS_PER_TICK)
                delay(step)
            }
        }
    }

    /** [problem] is what went wrong, or null if the connection just dropped. */
    private fun fail(problem: String?) {
        progress?.cancel()
        update { it.copy(phase = OnlinePhase.FAILED, problem = problem) }
    }

    private fun publish(
        tickProgress: Int = _uiState.value.battle.tickProgress,
        phase: OnlinePhase = _uiState.value.phase,
    ) {
        val mirror = mirror ?: return
        val isOver = outcome != null
        val battle = BattleUiState(
            actions = mirror.you.toButtons(mirror.conditions, isOver),
            player = mirror.you.toState(playerHealthChange),
            enemy = mirror.opponent.toState(enemyHealthChange),
            playerQueue = mirror.you.queue.toList(),
            enemyQueue = mirror.opponent.queue.toList(),
            tick = mirror.tick,
            tickProgress = tickProgress,
            landed = mirror.landed.toLanded(mirror.you),
            outcome = outcome,
            arena = mirror.arena.toHud(mirror.tick + 1),
            isOnline = true,
            endNote = endNote,
        )
        update { it.copy(phase = phase, battle = battle) }
    }

    private inline fun update(change: (OnlineUiState) -> OnlineUiState) {
        _uiState.value = change(_uiState.value)
    }

    override fun onCleared() {
        connection?.close()
    }

    companion object {
        fun factory(connector: OnlineConnector, url: String, profile: FighterProfile, arena: Arena) =
            viewModelFactory {
                initializer { OnlineBattleViewModel(connector, url, profile, arena) }
            }
    }
}

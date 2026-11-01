package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.ClientMessage
import com.caderea.battleapp.core.online.EndReason
import com.caderea.battleapp.core.online.FighterInfo
import com.caderea.battleapp.core.online.FighterState as WireFighter
import com.caderea.battleapp.core.online.GuardUp
import com.caderea.battleapp.core.online.LandedAction as WireLanded
import com.caderea.battleapp.core.online.MatchResult
import com.caderea.battleapp.core.online.QueuedTechnique
import com.caderea.battleapp.core.online.ServerMessage
import com.caderea.battleapp.core.online.Side as WireSide
import com.caderea.battleapp.online.FakeConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** Plays online matches against a pretend server, on virtual time. */
@OptIn(ExperimentalCoroutinesApi::class)
class OnlineBattleViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val connector = FakeConnector()

    // red's starter kit: Red Strike, Green Mend, Green Strike, Blue Guard
    private val red = FighterProfile.create("Red", Channel.RED)
    private val blue = FighterProfile.create("Blue", Channel.BLUE)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = OnlineBattleViewModel(connector, URL, red, Arenas.OPEN)

    private fun info(profile: FighterProfile) =
        FighterInfo(profile.name, profile.aura.color, profile.level, 2000, profile.loadoutFor(4))

    private val started = ServerMessage.MatchStarted("m1", "open", TICK_MS, info(red), info(blue))

    private fun state(
        tick: Int,
        you: WireFighter = WireFighter(2000, 20, emptyList()),
        opponent: WireFighter = WireFighter(2000, 20, emptyList()),
        landed: List<WireLanded> = emptyList(),
    ) = ServerMessage.State(tick, you, opponent, landed)

    @Test
    fun joinsWithTheCharacterAndArena() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()

        assertEquals(listOf(URL), connector.urls)
        assertEquals(listOf(ClientMessage.Join(red, "open")), connector.last.sent)
        assertEquals(OnlinePhase.CONNECTING, viewModel.uiState.value.phase)

        connector.last.receive(ServerMessage.Waiting("open"))
        runCurrent()
        assertEquals(OnlinePhase.WAITING, viewModel.uiState.value.phase)
        assertEquals("Open Field", viewModel.uiState.value.arenaName)
    }

    @Test
    fun theServersStateDrivesTheBattleScreen() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.receive(started)
        connector.last.receive(state(0))
        runCurrent()

        val opening = viewModel.uiState.value
        assertEquals(OnlinePhase.PLAYING, opening.phase)
        assertEquals(TICK_MS, opening.tickLengthMs)
        assertEquals(listOf("Red Strike", "Green Mend", "Green Strike", "Blue Guard"), opening.battle.actions.map { it.name })
        assertTrue(opening.battle.isOnline)
        assertEquals("Blue", opening.battle.enemy.name)

        // the opponent queues a strike and the server says so
        connector.last.receive(state(0, opponent = WireFighter(2000, 10, listOf(QueuedTechnique(0, 0, 1)))))
        runCurrent()
        assertEquals(1, viewModel.uiState.value.battle.enemyQueue.size)

        // then it lands on tick 1
        connector.last.receive(
            state(
                1,
                you = WireFighter(1700, 25, emptyList(), GuardUp(3, 0.5, 2)),
                opponent = WireFighter(2000, 15, emptyList()),
                landed = listOf(WireLanded(WireSide.OPPONENT, 0)),
            )
        )
        runCurrent()
        val hit = viewModel.uiState.value.battle
        assertEquals(1, hit.tick)
        assertEquals(1700, hit.player.health)
        assertEquals(-300, hit.player.healthChange)
        assertEquals("Blue Guard", hit.player.guard?.name)
        assertEquals(listOf(Side.ENEMY), hit.landed.map { it.side })
        assertEquals(0, hit.tickProgress)
    }

    @Test
    fun theTickBarCountsDownBetweenStates() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.receive(started)
        connector.last.receive(state(0))
        runCurrent()

        advanceTimeBy(TICK_MS / 2 + 1)
        assertEquals(50, viewModel.uiState.value.battle.tickProgress)
        // the bar waits at the last step for the server's next tick
        advanceTimeBy(TICK_MS * 3)
        assertEquals(75, viewModel.uiState.value.battle.tickProgress)
    }

    @Test
    fun tapsBecomeInputsOnlyWhenTheyCanBeQueued() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.receive(started)
        connector.last.receive(state(0, you = WireFighter(2000, 5, emptyList())))
        runCurrent()

        // 5 energy can't pay for anything
        viewModel.queueAction(0)
        viewModel.cancelLast()
        assertEquals(1, connector.last.sent.size)

        connector.last.receive(state(1, you = WireFighter(2000, 20, listOf(QueuedTechnique(2, 0, 2)))))
        runCurrent()
        viewModel.queueAction(0)
        viewModel.cancelLast()
        viewModel.giveUp()
        assertEquals(
            listOf(ClientMessage.Queue(0), ClientMessage.Cancel, ClientMessage.Leave),
            connector.last.sent.drop(1),
        )
    }

    @Test
    fun theMatchOverShowsTheResultAndWhy() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.receive(started)
        connector.last.receive(state(3))
        connector.last.receive(ServerMessage.MatchOver(MatchResult.WON, EndReason.DISCONNECT, 3))
        connector.last.hangUp()
        runCurrent()

        val over = viewModel.uiState.value
        assertEquals(OnlinePhase.OVER, over.phase)
        assertEquals(Outcome.WON, over.battle.outcome)
        assertEquals(EndNote.OPPONENT_DROPPED, over.battle.endNote)
        assertTrue(over.battle.actions.none { it.enabled })
        assertNull(over.battle.rewards)
        assertTrue(connector.last.closed)
    }

    @Test
    fun aKnockoutNeedsNoNote() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.receive(started)
        connector.last.receive(ServerMessage.MatchOver(MatchResult.DRAW, EndReason.TIME, 900))
        runCurrent()
        assertEquals(Outcome.DRAW, viewModel.uiState.value.battle.outcome)
        assertEquals(EndNote.TIME, viewModel.uiState.value.battle.endNote)

        connector.connections.clear()
        val other = viewModel()
        runCurrent()
        connector.last.receive(started)
        connector.last.receive(ServerMessage.MatchOver(MatchResult.LOST, EndReason.KNOCKOUT, 40))
        runCurrent()
        assertNull(other.uiState.value.battle.endNote)
    }

    @Test
    fun aDroppedConnectionFails() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.receive(started)
        connector.last.receive(state(2))
        connector.last.hangUp(IOException("connection reset"))
        runCurrent()

        assertEquals(OnlinePhase.FAILED, viewModel.uiState.value.phase)
        assertEquals("connection reset", viewModel.uiState.value.problem)
    }

    @Test
    fun aServerThatHangsUpSilentlyFailsWithNoReason() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.hangUp()
        runCurrent()

        assertEquals(OnlinePhase.FAILED, viewModel.uiState.value.phase)
        assertNull(viewModel.uiState.value.problem)
    }

    @Test
    fun aRefusedJoinFailsWithTheServersReason() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.receive(ServerMessage.Rejected("unknown arena: open"))
        runCurrent()

        assertEquals(OnlinePhase.FAILED, viewModel.uiState.value.phase)
        assertEquals("unknown arena: open", viewModel.uiState.value.problem)
    }

    @Test
    fun aRefusedInputMidMatchIsIgnored() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        connector.last.receive(started)
        connector.last.receive(ServerMessage.Rejected("can't queue slot 0 now"))
        runCurrent()
        assertEquals(OnlinePhase.PLAYING, viewModel.uiState.value.phase)
    }

    @Test
    fun againStartsOverOnAFreshConnection() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        val first = connector.last
        first.receive(started)
        first.receive(ServerMessage.MatchOver(MatchResult.LOST, EndReason.KNOCKOUT, 40))
        runCurrent()

        viewModel.again()
        runCurrent()
        assertEquals(2, connector.connections.size)
        assertTrue(first.closed)
        assertEquals(listOf(ClientMessage.Join(red, "open")), connector.last.sent)
        val fresh = viewModel.uiState.value
        assertEquals(OnlinePhase.CONNECTING, fresh.phase)
        assertNull(fresh.battle.outcome)
    }

    @Test
    fun aConnectionThatCantBeOpenedFails() = runTest(dispatcher) {
        val viewModel = OnlineBattleViewModel({ throw IllegalArgumentException("bad url") }, URL, red, Arenas.OPEN)
        runCurrent()
        assertEquals(OnlinePhase.FAILED, viewModel.uiState.value.phase)
        assertEquals("bad url", viewModel.uiState.value.problem)
        assertFalse(viewModel.uiState.value.battle.isOver)
    }

    private companion object {
        const val URL = "ws://test:8080/play"
        const val TICK_MS = 1000L
    }
}

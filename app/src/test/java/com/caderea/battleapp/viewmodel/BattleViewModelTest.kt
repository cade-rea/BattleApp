package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.Battle
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.fighter.Fighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Runs the ViewModel's tick loop on virtual time, so a whole battle takes no real time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BattleViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun showsTheActionButtons() {
        val actions = BattleViewModel(TICK_MS).uiState.value.actions

        assertEquals(listOf("Red Strike", "Green Mend", "Green Strike", "Blue Guard"), actions.map { it.name })
        assertEquals(listOf(10, 7, 7, 10), actions.map { it.cost })
        assertEquals(
            listOf(ActionKind.ATTACK, ActionKind.HEAL, ActionKind.ATTACK, ActionKind.GUARD),
            actions.map { it.kind },
        )
        assertEquals(listOf(1, 2, 2, 3), actions.map { it.windUpTicks })
        assertTrue(actions.all { it.enabled })
    }

    @Test
    fun actionButtonsPreviewWhatTheyDo() {
        val actions = BattleViewModel(TICK_MS).uiState.value.actions

        // green beats the enemy's blue aura, red is beaten by it
        assertEquals(1.5, actions[GREEN_STRIKE].effectiveness, 1e-9)
        assertEquals(0.5, actions[0].effectiveness, 1e-9)
        assertEquals(414, actions[GREEN_STRIKE].effect)
        assertTrue(actions[1].effect > 0)
        assertTrue(actions[3].effect in 1..90)
    }

    @Test
    fun buttonsYouCantAffordAreDisabled() {
        val viewModel = BattleViewModel(TICK_MS)

        // two red strikes spend all 20 starting energy
        viewModel.queueAction(0)
        viewModel.queueAction(0)

        val state = viewModel.uiState.value
        assertEquals(0, state.player.energy)
        assertTrue(state.actions.none { it.enabled })
    }

    @Test
    fun cancelTakesBackTheLastQueuedAction() {
        val viewModel = BattleViewModel(TICK_MS)
        viewModel.queueAction(0)

        viewModel.cancelLast()

        assertTrue(viewModel.uiState.value.playerQueue.isEmpty())
        assertEquals(15, viewModel.uiState.value.player.energy)
    }

    @Test
    fun showsBothFightersStatus() {
        val state = BattleViewModel(TICK_MS).uiState.value

        assertEquals(FighterState("Default Fighter", RgbColor.RED, 2000, 2000, 20, 30, null), state.player)
        assertEquals("Enemy", state.enemy.name)
        // the computer opponent opens with a telegraphed strike
        assertEquals(listOf("Strike"), state.enemyQueue.map { it.action.name })
    }

    @Test
    fun queuedActionShowsUpRightAway() {
        val viewModel = BattleViewModel(TICK_MS)

        viewModel.queueAction(0)

        assertEquals(1, viewModel.uiState.value.playerQueue.size)
    }

    @Test
    fun battleTicksOncePerTickLength() = runTest(dispatcher) {
        val viewModel = BattleViewModel(TICK_MS)
        viewModel.queueAction(GREEN_STRIKE)

        advanceTimeBy(TICK_MS - 1)
        assertEquals(0, viewModel.uiState.value.tick)

        // the green strike winds up on the first tick and lands on the second
        advanceTimeBy(2)
        assertEquals(1, viewModel.uiState.value.playerQueue.single().ticksLeft)

        advanceTimeBy(TICK_MS)
        val landed = viewModel.uiState.value
        assertEquals(listOf(LandedAction(Side.PLAYER, ActionKind.ATTACK, RgbColor.GREEN)), landed.landed)
        assertEquals(-414, landed.enemy.healthChange)
        val log = viewModel.uiState.value.log
        assertTrue(
            log.contains(LogEntry(2, "Default Fighter attacks Enemy with Green Strike for 414 damage.", Side.PLAYER))
        )
        assertTrue(viewModel.uiState.value.playerQueue.isEmpty())
    }

    @Test
    fun idlePlayerLosesAndTheBattleFinishes() = runTest(dispatcher) {
        val viewModel = BattleViewModel(TICK_MS)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Outcome.LOST, state.outcome)
        assertEquals("Battle Finished", state.log.last().text)
        assertTrue(state.log.any { it.text == "Default Fighter has died." })
    }

    @Test
    fun stopEndsTheBattleImmediately() = runTest(dispatcher) {
        val viewModel = BattleViewModel(TICK_MS)
        advanceTimeBy(TICK_MS / 2)
        assertFalse(viewModel.uiState.value.isOver)

        viewModel.stop()

        val state = viewModel.uiState.value
        assertEquals(Outcome.STOPPED, state.outcome)
        assertEquals(listOf("Battle Finished"), state.log.map { it.text })

        // the tick loop is cancelled, so nothing more happens
        advanceUntilIdle()
        assertEquals(listOf("Battle Finished"), viewModel.uiState.value.log.map { it.text })
    }

    @Test
    fun logKeepsOnlyTheLatestLines() = runTest(dispatcher) {
        // a sturdy player makes the fight last long enough to fill the log
        val viewModel = BattleViewModel(TICK_MS) { Battle(player = Fighter(maxHealth = 100_000)) }

        advanceUntilIdle()

        assertEquals(BattleViewModel.MAX_LOG_LINES, viewModel.uiState.value.log.size)
    }

    @Test
    fun pauseHoldsTheTickLoop() = runTest(dispatcher) {
        val viewModel = BattleViewModel(TICK_MS)
        viewModel.togglePause()
        assertTrue(viewModel.uiState.value.isPaused)

        advanceTimeBy(TICK_MS * 5)
        assertEquals(0, viewModel.uiState.value.tick)

        viewModel.togglePause()
        advanceTimeBy(TICK_MS * 2)
        assertFalse(viewModel.uiState.value.isPaused)
        assertTrue(viewModel.uiState.value.tick >= 1)
        viewModel.stop()
    }

    @Test
    fun rematchStartsAFreshBattle() = runTest(dispatcher) {
        val viewModel = BattleViewModel(TICK_MS)
        advanceUntilIdle()
        assertEquals(Outcome.LOST, viewModel.uiState.value.outcome)

        viewModel.rematch()

        val state = viewModel.uiState.value
        assertNull(state.outcome)
        assertEquals(0, state.tick)
        assertEquals(2000, state.player.health)
        assertTrue(state.log.isEmpty())

        // and the new battle ticks again
        advanceTimeBy(TICK_MS + 1)
        assertEquals(1, viewModel.uiState.value.tick)
        viewModel.stop()
    }

    private companion object {
        const val TICK_MS = 1000L
        const val GREEN_STRIKE = 2
    }
}

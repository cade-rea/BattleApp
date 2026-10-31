package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.Battle
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
        val viewModel = BattleViewModel(TICK_MS)

        assertEquals(
            listOf("Red Strike", "Green Mend", "Green Strike", ""),
            viewModel.uiState.value.actionNames,
        )
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
        assertTrue(viewModel.uiState.value.log.isEmpty())

        advanceTimeBy(2)
        val log = viewModel.uiState.value.log
        assertTrue(log.contains("Default Fighter attacks Enemy with Green Strike for 414 damage."))
        assertTrue(viewModel.uiState.value.playerQueue.isEmpty())
    }

    @Test
    fun idlePlayerLosesAndTheBattleFinishes() = runTest(dispatcher) {
        val viewModel = BattleViewModel(TICK_MS)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOver)
        assertEquals("Battle Finished", state.log.last())
        assertTrue(state.log.contains("Default Fighter has died."))
    }

    @Test
    fun stopEndsTheBattleImmediately() = runTest(dispatcher) {
        val viewModel = BattleViewModel(TICK_MS)
        advanceTimeBy(TICK_MS / 2)
        assertFalse(viewModel.uiState.value.isOver)

        viewModel.stop()

        val state = viewModel.uiState.value
        assertTrue(state.isOver)
        assertEquals(listOf("Battle Finished"), state.log)

        // the tick loop is cancelled, so nothing more happens
        advanceUntilIdle()
        assertEquals(listOf("Battle Finished"), viewModel.uiState.value.log)
    }

    @Test
    fun logKeepsOnlyTheLatestLines() = runTest(dispatcher) {
        // a sturdy player makes the fight last long enough to fill the log
        val viewModel = BattleViewModel(TICK_MS, Battle(player = Fighter(maxHealth = 10_000)))

        advanceUntilIdle()

        assertEquals(10, viewModel.uiState.value.log.size)
    }

    private companion object {
        const val TICK_MS = 1000L
        const val GREEN_STRIKE = 2
    }
}

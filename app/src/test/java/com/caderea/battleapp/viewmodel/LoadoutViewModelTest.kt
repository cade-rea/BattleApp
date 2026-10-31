package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.technique.TechniqueRoller
import com.caderea.battleapp.data.repositoryWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class LoadoutViewModelTest {

    private val profiles = repositoryWith(run {
        FighterProfile.create("Cade", Channel.RED, TechniqueRoller(Random(9)))
    })

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun showsTheLoadoutFirstThenTheRestOfTheCatalog() {
        val state = LoadoutViewModel(profiles).uiState.value

        // outside an arena, the whole saved order is editable
        assertEquals(6, state.slots)
        assertNull(state.arena)
        assertEquals(4, state.chosen)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), state.techniques.map { it.id })
        assertEquals(listOf(0, 1, 2, 3, null, null), state.techniques.map { it.slot })
        // the starter red strike against a neutral foe: 255 x red strength 0.8 x full affinity
        assertEquals(255, state.techniques[0].effect)
    }

    @Test
    fun tappingTakesTechniquesOutAndPutsThemBack() {
        val viewModel = LoadoutViewModel(profiles)

        viewModel.toggle(2)
        assertEquals(listOf(1, 3, 4), profiles.requireActive().loadout)
        assertEquals(3, viewModel.uiState.value.chosen)

        viewModel.toggle(6)
        assertEquals(listOf(1, 3, 4, 6), profiles.requireActive().loadout)
        assertEquals(3, viewModel.uiState.value.techniques.first { it.id == 6 }.slot)
        assertNull(viewModel.uiState.value.techniques.first { it.id == 2 }.slot)
    }

    @Test
    fun aFullLoadoutIgnoresNewPicks() {
        val viewModel = LoadoutViewModel(profiles, Arenas.OPEN)

        viewModel.toggle(5)

        assertEquals(listOf(1, 2, 3, 4), profiles.requireActive().loadout)
    }

    @Test
    fun anArenaEditsOnlyItsOwnSlotsAndKeepsTheRest() {
        profiles.update { it.withLoadout(listOf(1, 2, 3, 4, 5, 6)) }
        val viewModel = LoadoutViewModel(profiles, Arenas.OPEN)

        assertEquals(4, viewModel.uiState.value.slots)
        assertEquals("open", viewModel.uiState.value.arena?.id)
        assertEquals(listOf(0, 1, 2, 3, null, null), viewModel.uiState.value.techniques.map { it.slot })

        // taking one out moves the next saved pick up into this arena's slots
        viewModel.toggle(2)
        assertEquals(listOf(1, 3, 4, 5, 6), profiles.requireActive().loadout)
        assertEquals(listOf(1, 3, 4, 5), viewModel.uiState.value.techniques.take(4).map { it.id })
    }

    @Test
    fun cardsShowTheArenasLight() {
        val plain = LoadoutViewModel(profiles).uiState.value.techniques.first { it.id == 1 }
        val red = LoadoutViewModel(profiles, Arenas.RED_GLOW).uiState.value.techniques.first { it.id == 1 }
        val veiled = LoadoutViewModel(profiles, Arenas.RED_VEIL).uiState.value.techniques.first { it.id == 1 }

        // the starter red strike
        assertEquals(255, plain.effect)
        assertTrue(red.effect > plain.effect)
        assertTrue(veiled.effect in 127..128)
    }
}

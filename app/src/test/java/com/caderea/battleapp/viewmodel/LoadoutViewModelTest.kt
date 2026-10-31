package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.technique.TechniqueRoller
import com.caderea.battleapp.data.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class LoadoutViewModelTest {

    private val profiles = ProfileRepository(File.createTempFile("fighter", ".json").apply { delete() }) {
        FighterProfile.create("Cade", Channel.RED, TechniqueRoller(Random(9)))
    }

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

        assertEquals(4, state.slots)
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
        assertEquals(listOf(1, 3, 4), profiles.profile.value.loadout)
        assertEquals(3, viewModel.uiState.value.chosen)

        viewModel.toggle(6)
        assertEquals(listOf(1, 3, 4, 6), profiles.profile.value.loadout)
        assertEquals(3, viewModel.uiState.value.techniques.first { it.id == 6 }.slot)
        assertNull(viewModel.uiState.value.techniques.first { it.id == 2 }.slot)
    }

    @Test
    fun aFullLoadoutIgnoresNewPicks() {
        val viewModel = LoadoutViewModel(profiles)

        viewModel.toggle(5)

        assertEquals(listOf(1, 2, 3, 4), profiles.profile.value.loadout)
    }
}

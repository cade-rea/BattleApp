package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.CharacterCreator
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.technique.TechniqueRoller
import com.caderea.battleapp.data.emptyRepository
import com.caderea.battleapp.data.repositoryWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterCreationViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(profiles: com.caderea.battleapp.data.ProfileRepository = emptyRepository()) =
        CharacterCreationViewModel(profiles, CharacterCreator(TechniqueRoller(Random(8))))

    @Test
    fun theDraftNeedsANameAndAColor() {
        val viewModel = viewModel()

        viewModel.startDraft()
        assertEquals(CreationStep.IDENTITY, viewModel.uiState.value.step)

        viewModel.setName("   ")
        viewModel.setBase(Channel.BLUE)
        viewModel.startDraft()
        assertFalse(viewModel.uiState.value.canStart)
        assertEquals(CreationStep.IDENTITY, viewModel.uiState.value.step)

        viewModel.setName("Nova")
        viewModel.startDraft()
        assertEquals(CreationStep.DRAFT, viewModel.uiState.value.step)
        assertEquals(3, viewModel.uiState.value.offer.size)
    }

    @Test
    fun fourPicksMakeAndLoadTheCharacter() {
        val profiles = repositoryWith(FighterProfile.create("Old", Channel.RED))
        val viewModel = viewModel(profiles)
        viewModel.setName("Nova")
        viewModel.setBase(Channel.BLUE)
        viewModel.startDraft()

        val kept = mutableListOf<String>()
        repeat(4) { round ->
            val state = viewModel.uiState.value
            assertEquals(round + 1, state.round)
            assertEquals(round == 3, state.signatureRound)
            assertEquals(round == 3, state.offer.all { it.signature })
            assertEquals(round, state.picks.size)
            kept += state.offer[round % 3].name
            assertEquals(1, profiles.roster.value.size)
            viewModel.pick(round % 3)
        }

        val state = viewModel.uiState.value
        assertEquals(CreationStep.READY, state.step)
        assertEquals(kept, state.picks.map { it.name })
        assertEquals(2, profiles.roster.value.size)
        val nova = profiles.requireActive()
        assertEquals("Nova", nova.name)
        assertEquals(Channel.BLUE, nova.aura.base)
        assertEquals(kept, nova.catalog.map { it.name })
        assertTrue(nova.catalog.last().signature)
    }

    @Test
    fun startingOverKeepsTheNameAndColorButDropsThePicks() {
        val profiles = emptyRepository()
        val viewModel = viewModel(profiles)
        viewModel.setName("Nova")
        viewModel.setBase(Channel.GREEN)
        viewModel.startDraft()
        viewModel.pick(0)

        viewModel.startOver()

        val state = viewModel.uiState.value
        assertEquals(CreationStep.IDENTITY, state.step)
        assertEquals("Nova", state.name)
        assertEquals(Channel.GREEN, state.base)
        assertTrue(state.picks.isEmpty())
        assertNull(profiles.active.value)
    }
}

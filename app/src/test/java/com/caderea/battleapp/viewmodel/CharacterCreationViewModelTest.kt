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
        assertEquals(CreationStep.SIGNATURE, viewModel.uiState.value.step)
        assertEquals(3, viewModel.uiState.value.offer.size)
    }

    @Test
    fun aSignatureThenTwoOfSixMakeAndLoadTheCharacter() {
        val profiles = repositoryWith(FighterProfile.create("Old", Channel.RED))
        val viewModel = viewModel(profiles)
        viewModel.setName("Nova")
        viewModel.setBase(Channel.BLUE)
        viewModel.startDraft()

        var state = viewModel.uiState.value
        assertEquals(CreationStep.SIGNATURE, state.step)
        assertEquals(3, state.offer.size)
        assertTrue(state.offer.all { it.signature })
        val signature = state.offer[1].name
        viewModel.chooseSignature(1)

        state = viewModel.uiState.value
        assertEquals(CreationStep.TECHNIQUES, state.step)
        assertEquals(listOf(signature), state.kept.map { it.name })
        assertEquals(6, state.offer.size)
        assertFalse(state.canKeep)

        val pair = keepablePair(state)
        val offered = state.offer.map { it.name }
        viewModel.toggle(pair[0])
        assertFalse(viewModel.uiState.value.canKeep)
        viewModel.toggle(pair[1])
        assertTrue(viewModel.uiState.value.canKeep)
        assertEquals(1, profiles.roster.value.size)
        viewModel.keep()

        state = viewModel.uiState.value
        val expected = listOf(signature) + pair.map { offered[it] }
        assertEquals(CreationStep.READY, state.step)
        assertEquals(expected, state.kept.map { it.name })
        assertEquals(2, profiles.roster.value.size)
        val nova = profiles.requireActive()
        assertEquals("Nova", nova.name)
        assertEquals(Channel.BLUE, nova.aura.base)
        assertEquals(expected, nova.catalog.map { it.name })
        assertTrue(nova.catalog.first().signature)
    }

    @Test
    fun onlyTwoCanBeMarkedAndAFighterNeedsAnAttack() {
        // try seeds until the signature offer has a non-attack and the six have two non-attacks
        for (seed in 1..80) {
            val viewModel = CharacterCreationViewModel(emptyRepository(), CharacterCreator(TechniqueRoller(Random(seed))))
            viewModel.setName("Nova")
            viewModel.setBase(Channel.GREEN)
            viewModel.startDraft()
            val support = viewModel.uiState.value.offer.indexOfFirst { it.kind != ActionKind.ATTACK }
            if (support < 0) continue
            viewModel.chooseSignature(support)
            val nonAttacks = viewModel.uiState.value.offer.indices.filter { viewModel.uiState.value.offer[it].kind != ActionKind.ATTACK }
            if (nonAttacks.size < 3) continue

            nonAttacks.take(3).forEach(viewModel::toggle)
            val state = viewModel.uiState.value
            assertEquals(nonAttacks.take(2).toSet(), state.selected)
            assertFalse(state.canKeep)
            assertTrue(state.needsAttack)
            viewModel.keep()
            assertEquals(CreationStep.TECHNIQUES, viewModel.uiState.value.step)
            return
        }
        error("no seed gave a draft without attacks to try")
    }

    private fun keepablePair(state: CharacterCreationUiState): List<Int> {
        val hasAttack = state.kept.any { it.kind == ActionKind.ATTACK }
        val attack = state.offer.indexOfFirst { it.kind == ActionKind.ATTACK }
        val first = if (hasAttack) 0 else attack
        return listOf(first, state.offer.indices.first { it != first }).sorted()
    }

    @Test
    fun startingOverKeepsTheNameAndColorButDropsThePicks() {
        val profiles = emptyRepository()
        val viewModel = viewModel(profiles)
        viewModel.setName("Nova")
        viewModel.setBase(Channel.GREEN)
        viewModel.startDraft()
        viewModel.chooseSignature(0)

        viewModel.startOver()

        val state = viewModel.uiState.value
        assertEquals(CreationStep.IDENTITY, state.step)
        assertEquals("Nova", state.name)
        assertEquals(Channel.GREEN, state.base)
        assertTrue(state.kept.isEmpty())
        assertNull(profiles.active.value)
    }
}

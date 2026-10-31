package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.data.emptyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun switchingAndDeletingShowUpInTheList() {
        val profiles = emptyRepository()
        val ada = profiles.add(FighterProfile.create("Ada", Channel.RED))
        profiles.add(FighterProfile.create("Bo", Channel.BLUE))
        val viewModel = CharactersViewModel(profiles)

        assertEquals(listOf(false, true), viewModel.uiState.value.characters.map { it.active })

        viewModel.select(ada)
        assertEquals(listOf(true, false), viewModel.uiState.value.characters.map { it.active })

        viewModel.delete(ada)
        assertEquals(listOf("Bo"), viewModel.uiState.value.characters.map { it.fighter.name })
        assertEquals(listOf(true), viewModel.uiState.value.characters.map { it.active })
    }
}

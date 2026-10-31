package com.caderea.battleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.data.SavedProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** One saved character in the list. */
data class CharacterRow(val id: String, val fighter: FighterSummary, val active: Boolean)

data class CharactersUiState(val characters: List<CharacterRow> = emptyList())

/** Every saved character: switch which one is loaded, or delete one. */
class CharactersViewModel(private val profiles: ProfileRepository) : ViewModel() {

    val uiState: StateFlow<CharactersUiState> = combine(profiles.roster, profiles.activeId, ::toState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, toState(profiles.roster.value, profiles.activeId.value))

    fun select(id: String) = profiles.select(id)

    fun delete(id: String) = profiles.delete(id)

    private fun toState(roster: List<SavedProfile>, activeId: String?) = CharactersUiState(
        roster.map { CharacterRow(it.id, it.profile.toSummary(), it.id == activeId) }
    )

    companion object {
        fun factory(profiles: ProfileRepository) = viewModelFactory {
            initializer { CharactersViewModel(profiles) }
        }
    }
}

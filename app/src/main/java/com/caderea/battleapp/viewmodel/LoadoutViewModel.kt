package com.caderea.battleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.data.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The loadout screen: the fighter, their whole catalog, and which techniques come along. */
data class LoadoutUiState(
    val fighter: FighterSummary = FighterSummary(),
    // how many techniques the arena allows
    val slots: Int = Environment.MIN_LOADOUT,
    // chosen techniques first, in button order, then the rest of the catalog
    val techniques: List<TechniqueCardState> = emptyList(),
) {
    val chosen: Int
        get() = techniques.count { it.slot != null }
}

/**
 * Lets the player pick which techniques from their catalog to bring. Each tap is saved
 * right away. No arenas exist yet, so the slot count is the default arena's.
 */
class LoadoutViewModel(
    private val profiles: ProfileRepository,
    private val slots: Int = Environment().loadoutSize,
) : ViewModel() {

    val uiState: StateFlow<LoadoutUiState> = profiles.active
        .filterNotNull()
        .map { it.toLoadoutState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, profiles.requireActive().toLoadoutState())

    /**
     * Adds the technique to the end of the loadout, or takes it out if it's already in. A
     * full loadout ignores new picks until one is taken out.
     */
    fun toggle(id: Int) {
        profiles.update { profile ->
            val chosen = profile.loadout.take(slots)
            when {
                id in chosen -> profile.withLoadout(chosen - id)
                chosen.size < slots && profile.technique(id) != null -> profile.withLoadout(chosen + id)
                else -> profile
            }
        }
    }

    private fun FighterProfile.toLoadoutState(): LoadoutUiState {
        val chosen = loadout.take(slots)
        val color = aura.color
        val cards = chosen.mapIndexedNotNull { slot, id -> technique(id)?.toCard(color, slot) } +
            catalog.filter { it.id !in chosen }.map { it.toCard(color) }
        return LoadoutUiState(toSummary(), slots, cards)
    }

    companion object {
        fun factory(profiles: ProfileRepository) = viewModelFactory {
            initializer { LoadoutViewModel(profiles) }
        }
    }
}

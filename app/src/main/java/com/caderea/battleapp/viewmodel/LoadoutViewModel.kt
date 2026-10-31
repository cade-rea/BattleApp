package com.caderea.battleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caderea.battleapp.core.environment.Arena
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
    // how many techniques the arena allows, or the most any arena allows outside one
    val slots: Int = Environment.MAX_LOADOUT,
    // the arena the player is about to fight in, or null when just managing techniques
    val arena: ArenaCardState? = null,
    // chosen techniques first, in button order, then the rest of the catalog
    val techniques: List<TechniqueCardState> = emptyList(),
) {
    val chosen: Int
        get() = techniques.count { it.slot != null }
}

/**
 * Lets the player pick which techniques from their catalog to bring. Each tap is saved
 * right away.
 *
 * The saved loadout is one ordered list of up to [Environment.MAX_LOADOUT]; an arena with
 * fewer slots takes the first ones. Before a fight in [arena] the screen edits just those
 * first slots and shows each technique in the arena's light; without one it edits them all.
 */
class LoadoutViewModel(
    private val profiles: ProfileRepository,
    private val arena: Arena? = null,
) : ViewModel() {

    private val slots: Int = arena?.loadoutSize ?: Environment.MAX_LOADOUT
    private val environment: Environment = arena ?: Environment()

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
            // picks past this arena's slots stay saved for bigger arenas
            val rest = profile.loadout.drop(slots)
            when {
                id in chosen -> profile.withLoadout(chosen - id + rest)
                chosen.size < slots && profile.technique(id) != null ->
                    profile.withLoadout(chosen + id + (rest - id))
                else -> profile
            }
        }
    }

    private fun FighterProfile.toLoadoutState(): LoadoutUiState {
        val chosen = loadout.take(slots)
        val color = aura.color
        val cards = chosen.mapIndexedNotNull { slot, id -> technique(id)?.toCard(color, slot, environment, level) } +
            catalog.filter { it.id !in chosen }.map { it.toCard(color, environment = environment, level = level) }
        return LoadoutUiState(toSummary(), slots, arena?.toCard(), cards)
    }

    companion object {
        fun factory(profiles: ProfileRepository, arena: Arena? = null) = viewModelFactory {
            initializer { LoadoutViewModel(profiles, arena) }
        }
    }
}

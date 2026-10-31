package com.caderea.battleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.CharacterCreator
import com.caderea.battleapp.core.fighter.CharacterDraft
import com.caderea.battleapp.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Where character creation is up to. */
enum class CreationStep {
    /** Typing a name and picking a base color. */
    IDENTITY,
    /** Picking one of three rolled techniques, four times over. */
    DRAFT,
    /** Saved and loaded, ready to fight. */
    READY,
}

data class CharacterCreationUiState(
    val step: CreationStep = CreationStep.IDENTITY,
    val name: String = "",
    val base: Channel? = null,
    // the round being picked, from 1
    val round: Int = 1,
    val rounds: Int = CharacterDraft.ROUNDS,
    val signatureRound: Boolean = false,
    val offer: List<TechniqueCardState> = emptyList(),
    val picks: List<TechniqueCardState> = emptyList(),
    // the new fighter, once the draft has started
    val fighter: FighterSummary? = null,
) {
    val canStart: Boolean
        get() = base != null && CharacterDraft.cleanName(name) != null
}

/**
 * Makes a new character: a name and base primary, then a draft of rolled techniques (see
 * [CharacterDraft]). The finished character is saved and loaded straight away. Leaving
 * before the last pick saves nothing.
 */
class CharacterCreationViewModel(
    private val profiles: ProfileRepository,
    private val creator: CharacterCreator = CharacterCreator(),
) : ViewModel() {

    private var draft: CharacterDraft? = null

    private val _uiState = MutableStateFlow(CharacterCreationUiState())
    val uiState: StateFlow<CharacterCreationUiState> = _uiState.asStateFlow()

    fun setName(name: String) {
        if (_uiState.value.step != CreationStep.IDENTITY) return
        _uiState.update { it.copy(name = name.take(CharacterDraft.MAX_NAME_LENGTH)) }
    }

    fun setBase(base: Channel) {
        if (_uiState.value.step != CreationStep.IDENTITY) return
        _uiState.update { it.copy(base = base) }
    }

    /** Rolls the first three techniques. Does nothing until there's a name and a color. */
    fun startDraft() {
        val state = _uiState.value
        if (state.step != CreationStep.IDENTITY || !state.canStart) return
        show(creator.start(state.name, state.base!!))
    }

    /** Keeps offer [index]; the fourth pick finishes the character and saves it. */
    fun pick(index: Int) {
        val current = draft ?: return
        if (current.isComplete || index !in current.offer.indices) return
        val next = creator.pick(current, index)
        if (next.isComplete) profiles.add(next.toProfile())
        show(next)
    }

    /** Throws the picks away and goes back to the name and color. */
    fun startOver() {
        if (_uiState.value.step != CreationStep.DRAFT) return
        draft = null
        _uiState.update { CharacterCreationUiState(name = it.name, base = it.base) }
    }

    private fun show(draft: CharacterDraft) {
        this.draft = draft
        val aura = draft.aura
        _uiState.update {
            it.copy(
                step = if (draft.isComplete) CreationStep.READY else CreationStep.DRAFT,
                name = draft.name,
                round = draft.round,
                signatureRound = draft.isSignatureRound,
                offer = draft.offer.map { technique -> technique.toCard(aura.color) },
                picks = draft.picks.mapIndexed { slot, technique -> technique.toCard(aura.color, slot) },
                fighter = FighterSummary(name = draft.name, color = aura.color),
            )
        }
    }

    companion object {
        fun factory(profiles: ProfileRepository) = viewModelFactory {
            initializer { CharacterCreationViewModel(profiles) }
        }
    }
}

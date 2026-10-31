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
    /** Picking one of three signature techniques. */
    SIGNATURE,
    /** Picking two of six rolled techniques. */
    TECHNIQUES,
    /** Saved and loaded, ready to fight. */
    READY,
}

data class CharacterCreationUiState(
    val step: CreationStep = CreationStep.IDENTITY,
    val name: String = "",
    val base: Channel? = null,
    // what can be picked in this step
    val offer: List<TechniqueCardState> = emptyList(),
    // offers marked in the technique step, by index
    val selected: Set<Int> = emptySet(),
    val picks: Int = CharacterDraft.TECHNIQUE_PICKS,
    // the marked offers would finish the character
    val canKeep: Boolean = false,
    // the chosen signature, then the kept techniques
    val kept: List<TechniqueCardState> = emptyList(),
    // the new fighter, once the draft has started
    val fighter: FighterSummary? = null,
) {
    val canStart: Boolean
        get() = base != null && CharacterDraft.cleanName(name) != null

    /** Two offers are marked but neither they nor the signature is an attack. */
    val needsAttack: Boolean
        get() = step == CreationStep.TECHNIQUES && selected.size == picks && !canKeep
}

/**
 * Makes a new character: a name and base primary, then a signature technique, then two of
 * six rolled techniques (see [CharacterDraft]). The finished character is saved and loaded
 * straight away. Leaving before the end saves nothing.
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

    /** Rolls the signature offer. Does nothing until there's a name and a color. */
    fun startDraft() {
        val state = _uiState.value
        if (state.step != CreationStep.IDENTITY || !state.canStart) return
        show(creator.start(state.name, state.base!!))
    }

    /** Keeps signature offer [index] and rolls the six techniques. */
    fun chooseSignature(index: Int) {
        val current = draft ?: return
        if (!current.isSignatureStep || index !in current.offer.indices) return
        show(creator.chooseSignature(current, index))
    }

    /**
     * Marks or unmarks technique offer [index]. Once two are marked, others are ignored
     * until one is unmarked.
     */
    fun toggle(index: Int) {
        val current = draft ?: return
        if (current.isSignatureStep || current.isComplete || index !in current.offer.indices) return
        val selected = _uiState.value.selected
        val next = when {
            index in selected -> selected - index
            selected.size < CharacterDraft.TECHNIQUE_PICKS -> selected + index
            else -> return
        }
        _uiState.update { it.copy(selected = next, canKeep = current.canKeep(next)) }
    }

    /** Keeps the two marked techniques, which finishes the character and saves it. */
    fun keep() {
        val current = draft ?: return
        val selected = _uiState.value.selected
        if (!current.canKeep(selected)) return
        val done = creator.keep(current, selected)
        profiles.add(done.toProfile())
        show(done)
    }

    /** Throws the draft away and goes back to the name and color. */
    fun startOver() {
        if (draft == null || draft!!.isComplete) return
        draft = null
        _uiState.update { CharacterCreationUiState(name = it.name, base = it.base) }
    }

    private fun show(draft: CharacterDraft) {
        this.draft = draft
        val aura = draft.aura.color
        _uiState.update {
            it.copy(
                step = when {
                    draft.isComplete -> CreationStep.READY
                    draft.isSignatureStep -> CreationStep.SIGNATURE
                    else -> CreationStep.TECHNIQUES
                },
                name = draft.name,
                offer = draft.offer.map { technique -> technique.toCard(aura) },
                selected = emptySet(),
                canKeep = false,
                kept = draft.kept.mapIndexed { slot, technique -> technique.toCard(aura, slot) },
                fighter = FighterSummary(name = draft.name, color = aura),
            )
        }
    }

    companion object {
        fun factory(profiles: ProfileRepository) = viewModelFactory {
            initializer { CharacterCreationViewModel(profiles) }
        }
    }
}

package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.progression.Aura
import com.caderea.battleapp.core.technique.Technique
import com.caderea.battleapp.core.technique.TechniqueKind
import com.caderea.battleapp.core.technique.TechniqueRoller

/**
 * A character being created: a name, a base primary, and the techniques picked so far.
 *
 * Creation is a draft of [ROUNDS] rounds. Each round offers [CHOICES] freshly rolled
 * techniques and the player keeps one. The last round is the signature round: every offer
 * is the pure base primary and especially strong, fast or cheap. Once every round is picked
 * the draft [isComplete] and [toProfile] makes the fighter.
 *
 * Every offer holds at least one attack, and if the first rounds were all heals and guards
 * the signature round offers only attacks, so no fighter starts unable to hurt anyone.
 *
 * Plain immutable data; [CharacterCreator] rolls the offers.
 */
data class CharacterDraft(
    val name: String,
    val base: Channel,
    val picks: List<Technique> = emptyList(),
    val offer: List<Technique> = emptyList(),
) {

    init {
        require(picks.size <= ROUNDS) { "a draft has only $ROUNDS rounds" }
    }

    /** The round being picked, from 1, or [ROUNDS] + 1 once done. */
    val round: Int
        get() = picks.size + 1

    val isSignatureRound: Boolean
        get() = picks.size == ROUNDS - 1

    val isComplete: Boolean
        get() = picks.size == ROUNDS

    val aura: Aura
        get() = Aura.start(base)

    /** The new level 1 fighter: the picked techniques, all of them in the loadout. */
    fun toProfile(): FighterProfile {
        check(isComplete) { "the draft isn't finished: ${picks.size} of $ROUNDS picked" }
        return FighterProfile(name, aura, picks, loadout = picks.map { it.id })
    }

    companion object {
        const val ROUNDS = 4
        const val CHOICES = 3
        const val MAX_NAME_LENGTH = 16

        /** The name with stray spaces trimmed, or null if nothing usable is left. */
        fun cleanName(name: String): String? =
            name.trim().replace(Regex("\\s+"), " ").take(MAX_NAME_LENGTH).trim().ifEmpty { null }
    }
}

/** Rolls the offers for a [CharacterDraft]. Pass a seeded roller for repeatable drafts. */
class CharacterCreator(private val roller: TechniqueRoller = TechniqueRoller()) {

    /** A new draft with its first offer rolled. [name] must be usable (see [CharacterDraft.cleanName]). */
    fun start(name: String, base: Channel): CharacterDraft {
        val clean = requireNotNull(CharacterDraft.cleanName(name)) { "a character needs a name" }
        val draft = CharacterDraft(clean, base)
        return draft.copy(offer = offerFor(draft))
    }

    /** Keeps offer [choice] and rolls the next round's offer, if any round is left. */
    fun pick(draft: CharacterDraft, choice: Int): CharacterDraft {
        check(!draft.isComplete) { "the draft is already finished" }
        require(choice in draft.offer.indices) { "no offer $choice" }
        val picked = draft.copy(picks = draft.picks + draft.offer[choice], offer = emptyList())
        return if (picked.isComplete) picked else picked.copy(offer = offerFor(picked))
    }

    private fun offerFor(draft: CharacterDraft): List<Technique> {
        val id = draft.picks.size + 1
        val aura = draft.aura
        return if (draft.isSignatureRound) {
            val needsAttack = draft.picks.none { it.kind == TechniqueKind.ATTACK }
            val kinds = if (needsAttack) List(CharacterDraft.CHOICES) { TechniqueKind.ATTACK } else kinds()
            val standouts = TechniqueRoller.usefulStandouts(draft.base).shuffled(roller.random)
            kinds.mapIndexed { index, kind ->
                roller.rollSignature(aura, id, kind, standouts[index % standouts.size])
            }
        } else {
            kinds().map { roller.roll(aura, id, it) }
        }
    }

    /** [CharacterDraft.CHOICES] rolled kinds with at least one attack among them. */
    private fun kinds(): List<TechniqueKind> {
        val kinds = MutableList(CharacterDraft.CHOICES) { roller.rollKind() }
        if (TechniqueKind.ATTACK !in kinds) kinds[roller.random.nextInt(kinds.size)] = TechniqueKind.ATTACK
        return kinds
    }
}

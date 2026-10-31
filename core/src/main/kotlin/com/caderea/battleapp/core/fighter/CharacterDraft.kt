package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.progression.Aura
import com.caderea.battleapp.core.technique.Technique
import com.caderea.battleapp.core.technique.TechniqueKind
import com.caderea.battleapp.core.technique.TechniqueRoller

/**
 * A character being created: a name, a base primary, and the techniques chosen so far.
 *
 * Creation is a draft in two steps:
 *
 * 1. **Signature.** [SIGNATURE_CHOICES] signature techniques are offered, each the pure base
 *    primary and especially strong, fast or cheap. The player keeps one, so they know what
 *    they're building around before anything else.
 * 2. **Techniques.** [TECHNIQUE_CHOICES] normal rolls are offered and the player keeps
 *    [TECHNIQUE_PICKS] of them.
 *
 * Then the draft [isComplete] and [toProfile] makes a fighter with the signature and the
 * picks, [STARTING_TECHNIQUES] techniques in all.
 *
 * Every offer of normal rolls holds at least one attack, and a set of picks that leaves the
 * fighter with no attack at all isn't allowed (see [canKeep]), so no fighter starts unable to
 * hurt anyone.
 *
 * Plain immutable data; [CharacterCreator] rolls the offers.
 */
data class CharacterDraft(
    val name: String,
    val base: Channel,
    val signature: Technique? = null,
    val picks: List<Technique> = emptyList(),
    val offer: List<Technique> = emptyList(),
) {

    val isSignatureStep: Boolean
        get() = signature == null

    val isComplete: Boolean
        get() = signature != null && picks.size == TECHNIQUE_PICKS

    val aura: Aura
        get() = Aura.start(base)

    /** Every technique kept so far, signature first. */
    val kept: List<Technique>
        get() = listOfNotNull(signature) + picks

    /**
     * Whether keeping offers [indices] in the technique step would finish the draft: exactly
     * [TECHNIQUE_PICKS] different offers, and an attack somewhere among them and the signature.
     */
    fun canKeep(indices: Collection<Int>): Boolean {
        val chosen = indices.toSet()
        if (isSignatureStep || isComplete) return false
        if (chosen.size != TECHNIQUE_PICKS || chosen.any { it !in offer.indices }) return false
        return (chosen.map { offer[it] } + listOfNotNull(signature)).any { it.kind == TechniqueKind.ATTACK }
    }

    /** The new level 1 fighter: the signature and the picks, all of them in the loadout. */
    fun toProfile(): FighterProfile {
        check(isComplete) { "the draft isn't finished" }
        return FighterProfile(name, aura, kept, loadout = kept.map { it.id })
    }

    companion object {
        const val SIGNATURE_CHOICES = 3
        const val TECHNIQUE_CHOICES = 6
        const val TECHNIQUE_PICKS = 2
        const val STARTING_TECHNIQUES = 1 + TECHNIQUE_PICKS
        const val MAX_NAME_LENGTH = 16

        /** The name with stray spaces trimmed, or null if nothing usable is left. */
        fun cleanName(name: String): String? =
            name.trim().replace(Regex("\\s+"), " ").take(MAX_NAME_LENGTH).trim().ifEmpty { null }
    }
}

/** Rolls the offers for a [CharacterDraft]. Pass a seeded roller for repeatable drafts. */
class CharacterCreator(private val roller: TechniqueRoller = TechniqueRoller()) {

    /** A new draft with its signature offer rolled. [name] must be usable (see [CharacterDraft.cleanName]). */
    fun start(name: String, base: Channel): CharacterDraft {
        val clean = requireNotNull(CharacterDraft.cleanName(name)) { "a character needs a name" }
        val draft = CharacterDraft(clean, base)
        return draft.copy(offer = signatureOffer(draft))
    }

    /** Keeps signature offer [choice] and rolls the normal techniques to pick from. */
    fun chooseSignature(draft: CharacterDraft, choice: Int): CharacterDraft {
        check(draft.isSignatureStep) { "the signature is already chosen" }
        require(choice in draft.offer.indices) { "no offer $choice" }
        val signature = draft.offer[choice].copy(id = SIGNATURE_ID)
        val aura = draft.aura
        val offer = kinds(CharacterDraft.TECHNIQUE_CHOICES).mapIndexed { index, kind ->
            roller.roll(aura, id = SIGNATURE_ID + 1 + index, kind = kind)
        }
        return draft.copy(signature = signature, offer = offer)
    }

    /** Keeps technique offers [indices], which finishes the draft. See [CharacterDraft.canKeep]. */
    fun keep(draft: CharacterDraft, indices: Collection<Int>): CharacterDraft {
        require(draft.canKeep(indices)) { "can't keep offers $indices" }
        // numbered after the signature, in the order they were offered
        val picks = indices.toSortedSet().mapIndexed { index, offer ->
            draft.offer[offer].copy(id = SIGNATURE_ID + 1 + index)
        }
        return draft.copy(picks = picks, offer = emptyList())
    }

    /** One signature per useful standout, so the choice is between strong, fast and cheap. */
    private fun signatureOffer(draft: CharacterDraft): List<Technique> {
        val standouts = TechniqueRoller.usefulStandouts(draft.base).shuffled(roller.random)
        return kinds(CharacterDraft.SIGNATURE_CHOICES).mapIndexed { index, kind ->
            roller.rollSignature(draft.aura, SIGNATURE_ID, kind, standouts[index % standouts.size])
        }
    }

    /** [count] rolled kinds with at least one attack among them. */
    private fun kinds(count: Int): List<TechniqueKind> {
        val kinds = MutableList(count) { roller.rollKind() }
        if (TechniqueKind.ATTACK !in kinds) kinds[roller.random.nextInt(kinds.size)] = TechniqueKind.ATTACK
        return kinds
    }

    private companion object {
        const val SIGNATURE_ID = 1
    }
}

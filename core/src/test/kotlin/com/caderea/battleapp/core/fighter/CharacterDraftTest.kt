package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.ColorTraits
import com.caderea.battleapp.core.technique.Standout
import com.caderea.battleapp.core.technique.TechniqueKind
import com.caderea.battleapp.core.technique.TechniqueRoller
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CharacterDraftTest {

    private fun creator(seed: Int = 11) = CharacterCreator(TechniqueRoller(Random(seed)))

    /** The first pair of offers that can be kept. */
    private fun keepable(draft: CharacterDraft): List<Int> {
        val pairs = draft.offer.indices.flatMap { a -> draft.offer.indices.filter { it > a }.map { b -> listOf(a, b) } }
        return pairs.first(draft::canKeep)
    }

    private fun draftAll(creator: CharacterCreator, base: Channel, signature: Int = 0): CharacterDraft {
        val draft = creator.chooseSignature(creator.start("Iris", base), signature)
        return creator.keep(draft, keepable(draft))
    }

    @Test
    fun theSignatureComesFirstThenTwoOfSix() {
        val creator = creator()
        var draft = creator.start("Iris", Channel.BLUE)

        assertTrue(draft.isSignatureStep)
        assertEquals(3, draft.offer.size)
        assertTrue(draft.offer.all { it.signature })

        val chosen = draft.offer[1]
        draft = creator.chooseSignature(draft, 1)
        assertFalse(draft.isSignatureStep)
        assertEquals(chosen.name, draft.signature?.name)
        assertEquals(6, draft.offer.size)
        assertTrue(draft.offer.none { it.signature })

        val pair = keepable(draft)
        val names = pair.map { draft.offer[it].name }
        draft = creator.keep(draft, pair)
        assertTrue(draft.isComplete)
        assertTrue(draft.offer.isEmpty())
        assertEquals(listOf(chosen.name) + names, draft.kept.map { it.name })
    }

    @Test
    fun theFinishedDraftIsALevelOneFighterWithThreeTechniques() {
        val profile = draftAll(creator(), Channel.GREEN, signature = 2).toProfile()

        assertEquals("Iris", profile.name)
        assertEquals(Channel.GREEN, profile.aura.base)
        assertEquals(1, profile.level)
        assertEquals(CharacterDraft.STARTING_TECHNIQUES, profile.catalog.size)
        assertEquals(listOf(1, 2, 3), profile.catalog.map { it.id })
        assertEquals(listOf(1, 2, 3), profile.loadout)
        assertTrue(profile.catalog.first().signature)
        assertEquals(3, profile.toFighter().actions.filterNotNull().size)
    }

    @Test
    fun onlyExactlyTwoDifferentOffersCanBeKept() {
        val creator = creator()
        val start = creator.start("Iris", Channel.RED)
        assertFalse(start.canKeep(listOf(0, 1)), "the signature comes first")

        val draft = creator.chooseSignature(start, 0)
        assertFalse(draft.canKeep(listOf(0)))
        assertFalse(draft.canKeep(listOf(0, 0)))
        assertFalse(draft.canKeep(listOf(0, 1, 2)))
        assertFalse(draft.canKeep(listOf(0, 6)))
        assertFailsWith<IllegalArgumentException> { creator.keep(draft, listOf(0)) }
        assertFailsWith<IllegalStateException> { start.toProfile() }
        assertFailsWith<IllegalStateException> { draft.toProfile() }
    }

    @Test
    fun aFighterWithoutAnAttackIsNotAllowed() {
        for (seed in 1..80) {
            val creator = creator(seed)
            val start = creator.start("Iris", Channel.BLUE)
            val support = start.offer.indexOfFirst { it.kind != TechniqueKind.ATTACK }
            if (support < 0) continue
            val draft = creator.chooseSignature(start, support)
            assertTrue(draft.offer.any { it.kind == TechniqueKind.ATTACK }, "seed $seed")

            val nonAttacks = draft.offer.indices.filter { draft.offer[it].kind != TechniqueKind.ATTACK }
            if (nonAttacks.size >= 2) assertFalse(draft.canKeep(nonAttacks.take(2)), "seed $seed")
        }
    }

    @Test
    fun everySignatureOfferIsThePurePrimaryAtFullPowerAndBeatsNormalRolls() {
        for (seed in 1..40) for (base in Channel.entries) {
            for (technique in creator(seed).start("Iris", base).offer) {
                assertEquals(255, technique.color[base], "$technique")
                assertEquals(255, technique.color.power, "$technique")
                val rolls = technique.rolls
                val standouts = listOf(rolls.potency >= 1.3, rolls.windUp <= 0.7, rolls.cost <= 0.7)
                assertEquals(1, standouts.count { it }, "one standout stat: $rolls")
                assertTrue(rolls.potency >= 1.0 && rolls.windUp <= 1.0 && rolls.cost <= 1.0, "no unlucky stat: $rolls")
            }
        }
    }

    @Test
    fun theSignatureOfferHasDifferentStandouts() {
        val standouts = creator().start("Iris", Channel.BLUE).offer.map {
            when {
                it.rolls.potency >= 1.3 -> Standout.STRONGER
                it.rolls.windUp <= 0.7 -> Standout.FASTER
                else -> Standout.CHEAPER
            }
        }
        assertEquals(Standout.entries.toSet(), standouts.toSet())
    }

    @Test
    fun aFastSignatureIsActuallyFasterAndRedNeverGetsAUselessOne() {
        assertEquals(listOf(Standout.STRONGER, Standout.CHEAPER), TechniqueRoller.usefulStandouts(Channel.RED))

        val roller = TechniqueRoller(Random(5))
        for (base in listOf(Channel.GREEN, Channel.BLUE)) {
            val aura = CharacterDraft("Iris", base).aura
            val fast = roller.rollSignature(aura, 1, TechniqueKind.ATTACK, Standout.FASTER)
            val normalTicks = ColorTraits.of(fast.color).windUpTicks
            assertTrue(fast.toAction().duration < normalTicks, "$base: ${fast.toAction().duration} vs $normalTicks")
        }
    }

    @Test
    fun theSameSeedDraftsTheSameCharacter() {
        assertEquals(draftAll(creator(3), Channel.RED), draftAll(creator(3), Channel.RED))
    }

    @Test
    fun namesAreTrimmedAndCapped() {
        assertEquals("Iris Vale", CharacterDraft.cleanName("  Iris   Vale "))
        assertEquals(CharacterDraft.MAX_NAME_LENGTH, CharacterDraft.cleanName("x".repeat(40))!!.length)
        assertNull(CharacterDraft.cleanName("   "))
        assertFailsWith<IllegalArgumentException> { creator().start(" ", Channel.RED) }
    }

    @Test
    fun aSignatureSurvivesSaving() {
        val profile = draftAll(creator(), Channel.BLUE).toProfile()

        val reloaded = ProfileJson.decode(ProfileJson.encode(profile))

        assertEquals(profile, reloaded)
        assertTrue(reloaded.catalog.first().signature)
    }
}

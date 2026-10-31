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

    private fun draftAll(creator: CharacterCreator, base: Channel, choose: (CharacterDraft) -> Int = { 0 }): CharacterDraft {
        var draft = creator.start("Iris", base)
        while (!draft.isComplete) draft = creator.pick(draft, choose(draft))
        return draft
    }

    @Test
    fun threeRoundsOfThreeThenASignatureRound() {
        val creator = creator()
        var draft = creator.start("Iris", Channel.BLUE)

        repeat(3) { round ->
            assertEquals(round + 1, draft.round)
            assertEquals(3, draft.offer.size)
            assertFalse(draft.isSignatureRound)
            assertTrue(draft.offer.none { it.signature })
            draft = creator.pick(draft, 1)
        }

        assertTrue(draft.isSignatureRound)
        assertEquals(3, draft.offer.size)
        assertTrue(draft.offer.all { it.signature })
        draft = creator.pick(draft, 2)

        assertTrue(draft.isComplete)
        assertTrue(draft.offer.isEmpty())
        assertEquals(4, draft.picks.size)
    }

    @Test
    fun theFinishedDraftIsALevelOneFighterWithItsPicksInTheLoadout() {
        val draft = draftAll(creator(), Channel.GREEN) { it.round % 3 }
        val profile = draft.toProfile()

        assertEquals("Iris", profile.name)
        assertEquals(Channel.GREEN, profile.aura.base)
        assertEquals(1, profile.level)
        assertEquals(draft.picks, profile.catalog)
        assertEquals(listOf(1, 2, 3, 4), profile.loadout)
        assertEquals(4, profile.toFighter().actions.filterNotNull().size)
    }

    @Test
    fun anUnfinishedDraftIsNotAFighterYet() {
        val draft = creator().start("Iris", Channel.RED)

        assertFailsWith<IllegalStateException> { draft.toProfile() }
    }

    @Test
    fun everySignatureOfferIsThePurePrimaryAtFullPowerAndBeatsNormalRolls() {
        for (seed in 1..40) for (base in Channel.entries) {
            var draft = creator(seed).start("Iris", base)
            repeat(3) { draft = creator(seed).pick(draft, 0) }

            for (technique in draft.offer) {
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
    fun aSignatureRoundOffersDifferentStandouts() {
        var draft = creator().start("Iris", Channel.BLUE)
        repeat(3) { draft = creator().pick(draft, 0) }

        val standouts = draft.offer.map {
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
    fun everyOfferHasAnAttackAndEveryFighterCanAttack() {
        for (seed in 1..60) {
            // always take a non-attack when there is one, to push the draft toward no attacks
            val draft = draftAll(creator(seed), Channel.RED) { d ->
                d.offer.indexOfFirst { it.kind != TechniqueKind.ATTACK }.coerceAtLeast(0)
            }
            assertTrue(draft.picks.any { it.kind == TechniqueKind.ATTACK }, "seed $seed")
        }
        for (seed in 1..60) {
            var draft = creator(seed).start("Iris", Channel.GREEN)
            while (!draft.isComplete) {
                assertTrue(draft.offer.any { it.kind == TechniqueKind.ATTACK }, "seed $seed round ${draft.round}")
                draft = creator(seed).pick(draft, 0)
            }
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
        assertTrue(reloaded.catalog.last().signature)
    }
}

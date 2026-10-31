package com.caderea.battleapp.core.technique

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.ColorTraits
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.progression.Aura
import com.caderea.battleapp.core.progression.Levels
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TechniqueRollerTest {

    private val young = Aura.start(Channel.RED)
    private val veteran = Aura(Channel.RED, red = Levels.xpFor(30))

    private fun rolls(aura: Aura, count: Int = 500, seed: Int = 7): List<Technique> {
        val roller = TechniqueRoller(Random(seed))
        return List(count) { roller.roll(aura, it + 1) }
    }

    @Test
    fun theSameSeedRollsTheSameTechniques() {
        assertEquals(rolls(young, 20, seed = 3), rolls(young, 20, seed = 3))
    }

    @Test
    fun aYoungRedFighterMostlyRollsReds() {
        val reds = rolls(young).count { it.color.share(Channel.RED) > 0.7 }

        assertTrue(reds > 400, "$reds of 500 mostly red")
    }

    @Test
    fun rollsStayWithinTheBudgetAndTheChannelRange() {
        for (aura in listOf(young, veteran)) {
            for (technique in rolls(aura)) {
                assertTrue(technique.color.power <= aura.powerBudget, "$technique over ${aura.powerBudget}")
                assertTrue(technique.color.power > 0)
            }
        }
    }

    @Test
    fun strongerFightersRollWithMoreVariance() {
        fun spreadOf(list: List<Technique>, stat: (Technique) -> Double): Double {
            val values = list.map(stat)
            val mean = values.average()
            return values.sumOf { abs(it - mean) } / values.size
        }

        val youngRolls = rolls(young)
        val veteranRolls = rolls(veteran)

        assertTrue(spreadOf(veteranRolls) { it.color.share(Channel.RED) } > spreadOf(youngRolls) { it.color.share(Channel.RED) })
        assertTrue(spreadOf(veteranRolls) { it.rolls.potency } > spreadOf(youngRolls) { it.rolls.potency })
        assertTrue(spreadOf(veteranRolls) { it.color.power.toDouble() } > spreadOf(youngRolls) { it.color.power.toDouble() })
    }

    @Test
    fun statRollsStayWithinTheLevelsSpread() {
        val spread = TechniqueRoller.statSpread(young.level)
        for (technique in rolls(young)) {
            with(technique.rolls) {
                assertTrue(listOf(windUp, potency, cost).all { abs(it - 1) <= spread + 1e-9 }, "$this")
            }
        }
    }

    @Test
    fun attacksHealsAndGuardsAreAllRolled() {
        val kinds = rolls(young).groupingBy { it.kind }.eachCount()

        assertEquals(TechniqueKind.entries.toSet(), kinds.keys)
        assertTrue(kinds.getValue(TechniqueKind.ATTACK) > kinds.getValue(TechniqueKind.HEAL))
        // a kind can be asked for, and is rolled the same way
        val guard = TechniqueRoller(Random(1)).roll(young, 1, TechniqueKind.GUARD)
        assertEquals(TechniqueKind.GUARD, guard.kind)
        assertTrue(guard.toAction() is Guard)
    }

    @Test
    fun rollsCarryIntoTheBattleAction() {
        val technique = Technique(
            1, TechniqueKind.ATTACK, "Blue Jab", RgbColor.BLUE,
            TechniqueRolls(windUp = 0.8, potency = 1.1, cost = 0.9),
        )
        val action = technique.toAction() as Attack
        val plain = Attack(color = technique.color)

        // a fast blue: 2 x 1.3 x 0.8 rounds to 2 ticks instead of 3
        assertEquals(2, action.duration)
        assertEquals(3, plain.duration)
        assertEquals(9, action.cost)
        assertEquals(ColorTraits.of(technique.color).windUpTicks(0.8), action.duration)
        assertTrue(technique.copy(kind = TechniqueKind.HEAL).toAction() is Heal)
    }

    @Test
    fun namesSayTheHueAndWhatItDoes() {
        assertEquals("Red Jab", TechniqueNames.name(TechniqueKind.ATTACK, RgbColor.RED, TechniqueRolls.NEUTRAL))
        assertEquals("Blue Blast", TechniqueNames.name(TechniqueKind.ATTACK, RgbColor.BLUE, TechniqueRolls.NEUTRAL))
        assertEquals("Green Mend", TechniqueNames.name(TechniqueKind.HEAL, RgbColor.GREEN, TechniqueRolls.NEUTRAL))
        assertEquals("Magenta Guard", TechniqueNames.name(TechniqueKind.GUARD, RgbColor(200, 0, 200), TechniqueRolls.NEUTRAL))
        assertEquals("Grey Guard", TechniqueNames.name(TechniqueKind.GUARD, RgbColor(80, 80, 80), TechniqueRolls.NEUTRAL))
    }
}

package com.caderea.battleapp.server.match

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.EndReason
import com.caderea.battleapp.core.online.LandedAction
import com.caderea.battleapp.core.online.MatchResult
import com.caderea.battleapp.core.online.Side
import com.caderea.battleapp.core.progression.Levels
import com.caderea.battleapp.core.technique.Technique
import com.caderea.battleapp.core.technique.TechniqueKind
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Drives a match one tick at a time, with no clock or network. */
class MatchEngineTest {

    // red's starter kit: Red Strike, Green Mend, Green Strike, Blue Guard
    private val red = FighterProfile.create("Red", Channel.RED)
    private val blue = FighterProfile.create("Blue", Channel.BLUE)

    private fun engine(seed: Int = 1, maxTicks: Int = MatchEngine.MAX_TICKS) =
        MatchEngine("m", Arenas.OPEN, listOf(red, blue), Random(seed), maxTicks)

    @Test
    fun eachPlayerSeesThemselvesAsYou() {
        // whichever seat the coin flip makes the battle's player
        for (seed in SEEDS) {
            val engine = engine(seed)
            assertEquals("Red", engine.started(0, 1000).you.name)
            assertEquals("Blue", engine.started(0, 1000).opponent.name)
            assertEquals("Blue", engine.started(1, 1000).you.name)

            engine.queue(0, RED_STRIKE)
            engine.tick()
            assertTrue(engine.state(1).you.health < 2000)
            assertEquals(2000, engine.state(0).you.health)
        }
    }

    @Test
    fun theOpponentSeesWhatYouQueue() {
        val engine = engine()
        assertTrue(engine.queue(0, GREEN_STRIKE))

        val seen = engine.state(1).opponent.queue.single()
        assertEquals(GREEN_STRIKE, seen.slot)
        // green winds up for 2 ticks
        assertEquals(2, seen.ticksLeft)
        assertTrue(engine.state(1).you.queue.isEmpty())
    }

    @Test
    fun queuingSpendsEnergyAndCancelingRefundsSome() {
        val engine = engine()
        val before = engine.state(0).you.energy

        engine.queue(0, RED_STRIKE)
        val queued = engine.state(0).you.energy
        assertTrue(queued < before)

        assertTrue(engine.cancel(0))
        assertTrue(engine.state(0).you.energy in (queued + 1) until before)
        assertFalse(engine.cancel(0))
    }

    @Test
    fun badSlotsAreRefused() {
        val engine = engine()
        assertFalse(engine.queue(0, -1))
        assertFalse(engine.queue(0, 9))
    }

    @Test
    fun whatLandedIsToldFromEachSide() {
        val engine = engine()
        engine.queue(0, RED_STRIKE)
        engine.tick()

        assertEquals(listOf(LandedAction(Side.YOU, RED_STRIKE)), engine.state(0, withLanded = true).landed)
        assertEquals(listOf(LandedAction(Side.OPPONENT, RED_STRIKE)), engine.state(1, withLanded = true).landed)
        assertTrue(engine.state(0).landed.isEmpty())
    }

    @Test
    fun guardsShowWhileTheyAreUp() {
        val engine = engine()
        engine.queue(1, GREEN_GUARD)
        // green winds up for 2 ticks
        repeat(2) { engine.tick() }

        val guard = engine.state(0).opponent.guard
        assertEquals(GREEN_GUARD, guard?.slot)
        assertTrue(guard!!.strength > 0)
    }

    @Test
    fun aKnockoutEndsTheMatch() {
        for (seed in SEEDS) {
            val engine = engine(seed)
            while (!engine.isOver && engine.tickCount < TICK_LIMIT) {
                engine.queue(0, RED_STRIKE)
                engine.tick()
            }

            assertEquals(MatchEngine.Outcome(0, EndReason.KNOCKOUT), engine.outcome)
            assertEquals(MatchResult.WON, engine.result(0).result)
            assertEquals(MatchResult.LOST, engine.result(1).result)
            assertEquals(0, engine.state(1).you.health)
        }
    }

    @Test
    fun givingUpHandsTheOtherPlayerTheWin() {
        val engine = engine()
        engine.concede(1, EndReason.FORFEIT)

        assertEquals(MatchResult.WON, engine.result(0).result)
        assertEquals(EndReason.FORFEIT, engine.result(1).reason)
        assertFalse(engine.queue(0, RED_STRIKE))

        // the first ending stands
        engine.concede(0, EndReason.DISCONNECT)
        assertEquals(MatchResult.WON, engine.result(0).result)
    }

    @Test
    fun runningOutOfTicksIsADraw() {
        val engine = engine(maxTicks = 3)
        repeat(5) { engine.tick() }

        assertEquals(3, engine.tickCount)
        assertNull(engine.outcome!!.winner)
        assertEquals(MatchResult.DRAW, engine.result(0).result)
        assertEquals(EndReason.TIME, engine.result(1).reason)
    }

    @Test
    fun techniquesAreDimmedToTheFightersLevel() {
        // a hand-edited profile can't bring a technique brighter than its level allows
        val white = Technique(99, TechniqueKind.ATTACK, "Too Bright", RgbColor(255, 255, 255))
        val cheat = red.learn(white).withLoadout(listOf(99))

        val brought = MatchEngine.infoFor(cheat, Arenas.OPEN).techniques.first()!!
        assertEquals(1, cheat.level)
        assertTrue(brought.color.power <= Levels.powerBudget(1))
    }

    @Test
    fun theArenaSetsTheSlots() {
        val info = MatchEngine.infoFor(red, Arenas.DIM)
        assertEquals(Arenas.DIM.loadoutSize, info.techniques.size)
        assertEquals(minOf(red.catalog.size, 6), info.techniques.count { it != null })

        val small = MatchEngine.infoFor(red, Arenas.OPEN)
        assertEquals(red.loadoutFor(4).map { it.name }, small.techniques.map { it!!.name })
    }

    private companion object {
        const val RED_STRIKE = 0
        const val GREEN_STRIKE = 2
        // blue's kit: Blue Strike, Green Mend, Red Strike, Green Guard
        const val GREEN_GUARD = 3
        val SEEDS = 1..4
        const val TICK_LIMIT = 500
    }
}

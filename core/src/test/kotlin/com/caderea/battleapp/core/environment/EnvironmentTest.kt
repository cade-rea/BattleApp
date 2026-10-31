package com.caderea.battleapp.core.environment

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EnvironmentTest {

    private val red = Light.tinted(Channel.RED)
    private val green = Light.tinted(Channel.GREEN)
    private val blue = Light.tinted(Channel.BLUE)
    private val turning = Environment(lights = listOf(red, green, blue), ticksPerLight = 4)

    @Test
    fun aSteadyLightNeverTurns() {
        val glow = Environment(lights = listOf(red))

        assertFalse(glow.isTurning)
        assertEquals(red, glow.lightAt(1))
        assertEquals(red, glow.lightAt(99))
        assertNull(glow.ticksLeftInLight(5))
        assertNull(glow.nextLight(5))
        assertSame(glow, glow.at(7))
        assertEquals(1.1, glow.lightMultiplier(RgbColor.RED), 1e-9)
    }

    @Test
    fun aTurningLightCyclesEveryFewTicks() {
        // ticks count from 1, and before the first tick it's already the first light
        assertEquals(red, turning.lightAt(0))
        assertEquals(red, turning.lightAt(1))
        assertEquals(red, turning.lightAt(4))
        assertEquals(green, turning.lightAt(5))
        assertEquals(blue, turning.lightAt(12))
        assertEquals(red, turning.lightAt(13))
    }

    @Test
    fun aTurningLightSaysWhatComesNext() {
        assertEquals(4, turning.ticksLeftInLight(1))
        assertEquals(1, turning.ticksLeftInLight(4))
        assertEquals(4, turning.ticksLeftInLight(5))
        assertEquals(green, turning.nextLight(3))
        assertEquals(red, turning.nextLight(12))
    }

    @Test
    fun aSnapshotFixesTheLightAtOneTick() {
        val atFive = turning.at(5)

        assertFalse(atFive.isTurning)
        assertEquals(1.1, atFive.lightMultiplier(RgbColor.GREEN), 1e-9)
        assertEquals(1.0, atFive.lightMultiplier(RgbColor.RED), 1e-9)
        assertEquals(turning.loadoutSize, atFive.loadoutSize)
    }

    @Test
    fun rejectsBadArenas() {
        assertFailsWith<IllegalArgumentException> { Environment(loadoutSize = 7) }
        assertFailsWith<IllegalArgumentException> { Environment(lights = emptyList()) }
        assertFailsWith<IllegalArgumentException> { Environment(lights = listOf(red, green)) }
    }

    @Test
    fun everyArenaIsValidAndFindable() {
        assertEquals(Arenas.ALL.size, Arenas.ALL.map { it.id }.toSet().size)
        Arenas.ALL.forEach { arena ->
            assertSame(arena, Arenas.byId(arena.id))
            assertTrue(arena.loadoutSize in Environment.MIN_LOADOUT..Environment.MAX_LOADOUT)
        }
        assertSame(Arenas.DEFAULT, Arenas.byId("gone"))
        assertSame(Arenas.DEFAULT, Arenas.byId(null))
    }

    @Test
    fun arenasEachChangeSomething() {
        assertEquals(1.1, Arenas.RED_GLOW.lightMultiplier(RgbColor.RED), 1e-9)
        assertEquals(0.7, Arenas.DIM.lightMultiplier(RgbColor.GREEN), 1e-9)
        assertEquals(0.5, Arenas.RED_VEIL.lightMultiplier(RgbColor.RED), 1e-9)
        assertEquals(1, Arenas.EVEN.levelCap)
        assertEquals(1.2, Arenas.TURNING.at(Arenas.TURN_TICKS + 1).lightMultiplier(RgbColor.GREEN), 1e-9)
    }
}

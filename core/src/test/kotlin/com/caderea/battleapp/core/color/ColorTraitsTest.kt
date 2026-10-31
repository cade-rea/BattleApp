package com.caderea.battleapp.core.color

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorTraitsTest {

    private val red = ColorTraits.of(RgbColor.RED)
    private val green = ColorTraits.of(RgbColor.GREEN)
    private val blue = ColorTraits.of(RgbColor.BLUE)

    @Test
    fun redIsFastButWeaker() {
        assertTrue(red.windUpTicks < green.windUpTicks)
        assertTrue(red.strength < green.strength)
        assertEquals(1, red.windUpTicks)
    }

    @Test
    fun blueIsStrongButSlow() {
        assertTrue(blue.windUpTicks > green.windUpTicks)
        assertTrue(blue.strength > green.strength)
        assertEquals(3, blue.windUpTicks)
    }

    @Test
    fun greenIsEfficient() {
        assertTrue(green.costFactor < red.costFactor)
        assertTrue(green.costFactor < blue.costFactor)
        assertTrue(green.support > red.support)
        assertTrue(green.support > blue.support)
        assertEquals(ColorTraits.BASE_WIND_UP_TICKS, green.windUpTicks)
    }

    @Test
    fun mixesBlendTheTraits() {
        val magenta = ColorTraits.of(RgbColor.fromHex("#800080"))

        // red's speed and blue's slowness cancel out
        assertEquals(1.0, magenta.windUpFactor, DELTA)
        assertEquals(1.0, magenta.strength, DELTA)
    }

    @Test
    fun brightnessDoesNotChangeTraits() {
        assertEquals(red, ColorTraits.of(RgbColor.fromHex("#300000")))
    }

    @Test
    fun blackIsNeutral() {
        assertEquals(ColorTraits(1.0, 1.0, 1.0, 1.0), ColorTraits.of(RgbColor.BLACK))
    }

    private companion object {
        const val DELTA = 1e-9
    }
}

package com.caderea.battleapp.core.color

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AffinityTest {

    @Test
    fun matchingHueGetsTheFullBonus() {
        assertEquals(1.25, Affinity.multiplier(RgbColor.RED, RgbColor.RED), DELTA)
        // brightness doesn't matter, only hue
        assertEquals(1.25, Affinity.multiplier(RgbColor.fromHex("#400000"), RgbColor.RED), DELTA)
    }

    @Test
    fun oppositeHueGetsNoBonus() {
        val cyan = RgbColor.fromHex("#00FFFF")

        assertEquals(1.0, Affinity.multiplier(cyan, RgbColor.RED), DELTA)
    }

    @Test
    fun otherPrimariesGetAThirdOfTheBonus() {
        val third = 1 + Affinity.MAX_BONUS / 3

        assertEquals(third, Affinity.multiplier(RgbColor.GREEN, RgbColor.RED), DELTA)
        assertEquals(third, Affinity.multiplier(RgbColor.BLUE, RgbColor.RED), DELTA)
    }

    @Test
    fun closerMixesGetMoreBonus() {
        val red = RgbColor.RED
        val magenta = RgbColor.fromHex("#800080")
        val blue = RgbColor.BLUE

        val magentaBonus = Affinity.multiplier(magenta, red)
        assertTrue(magentaBonus < Affinity.multiplier(red, red))
        assertTrue(magentaBonus > Affinity.multiplier(blue, red))
    }

    @Test
    fun colorsWithNoHueGetNoBonus() {
        val grey = RgbColor.fromHex("#555555")

        assertEquals(1.0, Affinity.multiplier(grey, RgbColor.RED), DELTA)
        assertEquals(1.0, Affinity.multiplier(RgbColor.RED, grey), DELTA)
    }

    private companion object {
        const val DELTA = 1e-9
    }
}

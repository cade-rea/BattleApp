package com.caderea.battleapp.core.color

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RgbColorTest {

    @Test
    fun powerIsTheSumOfTheChannels() {
        assertEquals(255, RgbColor.RED.power)
        assertEquals(128, RgbColor.fromHex("#800000").power)
        assertEquals(256, RgbColor.fromHex("#800080").power)
        assertEquals(255, RgbColor.fromHex("#555555").power)
    }

    @Test
    fun sharesAddUpToOne() {
        val color = RgbColor(30, 60, 90)

        assertEquals(1.0, Channel.entries.sumOf(color::share), DELTA)
        assertEquals(0.5, color.share(Channel.BLUE), DELTA)
    }

    @Test
    fun blackHasNoShares() {
        Channel.entries.forEach { assertEquals(0.0, RgbColor.BLACK.share(it)) }
    }

    @Test
    fun channelsOutsideTheByteRangeAreRejected() {
        assertFailsWith<IllegalArgumentException> { RgbColor(256, 0, 0) }
        assertFailsWith<IllegalArgumentException> { RgbColor(0, -1, 0) }
    }

    @Test
    fun hexRoundTrips() {
        val color = RgbColor.fromHex("#1A2b3C")

        assertEquals(RgbColor(0x1A, 0x2B, 0x3C), color)
        assertEquals("#1A2B3C", color.hex)
        assertEquals(color, RgbColor.fromHex("1A2B3C"))
    }

    @Test
    fun argbIsOpaque() {
        assertEquals(0xFFFF0000.toInt(), RgbColor.RED.argb)
        assertEquals(0xFF1A2B3C.toInt(), RgbColor(0x1A, 0x2B, 0x3C).argb)
    }

    @Test
    fun primariesSitAThirdOfTheWheelApart() {
        assertEquals(0.0, RgbColor.RED.hue!!, DELTA)
        assertEquals(120.0, RgbColor.GREEN.hue!!, DELTA)
        assertEquals(240.0, RgbColor.BLUE.hue!!, DELTA)
        assertEquals(300.0, RgbColor.fromHex("#800080").hue!!, DELTA)
    }

    @Test
    fun greysHaveNoHue() {
        assertNull(RgbColor.BLACK.hue)
        assertNull(RgbColor.fromHex("#555555").hue)
        assertNull(RgbColor(255, 255, 255).hue)
    }

    @Test
    fun hueDistanceWrapsAroundTheWheel() {
        assertEquals(20.0, RgbColor.hueDistance(350.0, 10.0), DELTA)
        assertEquals(180.0, RgbColor.hueDistance(0.0, 180.0), DELTA)
        assertEquals(120.0, RgbColor.hueDistance(240.0, 0.0), DELTA)
    }

    @Test
    fun colorWithinBudgetIsUnchanged() {
        val color = RgbColor(100, 100, 55)

        assertSame(color, color.fitToBudget(RgbColor.STARTING_POWER_BUDGET))
    }

    @Test
    fun colorOverBudgetIsScaledDownKeepingItsMix() {
        val white = RgbColor(255, 255, 255)

        val fitted = white.fitToBudget(RgbColor.STARTING_POWER_BUDGET)

        assertEquals(RgbColor(85, 85, 85), fitted)

        val mixed = RgbColor(200, 0, 100).fitToBudget(150)
        assertTrue(mixed.power <= 150)
        assertEquals(RgbColor(100, 0, 50), mixed)
    }

    private companion object {
        const val DELTA = 1e-9
    }
}

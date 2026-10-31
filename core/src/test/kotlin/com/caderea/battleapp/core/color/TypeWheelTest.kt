package com.caderea.battleapp.core.color

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypeWheelTest {

    private val red = RgbColor.RED
    private val green = RgbColor.GREEN
    private val blue = RgbColor.BLUE
    private val magenta = RgbColor.fromHex("#800080")
    private val yellow = RgbColor.fromHex("#808000")
    private val grey = RgbColor.fromHex("#555555")

    @Test
    fun eachPrimaryBeatsTheNext() {
        assertEquals(Channel.GREEN, Channel.RED.prey)
        assertEquals(Channel.BLUE, Channel.GREEN.prey)
        assertEquals(Channel.RED, Channel.BLUE.prey)

        Channel.entries.forEach { assertEquals(it, it.prey.predator) }
    }

    // the examples from the design plan, with K = 0.5
    @Test
    fun pureRedAgainstPrimaries() {
        assertEquals(382.5, TypeWheel.typedPower(red, green), DELTA)
        assertEquals(255.0, TypeWheel.typedPower(red, red), DELTA)
        assertEquals(127.5, TypeWheel.typedPower(red, blue), DELTA)
    }

    @Test
    fun magentaIsNeutralAgainstGreen() {
        // red half is strong (192), blue half is weak (64)
        assertEquals(256.0, TypeWheel.typedPower(magenta, green), DELTA)
    }

    @Test
    fun magentaHasASlightEdgeOnYellow() {
        assertEquals(288.0, TypeWheel.typedPower(magenta, yellow), DELTA)
    }

    @Test
    fun pureColorsSwingBetweenHalfAndOneAndAHalf() {
        for (attack in listOf(red, green, blue)) {
            for (defender in listOf(red, green, blue)) {
                val effectiveness = TypeWheel.effectiveness(attack, defender)
                assertTrue(effectiveness in 0.5..1.5, "$attack vs $defender: $effectiveness")
            }
        }
        assertEquals(1.5, TypeWheel.effectiveness(green, blue), DELTA)
        assertEquals(1.5, TypeWheel.effectiveness(blue, red), DELTA)
        assertEquals(0.5, TypeWheel.effectiveness(green, red), DELTA)
    }

    @Test
    fun greyAndBlackDefendersAreNeutral() {
        for (attack in listOf(red, green, blue, magenta)) {
            assertEquals(1.0, TypeWheel.effectiveness(attack, grey), DELTA)
            assertEquals(1.0, TypeWheel.effectiveness(attack, RgbColor.BLACK), DELTA)
        }
    }

    @Test
    fun greyAttackIsNeutralAgainstAnything() {
        for (defender in listOf(red, green, blue, magenta, yellow)) {
            assertEquals(1.0, TypeWheel.effectiveness(grey, defender), DELTA)
        }
    }

    @Test
    fun colorsThatLookAlikePlayAlike() {
        val nearRed = RgbColor.fromHex("#F00800")

        for (defender in listOf(red, green, blue, magenta, yellow)) {
            val difference = TypeWheel.effectiveness(red, defender) - TypeWheel.effectiveness(nearRed, defender)
            assertTrue(kotlin.math.abs(difference) < 0.05, "against $defender: $difference")
        }
    }

    @Test
    fun blackAttackDoesNothing() {
        assertEquals(0.0, TypeWheel.typedPower(RgbColor.BLACK, green), DELTA)
    }

    private companion object {
        const val DELTA = 1e-9
    }
}

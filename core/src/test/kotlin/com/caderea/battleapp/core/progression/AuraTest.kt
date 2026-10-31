package com.caderea.battleapp.core.progression

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuraTest {

    @Test
    fun aNewAuraIsItsBasePrimary() {
        val aura = Aura.start(Channel.RED)

        assertEquals(Levels.STARTING_XP, aura.red)
        assertEquals(0, aura.green + aura.blue)
        assertEquals(1, aura.level)
        assertEquals(0.0, aura.color.hue)
    }

    @Test
    fun xpSplitsAThirdToTheBaseAndTheRestByTheTechniquesMix() {
        // the design plan's example: 100 XP from techniques that are 30% red and 70% blue
        val aura = Aura.start(Channel.RED).gain(100, RgbColor(77, 0, 179))

        assertEquals(60 + 33 + 20, aura.red)
        assertEquals(47, aura.blue)
        assertEquals(0, aura.green)
    }

    @Test
    fun ownColorTechniquesEarnTheFullAmountInTheBase() {
        val aura = Aura.start(Channel.GREEN).gain(10, RgbColor.GREEN)

        assertEquals(70, aura.green)
    }

    @Test
    fun noXpIsLostToRounding() {
        var aura = Aura.start(Channel.BLUE)
        repeat(50) { aura = aura.gain(7, RgbColor(91, 37, 128)) }

        assertEquals(Levels.STARTING_XP + 50 * 7, aura.totalXp)
    }

    @Test
    fun manySmallGainsAtOnceKeepTheExactSplit() {
        // 5 XP alone would round its 33% base share up to 2; twenty at once round once
        val aura = Aura.start(Channel.RED).gain(List(20) { 5 to RgbColor.BLUE })

        assertEquals(60 + 33, aura.red)
        assertEquals(67, aura.blue)
    }

    @Test
    fun blackTechniquesFeedOnlyTheBase() {
        val aura = Aura.start(Channel.RED).gain(10, RgbColor.BLACK)

        assertEquals(70, aura.red)
    }

    @Test
    fun usingOnlyAnotherColorDriftsTowardAThirdOfTheBaseButNeverBelow() {
        var aura = Aura.start(Channel.RED)
        repeat(30) { aura = aura.gain(1000, RgbColor.BLUE) }

        val redShare = aura.share(Channel.RED)
        assertTrue(redShare > Aura.BASE_SHARE, "red share $redShare")
        assertTrue(redShare < Aura.BASE_SHARE + 0.01, "red share $redShare")
        // still red-based but mostly blue: a violet
        assertTrue(aura.color.blue > aura.color.red)
    }

    @Test
    fun theAurasHueFollowsItsMixAndItsBrightnessItsLevel() {
        val young = Aura(Channel.RED, red = 60)
        val tinted = Aura(Channel.RED, red = 113, blue = 47)
        val veteran = Aura(Channel.RED, red = Levels.xpFor(Aura.FULL_BRIGHTNESS_LEVEL))

        assertEquals(RgbColor(153, 0, 0), young.color)
        // a red with a visible violet tint
        assertEquals(47.0 / 113, tinted.color.blue.toDouble() / tinted.color.red, 0.01)
        assertEquals(RgbColor.RED, veteran.color)
    }
}

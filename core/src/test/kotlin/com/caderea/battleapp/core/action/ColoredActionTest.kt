package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * How a technique's color, the fighters' auras and the arena combine into damage and healing.
 */
class ColoredActionTest {

    private val environment = Environment()

    private val redFighter = fighter(RgbColor.RED)
    private val greenFighter = fighter(RgbColor.GREEN)
    private val blueFighter = fighter(RgbColor.BLUE)

    private val redStrike = Attack(color = RgbColor.RED)
    private val greenStrike = Attack(color = RgbColor.GREEN)
    private val blueStrike = Attack(color = RgbColor.BLUE)

    @Test
    fun defenderAuraDecidesTheMatchup() {
        // a magenta attacker using red: 255 x matchup x red strength 0.8 x affinity 2/3 of 25%
        val magentaFighter = fighter(RgbColor.fromHex("#800080"))

        assertEquals(357, redStrike.damage(magentaFighter, greenFighter, environment)) // 1.5x
        assertEquals(238, redStrike.damage(magentaFighter, redFighter, environment)) // 1.0x
        assertEquals(119, redStrike.damage(magentaFighter, blueFighter, environment)) // 0.5x
    }

    @Test
    fun matchingTheAttackersAuraAddsAffinity() {
        val ownColor = redStrike.damage(redFighter, redFighter, environment)
        val offColor = redStrike.damage(blueFighter, redFighter, environment)

        assertEquals(255, ownColor) // 255 x 0.8 strength x 1.25 affinity
        assertTrue(ownColor > offColor)
    }

    @Test
    fun blueHitsHarderThanRedAtTheSamePower() {
        // a grey defender and a grey attacker take matchups and affinity out of it
        val grey = fighter(RgbColor.fromHex("#555555"))

        assertEquals(204, redStrike.damage(grey, grey, environment))
        assertEquals(255, greenStrike.damage(grey, grey, environment))
        assertEquals(306, blueStrike.damage(grey, grey, environment))
    }

    @Test
    fun attackLowersTheTargetsHealth() {
        val log = greenStrike.performAction(redFighter, blueFighter, environment)

        // 382.5 x green strength 1.0 x affinity 1/3 of 25%
        assertEquals(2000 - 414, blueFighter.health)
        assertEquals("Fighter attacks Fighter with Attack for 414 damage.", log)
    }

    @Test
    fun healAmountComesFromItsColor() {
        val greenMend = Heal(color = RgbColor.GREEN)
        val redMend = Heal(color = RgbColor.RED)

        // 255 x 0.5 x green support 1.3 x full affinity
        assertEquals(207, greenMend.amount(greenFighter, environment))
        // red has no support bonus
        assertEquals(159, redMend.amount(redFighter, environment))
        // a dimmer heal restores less
        assertTrue(Heal(color = RgbColor.fromHex("#008000")).amount(greenFighter, environment) < 207)
    }

    @Test
    fun healRestoresThePerformersHealth() {
        greenFighter.health = 500

        Heal(color = RgbColor.GREEN).performAction(greenFighter, redFighter, environment)

        assertEquals(707, greenFighter.health)
        assertEquals(2000, redFighter.health)
    }

    @Test
    fun arenaLightMultipliesDamageAndHealing() {
        val redLight = object : Environment() {
            override fun lightMultiplier(color: RgbColor) = if (color == RgbColor.RED) 2.0 else 1.0
        }

        assertEquals(510, redStrike.damage(redFighter, redFighter, redLight))
        assertEquals(
            greenStrike.damage(redFighter, blueFighter, environment),
            greenStrike.damage(redFighter, blueFighter, redLight),
        )
        assertEquals(319, Heal(color = RgbColor.RED).amount(redFighter, redLight))
    }

    @Test
    fun durationIsTheColorsWindUp() {
        assertEquals(1, redStrike.duration)
        assertEquals(2, greenStrike.duration)
        assertEquals(3, blueStrike.duration)
    }

    private fun fighter(color: RgbColor) = Fighter(name = "Fighter", color = color, actions = emptyList())
}

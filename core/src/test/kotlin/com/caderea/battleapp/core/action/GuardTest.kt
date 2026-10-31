package com.caderea.battleapp.core.action

import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * How much a guard blocks, by its color against the attack's.
 */
class GuardTest {

    private val environment = Environment()

    @Test
    fun guardBlocksAttacksOfItsOwnColor() {
        val guard = raise(Guard(color = RgbColor.GREEN), by = RgbColor.GREEN)

        // 0.5 x green support 1.3 x full affinity
        assertEquals(81, guard.blocked(100, RgbColor.GREEN))
    }

    @Test
    fun guardInAnotherPrimaryBlocksNothing() {
        val guard = raise(Guard(color = RgbColor.GREEN), by = RgbColor.GREEN)

        assertEquals(0, guard.blocked(100, RgbColor.RED))
        assertEquals(0, guard.blocked(100, RgbColor.BLUE))
    }

    @Test
    fun mixedColorsBlockByHowMuchTheyOverlap() {
        val guard = raise(Guard(color = RgbColor.BLUE), by = RgbColor.fromHex("#555555"))
        val magenta = RgbColor.fromHex("#800080")

        // half of magenta is blue, so a blue guard blocks half as much of it
        assertEquals(50, guard.blocked(100, RgbColor.BLUE))
        assertEquals(25, guard.blocked(100, magenta))
    }

    @Test
    fun dimGuardBlocksLessOfABrightAttack() {
        val bright = raise(Guard(color = RgbColor.GREEN), by = RgbColor.GREEN)
        val dim = raise(Guard(color = RgbColor.fromHex("#008000")), by = RgbColor.GREEN)

        assertTrue(dim.blocked(100, RgbColor.GREEN) < bright.blocked(100, RgbColor.GREEN))
        // against an attack no brighter than itself it blocks in full
        assertEquals(81, dim.blocked(100, RgbColor.fromHex("#008000")))
    }

    @Test
    fun noGuardBlocksEverything() {
        val bright = object : Environment() {
            override fun lightMultiplier(color: RgbColor) = 3.0
        }
        val guard = ActiveGuard(Guard(color = RgbColor.GREEN), Guard(color = RgbColor.GREEN).strength(fighter(RgbColor.GREEN), bright))

        assertEquals(90, guard.blocked(100, RgbColor.GREEN))
    }

    @Test
    fun guardActionRaisesTheGuardOnThePerformer() {
        val performer = fighter(RgbColor.RED)
        val log = Guard("Blue Guard", RgbColor.BLUE).performAction(performer, fighter(RgbColor.BLUE), environment)

        assertEquals("Fighter raises Blue Guard.", log)
        assertEquals("Blue Guard", performer.activeGuard?.guard?.name)
    }

    @Test
    fun attackIntoAGuardDealsWhatGetsThrough() {
        val defender = fighter(RgbColor.GREEN)
        Guard("Red Guard", RgbColor.RED).performAction(defender, defender, environment)

        val log = Attack("Red Strike", RgbColor.RED).performAction(fighter(RgbColor.RED), defender, environment)

        // 383 damage, 0.5 x a green fighter's affinity for red (1/3 of 25%) of it blocked
        assertEquals("Fighter attacks Fighter with Red Strike for 176 damage (207 blocked by Red Guard).", log)
        assertEquals(defender.maxHealth - 176, defender.health)
    }

    private fun raise(guard: Guard, by: RgbColor) = ActiveGuard(guard, guard.strength(fighter(by), environment))

    private fun fighter(color: RgbColor) = Fighter(name = "Fighter", color = color, actions = emptyList())
}

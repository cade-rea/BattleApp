package com.caderea.battleapp.core

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Drives the battle one tick at a time, with no clock.
 */
class BattleTest {

    private lateinit var battle: Battle
    private val log = mutableListOf<String>()

    @BeforeTest
    fun setUp() {
        battle = Battle()
        log.clear()
    }

    private fun tick() {
        log += battle.tick()
    }

    @Test
    fun queuedAttackDamagesTheEnemy() {
        battle.queueAction(GREEN_STRIKE)
        tick()

        assertEquals(1000 - GREEN_STRIKE_DAMAGE, battle.enemy.health)
        assertTrue("Default Fighter attacks Enemy with Green Strike for $GREEN_STRIKE_DAMAGE damage." in log)
    }

    @Test
    fun ownColorIsWeakAgainstItsPredator() {
        battle.queueAction(RED_STRIKE)
        tick()

        // red into a blue enemy is a bad matchup, even with the red player's affinity bonus
        assertTrue(1000 - battle.enemy.health < GREEN_STRIKE_DAMAGE / 2)
    }

    @Test
    fun healingCannotGoAboveMaxHealth() {
        battle.queueAction(GREEN_MEND)
        tick()

        assertEquals(battle.player.maxHealth, battle.player.health)
    }

    @Test
    fun healingRestoresLostHealth() {
        battle.player.health = 500

        battle.queueAction(GREEN_MEND)
        tick()

        // 255 x 0.5 x green support 1.3 x a red fighter's affinity for green
        assertEquals(680, battle.player.health)
    }

    @Test
    fun emptyAndMissingButtonsQueueNothing() {
        assertFalse(battle.queueAction(3))
        assertFalse(battle.queueAction(-1))
        assertFalse(battle.queueAction(7))

        assertTrue(battle.player.queue.isEmpty())
    }

    @Test
    fun enemyAttacksEveryOtherTick() {
        repeat(4) { tick() }

        // the enemy queues an attack on ticks 2 and 4 and performs it on the following tick
        assertEquals(1000 - ENEMY_STRIKE_DAMAGE, battle.player.health)
        tick()
        assertEquals(0, battle.player.health)
    }

    @Test
    fun idlePlayerLosesAFullFight() {
        var ticks = 0
        while (!battle.isOver && ticks < TICK_LIMIT) {
            tick()
            ++ticks
        }

        assertTrue(battle.isOver)
        assertTrue(ticks < TICK_LIMIT)
        assertEquals(0, battle.player.health)
        assertEquals("Default Fighter has died.", log.last())
    }

    @Test
    fun playerUsingTheRightColorWinsAFullFight() {
        fightWith(GREEN_STRIKE)

        assertEquals(0, battle.enemy.health)
        assertTrue(battle.player.health > 0)
        assertEquals("Enemy has died.", log.last())
    }

    @Test
    fun playerUsingTheWrongColorLosesAFullFight() {
        fightWith(RED_STRIKE)

        assertEquals(0, battle.player.health)
        assertTrue(battle.enemy.health > 0)
        assertEquals("Default Fighter has died.", log.last())
    }

    private fun fightWith(slot: Int) {
        var ticks = 0
        while (!battle.isOver && ticks < TICK_LIMIT) {
            battle.queueAction(slot)
            tick()
            ++ticks
        }

        assertTrue(battle.isOver)
    }

    @Test
    fun deadFighterDoesNotActAfterDying() {
        battle.enemy.health = 1
        battle.queueAction(RED_STRIKE)
        // give the enemy an attack it would perform this tick if it were still alive
        battle.enemy.addAttackToQueue()

        tick()

        assertTrue(battle.isOver)
        assertEquals(battle.player.maxHealth, battle.player.health)
    }

    @Test
    fun stoppedBattleDoesNotTick() {
        battle.stop()

        assertTrue(battle.isOver)
        assertTrue(battle.tick().isEmpty())
        assertEquals(0, battle.tickCount)
    }

    private companion object {
        // the default red fighter's buttons, against the default blue enemy
        const val RED_STRIKE = 0
        const val GREEN_MEND = 1
        const val GREEN_STRIKE = 2

        // 255 x green beats blue 1.5 x a red fighter's affinity for green
        const val GREEN_STRIKE_DAMAGE = 414
        // 255 x blue beats red 1.5 x blue strength 1.2 x the blue enemy's own affinity 1.25
        const val ENEMY_STRIKE_DAMAGE = 574
        // a fight should be over well before this many ticks
        const val TICK_LIMIT = 100
    }
}

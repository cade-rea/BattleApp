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
        battle.queueAction(ATTACK)
        tick()

        assertEquals(9, battle.enemy.health)
        assertTrue("Default Fighter attacks Enemy for 1 damage." in log)
    }

    @Test
    fun healingCannotGoAboveMaxHealth() {
        battle.queueAction(HEAL)
        tick()

        assertEquals(battle.player.maxHealth, battle.player.health)
    }

    @Test
    fun healingRestoresLostHealth() {
        battle.player.health = 5

        battle.queueAction(HEAL)
        tick()

        assertEquals(6, battle.player.health)
    }

    @Test
    fun emptyAndMissingButtonsQueueNothing() {
        assertFalse(battle.queueAction(2))
        assertFalse(battle.queueAction(3))
        assertFalse(battle.queueAction(-1))
        assertFalse(battle.queueAction(7))

        assertTrue(battle.player.queue.isEmpty())
    }

    @Test
    fun enemyAttacksEveryOtherTick() {
        repeat(4) { tick() }

        // the enemy queues an attack on ticks 2 and 4 and performs it on the following tick
        assertEquals(9, battle.player.health)
        tick()
        assertEquals(8, battle.player.health)
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
    fun attackingPlayerWinsAFullFight() {
        var ticks = 0
        while (!battle.isOver && ticks < TICK_LIMIT) {
            battle.queueAction(ATTACK)
            tick()
            ++ticks
        }

        assertTrue(battle.isOver)
        assertEquals(0, battle.enemy.health)
        assertTrue(battle.player.health > 0)
        assertEquals("Enemy has died.", log.last())
    }

    @Test
    fun deadFighterDoesNotActAfterDying() {
        battle.enemy.health = 1
        battle.queueAction(ATTACK)
        // give the enemy an attack it would perform this tick if it were still alive
        battle.enemy.addAttackToQueue()

        tick()

        assertTrue(battle.isOver)
        assertEquals(10, battle.player.health)
    }

    @Test
    fun stoppedBattleDoesNotTick() {
        battle.stop()

        assertTrue(battle.isOver)
        assertTrue(battle.tick().isEmpty())
        assertEquals(0, battle.tickCount)
    }

    private companion object {
        const val ATTACK = 0
        const val HEAL = 1
        // a fight should be over well before this many ticks
        const val TICK_LIMIT = 100
    }
}

package com.caderea.battleapp.core

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.EnemyFighter
import com.caderea.battleapp.core.fighter.Fighter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Drives the battle one tick at a time, with no clock. Most tests leave out the computer
 * opponent, so the enemy only does what the test queues for it.
 */
class BattleTest {

    private lateinit var battle: Battle
    private val log = mutableListOf<String>()

    @BeforeTest
    fun setUp() {
        battle = Battle(opponent = null)
        log.clear()
    }

    private fun tick() {
        log += battle.tick()
    }

    private val player get() = battle.player
    private val enemy get() = battle.enemy

    @Test
    fun actionsWindUpByColorBeforeTheyLand() {
        battle.queueAction(GREEN_STRIKE)

        // green winds up for 2 ticks: nothing on the first, the hit on the second
        tick()
        assertEquals(enemy.maxHealth, enemy.health)
        assertEquals(1, player.queue.peek()?.ticksLeft)

        tick()
        assertEquals(enemy.maxHealth - GREEN_STRIKE_DAMAGE, enemy.health)
        assertTrue("Default Fighter attacks Enemy with Green Strike for $GREEN_STRIKE_DAMAGE damage." in log)
        assertTrue(player.queue.isEmpty())
    }

    @Test
    fun landedListsWhatLandedOnTheLastTick() {
        battle.queueAction(GREEN_STRIKE)
        tick()
        assertTrue(battle.landed.isEmpty())

        tick()
        assertEquals(listOf("Green Strike"), battle.landed.map { it.action.name })
        assertEquals(player, battle.landed.single().performer)

        tick()
        assertTrue(battle.landed.isEmpty())
    }

    @Test
    fun redLandsOnTheNextTickAndBlueTakesThree() {
        battle.queueAction(RED_STRIKE)
        tick()
        assertTrue(enemy.health < enemy.maxHealth)

        enemy.queueAction(ENEMY_STRIKE)
        tick()
        tick()
        assertEquals(player.maxHealth, player.health)
        tick()
        assertEquals(player.maxHealth - ENEMY_STRIKE_DAMAGE, player.health)
    }

    @Test
    fun onlyTheFrontOfTheQueueWindsUp() {
        battle.queueAction(RED_STRIKE)
        battle.queueAction(GREEN_STRIKE)

        tick()
        // the green strike only starts winding up once the red one has landed
        assertEquals(2, player.queue.peek()?.ticksLeft)
        tick()
        tick()
        assertTrue(player.queue.isEmpty())
    }

    @Test
    fun ownColorIsWeakAgainstItsPredator() {
        battle.queueAction(RED_STRIKE)
        tick()

        // red into a blue enemy is a bad matchup, even with the red player's affinity bonus
        assertTrue(enemy.maxHealth - enemy.health < GREEN_STRIKE_DAMAGE / 2)
    }

    @Test
    fun queueingSpendsEnergyUpFront() {
        assertEquals(Fighter.STARTING_ENERGY, player.energy)

        assertTrue(battle.queueAction(RED_STRIKE))
        assertEquals(Fighter.STARTING_ENERGY - 10, player.energy)
        assertTrue(battle.queueAction(RED_STRIKE))
        assertEquals(0, player.energy)

        // there's room in the queue, but no energy left
        assertFalse(battle.queueAction(GREEN_STRIKE))
        assertEquals(2, player.queue.size)
    }

    @Test
    fun greenIsCheaper() {
        assertEquals(10, player.actions[RED_STRIKE]!!.cost)
        assertEquals(7, player.actions[GREEN_STRIKE]!!.cost)
        assertEquals(10, player.actions[BLUE_GUARD]!!.cost)
    }

    @Test
    fun energyComesBackEachTickUpToTheMax() {
        battle.queueAction(RED_STRIKE)
        tick()
        assertEquals(Fighter.STARTING_ENERGY - 10 + Fighter.ENERGY_PER_TICK, player.energy)

        repeat(10) { tick() }
        assertEquals(Fighter.MAX_ENERGY, player.energy)
    }

    @Test
    fun queueHoldsThreeActions() {
        player.energy = Fighter.MAX_ENERGY

        repeat(3) { assertTrue(battle.queueAction(GREEN_STRIKE)) }
        assertFalse(battle.queueAction(GREEN_STRIKE))
    }

    @Test
    fun cancelingTakesBackTheLastActionForHalfItsEnergy() {
        battle.queueAction(RED_STRIKE)
        battle.queueAction(GREEN_STRIKE)

        assertTrue(battle.cancelLast())

        assertEquals(1, player.queue.size)
        assertEquals("Red Strike", player.queue.peek()?.action?.name)
        // 20 - 10 - 7, then half of the 7 back
        assertEquals(7, player.energy)
    }

    @Test
    fun cancelingWithNothingQueuedDoesNothing() {
        assertFalse(battle.cancelLast())
        assertEquals(Fighter.STARTING_ENERGY, player.energy)
    }

    @Test
    fun guardLandingWithTheAttackBlocksIt() {
        // the enemy's blue strike and the player's blue guard both take 3 ticks
        enemy.queueAction(ENEMY_STRIKE)
        battle.queueAction(BLUE_GUARD)

        repeat(3) { tick() }

        assertEquals(player.maxHealth - ENEMY_STRIKE_DAMAGE + BLUE_GUARD_BLOCK, player.health)
        val blocked = ENEMY_STRIKE_DAMAGE - BLUE_GUARD_BLOCK
        assertTrue(
            "Enemy attacks Default Fighter with Strike for $blocked damage ($BLUE_GUARD_BLOCK blocked by Blue Guard)." in log
        )
        // the guard lands first, even though the player normally acts first anyway
        assertTrue(log.indexOf("Default Fighter raises Blue Guard.") < log.indexOfFirst { it.startsWith("Enemy attacks") })
    }

    @Test
    fun guardLandsBeforeTheEnemysAttackEvenWhenTheEnemyIsListedFirst() {
        val swapped = Battle(player = EnemyFighter(), enemy = Fighter(), opponent = null)
        // the second fighter's guard still lands before the first fighter's attack
        swapped.enemy.queueAction(BLUE_GUARD)
        swapped.player.queueAction(ENEMY_STRIKE)

        repeat(3) { swapped.tick() }

        assertEquals(swapped.enemy.maxHealth - ENEMY_STRIKE_DAMAGE + BLUE_GUARD_BLOCK, swapped.enemy.health)
    }

    @Test
    fun guardIsUpForTheTickItLandsAndTheNext() {
        battle.queueAction(BLUE_GUARD)
        repeat(3) { tick() }

        // still up for one more tick, so an attack landing then is blocked too
        assertEquals("Blue Guard", player.activeGuard?.guard?.name)
        assertEquals(1, player.activeGuard?.ticksLeft)

        tick()
        assertNull(player.activeGuard)
    }

    @Test
    fun guardInTheWrongColorBlocksNothing() {
        // the enemy's green guard against the player's red strike
        enemy.queueAction(ENEMY_GREEN_GUARD)
        tick()
        tick()
        battle.queueAction(RED_STRIKE)
        tick()

        val redDamage = enemy.maxHealth - enemy.health
        assertTrue(redDamage > 0)
        assertTrue(log.none { "blocked" in it })
    }

    @Test
    fun healingCannotGoAboveMaxHealth() {
        battle.queueAction(GREEN_MEND)
        tick()
        tick()

        assertEquals(player.maxHealth, player.health)
    }

    @Test
    fun healingRestoresLostHealth() {
        player.health = 500

        battle.queueAction(GREEN_MEND)
        tick()
        tick()

        // 255 x 0.5 x green support 1.3 x a red fighter's affinity for green
        assertEquals(680, player.health)
    }

    @Test
    fun emptyAndMissingButtonsQueueNothing() {
        val noFourthButton = Battle(player = Fighter(actions = Fighter.defaultActions().take(3) + null), opponent = null)

        assertFalse(noFourthButton.queueAction(3))
        assertFalse(noFourthButton.queueAction(-1))
        assertFalse(noFourthButton.queueAction(7))

        assertTrue(noFourthButton.player.queue.isEmpty())
    }

    @Test
    fun deadFighterDoesNotActAfterDying() {
        enemy.health = 1
        // the enemy's strike would land on the same tick as the player's
        enemy.queueAction(ENEMY_STRIKE)
        tick()
        tick()
        battle.queueAction(RED_STRIKE)

        tick()

        assertTrue(battle.isOver)
        assertEquals(player.maxHealth, player.health)
    }

    @Test
    fun stoppedBattleDoesNotTickOrTakeActions() {
        battle.stop()

        assertTrue(battle.isOver)
        assertTrue(battle.tick().isEmpty())
        assertEquals(0, battle.tickCount)
        assertFalse(battle.queueAction(RED_STRIKE))
    }

    @Test
    fun idlePlayerLosesAFullFight() {
        battle = Battle()
        runFight { }

        assertEquals(0, player.health)
        assertEquals("Default Fighter has died.", log.last())
    }

    @Test
    fun playerUsingTheRightColorWinsAFullFight() {
        battle = Battle()
        runFight { battle.queueAction(GREEN_STRIKE) }

        assertEquals(0, enemy.health)
        assertTrue(player.health > 0)
        assertEquals("Enemy has died.", log.last())
    }

    @Test
    fun playerUsingTheWrongColorLosesAFullFight() {
        battle = Battle()
        runFight { battle.queueAction(RED_STRIKE) }

        assertEquals(0, player.health)
        assertTrue(enemy.health > 0)
        assertEquals("Default Fighter has died.", log.last())
    }

    private fun runFight(playerMove: () -> Unit) {
        var ticks = 0
        while (!battle.isOver && ticks < TICK_LIMIT) {
            playerMove()
            tick()
            ++ticks
        }

        assertTrue(battle.isOver)
    }

    @Test
    fun actionsLandInTheLightOfTheirTick() {
        val arena = Arenas.TURNING
        battle = Battle(environment = arena, opponent = null)
        val redStrike = player.actions[RED_STRIKE] as Attack
        val plain = redStrike.damage(player, enemy, Environment())

        // red winds up in 1 tick, so it lands on tick 1 under the red light
        assertEquals(arena.lightAt(1), battle.conditions.lights.single())
        battle.queueAction(RED_STRIKE)
        tick()
        val underRed = enemy.maxHealth - enemy.health
        assertEquals(redStrike.damage(player, enemy, arena.at(1)), underRed)
        assertTrue(underRed > plain)

        // the light turns green after tick 4, and red does plain damage under it
        repeat(Arenas.TURN_TICKS - 1) { tick() }
        assertEquals(arena.lightAt(Arenas.TURN_TICKS + 1), battle.conditions.lights.single())
        val before = enemy.health
        battle.queueAction(RED_STRIKE)
        tick()
        assertEquals(plain, before - enemy.health)
    }

    private companion object {
        // the default red fighter's buttons
        const val RED_STRIKE = 0
        const val GREEN_MEND = 1
        const val GREEN_STRIKE = 2
        const val BLUE_GUARD = 3

        // the default blue enemy's buttons
        const val ENEMY_STRIKE = 0
        const val ENEMY_GREEN_GUARD = 1

        // 255 x green beats blue 1.5 x a red fighter's affinity for green
        const val GREEN_STRIKE_DAMAGE = 414
        // 255 x blue beats red 1.5 x blue strength 1.2 x the blue enemy's own affinity 1.25
        const val ENEMY_STRIKE_DAMAGE = 574
        // 574 x (0.5 base x blue support 1.0 x a red fighter's affinity for blue)
        const val BLUE_GUARD_BLOCK = 311
        // a fight should be over well before this many ticks
        const val TICK_LIMIT = 100
    }
}

package com.caderea.battleapp.core.ai

import com.caderea.battleapp.core.Battle
import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.EnemyFighter
import com.caderea.battleapp.core.fighter.Fighter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The computer opponent, driven directly rather than through a whole battle.
 */
class ComputerOpponentTest {

    private val environment = Environment()

    private lateinit var opponent: ComputerOpponent
    private lateinit var me: Fighter
    private lateinit var foe: Fighter

    @BeforeTest
    fun setUp() {
        opponent = ComputerOpponent()
        me = EnemyFighter()
        foe = Fighter()
        me.target = foe
        foe.target = me
    }

    private fun queuedNames(fighter: Fighter) = fighter.queue.map { it.action.name }

    @Test
    fun attacksWithItsHardestHitWhenNothingIsComing() {
        opponent.plan(me, foe, environment)

        assertEquals(listOf("Strike"), queuedNames(me))
    }

    @Test
    fun guardsATelegraphedAttackWithTheMatchingColor() {
        foe.queueAction(GREEN_STRIKE)

        opponent.react(me, foe)

        assertEquals(listOf("Green Guard"), queuedNames(me))
    }

    @Test
    fun reactingNeverStartsAnAttack() {
        opponent.react(me, foe)

        assertTrue(me.queue.isEmpty())
    }

    @Test
    fun doesNotGuardWhenTheGuardWouldLandTooLate() {
        // a red strike lands next tick; the green guard needs two
        foe.queueAction(RED_STRIKE)
        opponent.react(me, foe)
        assertTrue(me.queue.isEmpty())

        // a green strike behind its own blue strike can't be reached either
        me.queueAction(STRIKE)
        foe.queue.clear()
        foe.queueAction(GREEN_STRIKE)
        opponent.react(me, foe)
        assertEquals(listOf("Strike"), queuedNames(me))
    }

    @Test
    fun guardsTheNextAttackItCanStillReach() {
        foe.energy = Fighter.MAX_ENERGY
        // the red strike is too close to stop, but the green one behind it isn't
        foe.queueAction(RED_STRIKE)
        foe.queueAction(GREEN_STRIKE)

        opponent.react(me, foe)

        assertEquals(listOf("Green Guard"), queuedNames(me))
    }

    @Test
    fun doesNotGuardAgainstAColorItHasNoGuardFor() {
        val blueFoe = Fighter(actions = listOf(Attack("Blue Strike", RgbColor.BLUE)))
        blueFoe.queueAction(0)

        opponent.react(me, blueFoe)

        assertTrue(me.queue.isEmpty())
    }

    @Test
    fun neverGuardsTwiceInARow() {
        foe.queueAction(GREEN_STRIKE)
        opponent.react(me, foe)
        me.queue.clear()

        // another green strike, but it guarded last time, so now it attacks
        opponent.plan(me, foe, environment)

        assertEquals(listOf("Strike"), queuedNames(me))
    }

    @Test
    fun keepsItsQueueFreeToReact() {
        opponent.plan(me, foe, environment)
        opponent.plan(me, foe, environment)

        assertEquals(1, me.queue.size)
    }

    @Test
    fun healsWhenHurt() {
        me.health = me.maxHealth / 4

        opponent.plan(me, foe, environment)

        assertTrue(me.queue.peek()?.action is Heal)
    }

    @Test
    fun waitsWhenItCantAffordAnything() {
        me.energy = 0
        foe.queueAction(GREEN_STRIKE)

        opponent.react(me, foe)
        opponent.plan(me, foe, environment)

        assertTrue(me.queue.isEmpty())
    }

    @Test
    fun guardInABattleBlocksThePlayersStrike() {
        val battle = Battle()
        // take back the opponent's opening strike, so it's free to react
        battle.enemy.cancelLast()
        val healthBefore = battle.enemy.health

        battle.queueAction(GREEN_STRIKE)
        val log = battle.tick() + battle.tick()

        assertTrue(log.any { it == "Enemy raises Green Guard." })
        assertTrue(battle.enemy.activeGuard?.guard is Guard)
        // 414 - 292 blocked
        assertEquals(healthBefore - 122, battle.enemy.health)
    }

    private companion object {
        // the default red fighter's buttons
        const val RED_STRIKE = 0
        const val GREEN_STRIKE = 2

        // the enemy's buttons
        const val STRIKE = 0
    }
}

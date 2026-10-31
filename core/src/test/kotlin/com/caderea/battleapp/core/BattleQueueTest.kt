package com.caderea.battleapp.core

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BattleQueueTest {

    private val max = BattleQueue.MAX_SIZE
    private val attack = Attack(color = RgbColor.RED)
    private val heal = Heal(color = RgbColor.GREEN)

    private lateinit var queue: BattleQueue

    @BeforeTest
    fun setUp() {
        queue = BattleQueue()
    }

    @Test
    fun newQueueIsEmpty() {
        assertEquals(0, queue.size)
        assertTrue(queue.isEmpty())
        assertNull(queue.peek())
        assertNull(queue.poll())
    }

    @Test
    fun actionsComeOutInTheOrderTheyWentIn() {
        queue.offer(attack)
        queue.offer(heal)

        assertEquals(2, queue.size)
        assertSame(attack, queue.peek()?.action)
        assertSame(attack, queue.poll()?.action)
        assertSame(heal, queue.poll()?.action)
        assertTrue(queue.isEmpty())
    }

    @Test
    fun fullQueueRejectsMoreActions() {
        repeat(max) { assertTrue(queue.offer(attack)) }

        assertFalse(queue.offer(attack))
        assertEquals(max, queue.size)
        assertTrue(queue.isFull())
    }

    @Test
    fun queueHoldsThreeActions() {
        assertEquals(3, max)
    }

    @Test
    fun queueKeepsWorkingAfterManyAddsAndRemoves() {
        repeat(10) {
            repeat(max) { queue.offer(attack) }
            repeat(max - 1) { queue.poll() }
            queue.offer(heal)

            assertSame(attack, queue.poll()?.action)
            assertSame(heal, queue.poll()?.action)
            assertTrue(queue.isEmpty())
        }
    }

    @Test
    fun removeLastTakesTheNewestAction() {
        queue.offer(attack)
        queue.offer(heal)

        assertSame(heal, queue.removeLast()?.action)
        assertSame(attack, queue.removeLast()?.action)
        assertNull(queue.removeLast())
    }

    @Test
    fun newActionsHaveTheirWholeWindUpLeft() {
        val blueStrike = Attack(color = RgbColor.BLUE)
        queue.offer(blueStrike)

        assertEquals(3, queue.peek()?.ticksLeft)
        assertFalse(queue.peek()!!.isWindingUp)
    }

    @Test
    fun replaceFrontOnlyChangesTheFront() {
        queue.offer(heal)
        queue.offer(attack)

        queue.replaceFront(queue.peek()!!.copy(ticksLeft = 1))

        assertEquals(1, queue[0]?.ticksLeft)
        assertTrue(queue[0]!!.isWindingUp)
        assertEquals(attack.duration, queue[1]?.ticksLeft)
    }

    @Test
    fun ticksUntilLandsCountsTheWindUpsAhead() {
        // green 2, red 1, blue 3
        queue.offer(heal)
        queue.offer(attack)
        queue.offer(Attack(color = RgbColor.BLUE))

        assertEquals(2, queue.ticksUntilLands(0))
        assertEquals(3, queue.ticksUntilLands(1))
        assertEquals(6, queue.ticksUntilLands(2))
        assertEquals(6, queue.ticksUntilFree)
    }

    @Test
    fun eachQueuedActionGetsItsOwnId() {
        queue.offer(attack)
        queue.offer(attack)

        val ids = queue.map { it.id }

        assertEquals(listOf(0, 1), ids)
    }

    @Test
    fun iteratingDoesNotEmptyTheQueue() {
        queue.offer(attack)
        queue.offer(heal)

        val seen = queue.map { it.action }

        assertEquals(listOf(attack, heal), seen)
        assertEquals(2, queue.size)
    }

    @Test
    fun getIsRelativeToTheFront() {
        queue.offer(attack)
        queue.offer(heal)
        queue.poll()

        assertSame(heal, queue[0]?.action)
        assertNull(queue[1])
        assertNull(queue[-1])
    }

    @Test
    fun clearEmptiesTheQueue() {
        queue.offer(attack)
        queue.offer(attack)

        queue.clear()

        assertTrue(queue.isEmpty())
        assertTrue(queue.offer(attack))
    }
}

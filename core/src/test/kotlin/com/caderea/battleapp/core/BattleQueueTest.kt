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
    fun queueKeepsWorkingAfterManyAddsAndRemoves() {
        repeat(max) { queue.offer(attack) }
        repeat(7) { queue.poll() }
        assertEquals(max - 7, queue.size)

        repeat(5) { queue.offer(heal) }
        assertEquals(max - 2, queue.size)

        repeat(max - 7) { assertSame(attack, queue.poll()?.action) }
        repeat(5) { assertSame(heal, queue.poll()?.action) }
        assertTrue(queue.isEmpty())
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

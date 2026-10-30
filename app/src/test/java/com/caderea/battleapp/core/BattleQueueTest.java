package com.caderea.battleapp.core;

import com.caderea.battleapp.core.action.Attack;
import com.caderea.battleapp.view.battle.queue.QueueAction;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BattleQueueTest {

    private static final int MAX = BattleQueue.getMaxQueueSize();

    private BattleQueue queue;

    @Before
    public void setUp() {
        queue = new BattleQueue();
    }

    private static QueueAction action() {
        return new QueueAction(new Attack());
    }

    @Test
    public void newQueueIsEmpty() {
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        assertFalse(queue.isNotEmpty());
        assertNull(queue.peek());
        assertNull(queue.poll());
    }

    @Test
    public void actionsComeOutInTheOrderTheyWentIn() {
        QueueAction first = action();
        QueueAction second = action();

        queue.offer(first);
        queue.offer(second);

        assertEquals(2, queue.size());
        assertSame(first, queue.peek());
        assertSame(first, queue.poll());
        assertSame(second, queue.poll());
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    public void fullQueueRejectsMoreActions() {
        for (int i = 0; i < MAX; ++i) {
            assertTrue(queue.offer(action()));
        }

        assertFalse(queue.offer(action()));
        assertEquals(MAX, queue.size());
        assertTrue(queue.isNotEmpty());
    }

    @Test
    public void sizeIsCorrectAfterWrappingAround() {
        List<QueueAction> expected = new ArrayList<>();

        for (int i = 0; i < MAX; ++i) {
            queue.offer(action());
        }
        for (int i = 0; i < 7; ++i) {
            queue.poll();
        }
        assertEquals(MAX - 7, queue.size());

        // the tail wraps past the end of the buffer
        for (int i = 0; i < 5; ++i) {
            QueueAction a = action();
            expected.add(a);
            queue.offer(a);
        }
        assertEquals(MAX - 2, queue.size());

        for (int i = 0; i < MAX - 7; ++i) {
            queue.poll();
        }
        for (QueueAction a : expected) {
            assertSame(a, queue.poll());
        }
        assertTrue(queue.isEmpty());
    }

    @Test
    public void iteratingDoesNotEmptyTheQueue() {
        QueueAction first = action();
        QueueAction second = action();
        queue.offer(first);
        queue.offer(second);

        List<QueueAction> seen = new ArrayList<>();
        for (QueueAction a : queue) {
            seen.add(a);
        }

        assertEquals(2, seen.size());
        assertSame(first, seen.get(0));
        assertSame(second, seen.get(1));
        assertEquals(2, queue.size());
    }

    @Test
    public void getIsRelativeToHead() {
        QueueAction first = action();
        QueueAction second = action();
        queue.offer(first);
        queue.offer(second);
        queue.poll();

        assertSame(second, queue.get(0));
        assertNull(queue.get(1));
        assertNull(queue.get(-1));
        assertEquals("empty", queue.getName(1));
    }

    @Test
    public void toArrayReturnsQueuedActionsInOrder() {
        QueueAction first = action();
        QueueAction second = action();
        queue.offer(first);
        queue.offer(second);

        Object[] array = queue.toArray();

        assertEquals(2, array.length);
        assertSame(first, array[0]);
        assertSame(second, array[1]);
    }

    @Test
    public void clearEmptiesTheQueue() {
        queue.offer(action());
        queue.offer(action());

        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertTrue(queue.offer(action()));
    }
}

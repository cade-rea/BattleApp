package com.caderea.battleapp.core;


import com.caderea.battleapp.view.battle.queue.QueueAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Created by Cade on 8/4/2014.
 *
 * Fixed-size ring buffer of queued actions. The UI thread adds and reads actions while the
 * battle thread removes them, so every method that touches the buffer is synchronized.
 */
public class BattleQueue implements Queue<QueueAction> {

    private static final int MAX_QUEUE_SIZE = 10;

    private QueueAction[] queue;
    private int head;
    private int position;
    private boolean full;

    public BattleQueue() {
        queue = new QueueAction[MAX_QUEUE_SIZE];
        head = 0;
        position = 0;
        full = false;
    }

    @Override
    public boolean add(QueueAction queueAction) {
        return offer(queueAction);
    }

    @Override
    public boolean addAll(Collection<? extends QueueAction> queueActions) {
        return false;
    }

    @Override
    public synchronized boolean offer(QueueAction queueAction) {
        if(full)
            return false;

        queue[position] = queueAction;

        if(++position == MAX_QUEUE_SIZE)
            position = 0;

        if(position == head) {
            full = true;
        }

        return true;
    }

    @Override
    public QueueAction remove() {
        return poll();
    }

    @Override
    public synchronized QueueAction poll() {
        if(isEmpty())
            return null;

        QueueAction toReturn = queue[head];
        queue[head] = null;

        if(++head == MAX_QUEUE_SIZE)
            head = 0;

        full = false;

        return toReturn;
    }

    @Override
    public QueueAction element() {
        return peek();
    }

    @Override
    public synchronized QueueAction peek() {
        return queue[head];
    }

    @Override
    public synchronized boolean isEmpty() {
        return ((head == position) && !full);
    }

    //possibly more useful than isEmpty for Battle
    public synchronized boolean isNotEmpty() {
        return (full || (head != position));
    }

    /**
     * Iterates from head to tail without removing anything.
     */
    @Override
    public Iterator<QueueAction> iterator() {
        return new Iterator<QueueAction>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public QueueAction next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                return get(index++);
            }
        };
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        return false;
    }

    @Override
    public synchronized int size() {
        if(full)
            return MAX_QUEUE_SIZE;

        int size = position - head;
        if(size < 0)
            size += MAX_QUEUE_SIZE;

        return size;
    }

    @Override
    public synchronized Object[] toArray() {
        return toList().toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] ts) {
        return toList().toArray(ts);
    }

    private List<QueueAction> toList() {
        List<QueueAction> list = new ArrayList<>(size());
        for (int i = 0; i < size(); ++i) {
            list.add(get(i));
        }
        return list;
    }

    @Override
    public synchronized void clear() {
        queue = new QueueAction[MAX_QUEUE_SIZE];
        head = position = 0;
        full = false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        return false;
    }

    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < MAX_QUEUE_SIZE; ++i) {
            sb.append("[" + i + "] " + queue[i]);
        }
        return sb.toString();
    }

    public synchronized String getName(int index) {
        QueueAction queueAction = get(index);

        if (queueAction != null)
            return queueAction.toString();

        return "empty";
    }

    /**
     * Retrieves the QueueAction at the given queue index.
     * The index is relative to head, eg, get(0) == head
     * @param index
     * @return the action, or null if index is outside the queued actions
     */
    public synchronized QueueAction get(int index) {
        if (index < 0 || index >= size())
            return null;

        int target = head + index;

        if (target >= MAX_QUEUE_SIZE)
            target -= MAX_QUEUE_SIZE;

        return queue[target];
    }

    public static int getMaxQueueSize() {
        return MAX_QUEUE_SIZE;
    }
}

package com.caderea.battleapp.core;


import com.caderea.battleapp.view.battle.queue.QueueAction;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by Cade on 8/4/2014.
 */
public class BattleQueue implements Queue<QueueAction> {

    private static int MAX_QUEUE_SIZE = 10;

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

    private int getPlace() {
        int place = head + position;
        if(place >= MAX_QUEUE_SIZE)
            place -= MAX_QUEUE_SIZE;

        return place;
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
    public boolean offer(QueueAction queueAction) {
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
    public QueueAction poll() {
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
    public QueueAction peek() {
        return queue[head];
    }

    @Override
    public boolean isEmpty() {
        return ((head == position) && !full);
    }

    //possibly more useful than isEmpty for Battle
    public boolean isNotEmpty() {
        return (full || (head != position));
    }

    @Override
    public Iterator<QueueAction> iterator() {
        return new Iterator<QueueAction>() {
            @Override
            public boolean hasNext() {
                return !isEmpty();
            }

            @Override
            public QueueAction next() {
                return poll();
            }

            @Override
            public void remove() {
                //this method doesn't do anything
                return;
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
    public int size() {
        int size = position - head;
        if(size <= 0)
            size += MAX_QUEUE_SIZE;

        return size;
    }

    @Override
    public Object[] toArray() {
        return queue;
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return null;
    }

    @Override
    public void clear() {
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < MAX_QUEUE_SIZE; ++i) {
            sb.append("[" + i + "] " + queue[i]);
        }
        return sb.toString();
    }

    public String getName(int index) {
        int target = head + index;

        if (target >= MAX_QUEUE_SIZE)
            target -= MAX_QUEUE_SIZE;

        if (queue[target] != null)
            return queue[target].toString();

        return "empty";
    }

    /**
     * Retrieves the QueueAction at the given queue index.
     * The index is relative to head, eg, get(0) == head
     * @param index
     * @return
     */
    public QueueAction get(int index) {
        int target = head + index;

        if (target >= MAX_QUEUE_SIZE)
            target -= MAX_QUEUE_SIZE;

        if (queue[target] != null)
            return queue[target];

        return null;
    }

    public static int getMaxQueueSize() {
        return MAX_QUEUE_SIZE;
    }
}

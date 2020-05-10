package com.caderea.battleapp.core;

import com.caderea.battleapp.core.action.BattleAction;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by Cade on 8/4/2014.
 */
public class BattleQueue implements Queue<BattleAction> {

    private static final int MAX_QUEUE_SIZE = 10;

    private BattleAction[] queue;
    private int head;
    private int position;
    private boolean full;

    public BattleQueue() {
        queue = new BattleAction[MAX_QUEUE_SIZE];
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
    public boolean add(BattleAction battleAction) {
        return offer(battleAction);
    }

    @Override
    public boolean addAll(Collection<? extends BattleAction> battleActions) {
        return false;
    }

    @Override
    public boolean offer(BattleAction battleAction) {
        if(full)
            return false;

        queue[position] = battleAction;

        if(++position == MAX_QUEUE_SIZE)
            position = 0;

        if(position == head) {
            full = true;
        }

        return true;
    }

    @Override
    public BattleAction remove() {
        return poll();
    }

    @Override
    public BattleAction poll() {
        if(isEmpty())
            return null;

        BattleAction toReturn = queue[head];
        queue[head] = null;

        if(++head == MAX_QUEUE_SIZE)
            head = 0;

        full = false;

        return toReturn;
    }

    @Override
    public BattleAction element() {
        return peek();
    }

    @Override
    public BattleAction peek() {
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
    public Iterator<BattleAction> iterator() {
        return new Iterator<BattleAction>() {
            @Override
            public boolean hasNext() {
                return !isEmpty();
            }

            @Override
            public BattleAction next() {
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
        queue = new BattleAction[MAX_QUEUE_SIZE];
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
     * Retrieves the BattleAction at the given queue index.
     * The index is relative to head, eg, get(0) == head
     * @param index
     * @return
     */
    public BattleAction get(int index) {
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
